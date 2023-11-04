package jp.wataju

import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import jp.wataju.model.*
import jp.wataju.session.AccountSession
import jp.wataju.util.IsNumeric
import jp.wataju.util.parser.OrderCondition
import jp.wataju.util.pool.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.math.roundToInt

fun Application.routing() {
    val databaseConfig = environment.config.config("database")

    routing {
        route(LOGIN) {
            get {
                val model = mapOf(
                    "tab" to TAB,
                    "title" to TITLE,
                    "message" to PLEASE_LOGIN,
                    "alert" to true
                )

                call.respond(MustacheContent("authentication/login.hbs", model))
            }
            post {
                val loginParams = call.receiveParameters()
                val identifier = loginParams["identifier"] ?: ""
                val password = loginParams["password"] ?: ""

                val account = AccountService(databaseConfig).read(identifier)
                if (account != null) {
                    if (account.password != password) {
                        val model = mapOf(
                            "tab_title" to TAB,
                            "title" to TITLE,
                            "message" to LOGIN_FAILURE,
                            "alert" to true
                        )

                        call.respond(MustacheContent("authentication/login.hbs", model))
                    } else {
                        call.sessions.set(
                            AccountSession(account.id, account.identifier, account.admin)
                        )
                        call.respondRedirect(INDEX)
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
        }
        route(SIGNING) {
            get {
                val model = mapOf(
                    "tab" to TAB,
                    "title" to TITLE,
                    "message" to CREATE_ACCOUNT,
                    "alert" to false
                )

                call.respond(MustacheContent("authentication/signing.hbs", model))
            }
            post {
                val signingParams = call.receiveParameters()
                val identifier = signingParams["identifier"] ?: ""
                val password = signingParams["password"] ?: ""
                val passwordConfirm = signingParams["password-confirm"] ?: ""
                val admin = signingParams["admin"] ?: ""

                val account = AccountService(databaseConfig).read(identifier)
                if (account != null) {
                    val module = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "message" to ACCOUNT_ALREADY_EXISTS,
                        "alert" to true
                    )

                    call.respond(MustacheContent("authentication/signing.hbs", module))
                } else {
                    if ((password != passwordConfirm) or (password == "")) {
                        val model = mapOf(
                            "tab" to TAB,
                            "title" to TITLE,
                            "message" to PASSWORD_FAILURE,
                            "alert" to true
                        )

                        call.respond(MustacheContent("authentication/signing.hbs", model))
                    } else {
                        var isAdmin = false
                        if (admin == "on") isAdmin = true

                        val id = UUID.randomUUID()
                        val date = Date().toString()
                        AccountService(databaseConfig).create(
                            Account(
                                id = id,
                                identifier = identifier,
                                password = password,
                                admin = isAdmin,
                                createDate = date,
                                updateDate = date,
                                createAccount = id,
                                updateAccount = id
                            )
                        )

                        call.respondRedirect(LOGIN)
                    }
                }
            }
        }
        route(CUSTOMER) {
            get("/list") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                val customers = CustomerService(databaseConfig).read()
                if (session.id != null) {
                    val module = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "customers" to customers
                    )

                    call.respond(MustacheContent("customer/list.hbs", module))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/edit") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "new_customer" to true
                    )

                    call.respond(MustacheContent("customer/edit.hbs", model))

                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            post("/confirm") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerInfoParams = call.receiveParameters()

                    val familyName = customerInfoParams["family-name"] ?: ""
                    val firstName = customerInfoParams["first-name"] ?: ""
                    if ((familyName != "") and  (firstName != "")) {
                        RegistryStorage.customerRegistry = CustomerRegistry(
                            familyName = familyName,
                            firstName = firstName,
                            familyNameKana = customerInfoParams["family-name-kana"] ?: "",
                            firstNameKane = customerInfoParams["first-name-kana"] ?: "",
                            zipcodeWard = customerInfoParams["zipcode-ward"] ?: "",
                            zipcodeTown = customerInfoParams["zipcode-town"] ?: "",
                            prefecture = customerInfoParams["prefecture"] ?: "",
                            address1 = customerInfoParams["address-1"] ?: "",
                            address2 = customerInfoParams["address-2"] ?: "",
                            address3 = customerInfoParams["address-3"] ?: "",
                            phoneNumber = customerInfoParams["phone-number"] ?: "",
                            mailAddress = customerInfoParams["mail-address"] ?: ""
                        )

                        val model = mapOf(
                            "tab" to TAB,
                            "title" to TITLE,
                            "identifier" to session.identifier,
                            "customer" to RegistryStorage.customerRegistry,
                            "new_customer" to true
                        )

                        call.respond(MustacheContent("customer/confirm.hbs", model))
                    } else {
                        call.respondRedirect("$CUSTOMER/edit")
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/registry") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerPool = RegistryStorage.customerRegistry
                    val date = Date().toString()
                    val id = session.id
                    CustomerService(databaseConfig).create(
                        Customer(
                            null,
                            customerPool.firstName,
                            customerPool.familyName,
                            customerPool.firstNameKane,
                            customerPool.familyNameKana,
                            customerPool.zipcodeWard,
                            customerPool.zipcodeTown,
                            customerPool.prefecture,
                            customerPool.address1,
                            customerPool.address2,
                            customerPool.address3,
                            customerPool.phoneNumber,
                            customerPool.mailAddress,
                            date,
                            date,
                            id,
                            id
                        )
                    )

                    call.respondRedirect("$CUSTOMER/list")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)
                val customerId = call.parameters["customerId"]

                if (session.id != null) {
                    val customer = CustomerService(databaseConfig).read(UUID.fromString(customerId))
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "admin" to session.admin,
                        "customer" to customer
                    )

                    call.respond(MustacheContent("customer/detail.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/edit/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = call.parameters["customerId"] ?: ""
                    val customer = CustomerService(databaseConfig).read(UUID.fromString(customerId))
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "customer" to customer,
                        "new_customer" to false
                    )

                    call.respond(MustacheContent("customer/edit.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            post("/confirm/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerInfoParams = call.receiveParameters()

                    val customerId = call.parameters["customerId"] ?: ""
                    val familyName = customerInfoParams["family-name"] ?: ""
                    val firstName = customerInfoParams["first-name"] ?: ""
                    if ((familyName != "") || (firstName != "")) {
                        RegistryStorage.customerRegistry = CustomerRegistry(
                            familyName = familyName,
                            firstName = firstName,
                            familyNameKana = customerInfoParams["family-name-kana"] ?: "",
                            firstNameKane = customerInfoParams["first-name-kana"] ?: "",
                            zipcodeWard = customerInfoParams["zipcode-ward"] ?: "",
                            zipcodeTown = customerInfoParams["zipcode-town"] ?: "",
                            prefecture = customerInfoParams["prefecture"] ?: "",
                            address1 = customerInfoParams["address-1"] ?: "",
                            address2 = customerInfoParams["address-2"] ?: "",
                            address3 = customerInfoParams["address-3"] ?: "",
                            phoneNumber = customerInfoParams["phone-number"] ?: "",
                            mailAddress = customerInfoParams["mail-address"] ?: ""
                        )

                        val old = CustomerService(databaseConfig).read(UUID.fromString(customerId))
                        val model = mapOf(
                            "tab" to TAB,
                            "title" to TITLE,
                            "identifier" to session.identifier,
                            "old" to old,
                            "customer" to RegistryStorage.customerRegistry,
                            "new_customer" to false
                        )

                        call.respond(MustacheContent("customer/confirm.hbs", model))
                    } else {
                        call.respondRedirect("$CUSTOMER/edit/$customerId")
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/registry/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = call.parameters["customerId"] ?: ""

                    val customerPool = RegistryStorage.customerRegistry
                    val date = Date().toString()
                    val id = session.id
                    CustomerService(databaseConfig).update(
                        UUID.fromString(customerId),
                        Customer(
                            id = null,
                            firstName = customerPool.firstName,
                            familyName = customerPool.familyName,
                            firstNameKana = customerPool.firstNameKane,
                            familyNameKana = customerPool.familyNameKana,
                            zipcodeWard = customerPool.zipcodeWard,
                            zipcodeTown = customerPool.zipcodeTown,
                            prefecture = customerPool.prefecture,
                            address1 = customerPool.address1,
                            address2 = customerPool.address2,
                            address3 = customerPool.address3,
                            phoneNumber = customerPool.phoneNumber,
                            mailAddress = customerPool.mailAddress,
                            createDate = date,
                            updateDate = date,
                            createAccount = id,
                            updateAccount = id
                        )
                    )
                    call.respondRedirect("$CUSTOMER/list")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/delete/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = call.parameters["customerId"] ?: ""
                    CustomerService(databaseConfig).delete(UUID.fromString(customerId))

                    call.respondRedirect("$CUSTOMER/list")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
        }
        route(PRODUCT) {
            get("/list") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val products = ProductService(databaseConfig).read()
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "admin" to session.admin,
                        "products" to products
                    )

                    call.respond(MustacheContent("product/list.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/edit") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "new_product" to true
                    )

                    call.respond(MustacheContent("product/edit.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            post("/confirm") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val productInfoParams = call.receiveParameters()

                    val productName = productInfoParams["product-name"] ?: ""
                    val numberOrString = productInfoParams["price"] ?: ""
                    val isNumeric = IsNumeric.isNumeric(numberOrString)
                    var enabled = Enabled.FALSE
                    if (productInfoParams["enabled"] == "on") enabled = Enabled.TRUE

                    if ((productName != "") and isNumeric) {
                        RegistryStorage.productRegistry = ProductRegistry(
                            productName = productName,
                            productNameKana = productInfoParams["product-name-kana"] ?: "",
                            price = numberOrString.toInt(),
                            enabled = enabled
                        )

                        val model = mapOf(
                            "tab" to TAB,
                            "title" to TITLE,
                            "identifier" to session.identifier,
                            "product" to RegistryStorage.productRegistry,
                            "new_product" to true
                        )

                        call.respond(MustacheContent("product/confirm.hbs", model))
                    } else {
                        call.respondRedirect("$PRODUCT/edit")
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/registry") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val productPool = RegistryStorage.productRegistry
                    val date = Date().toString()
                    val id = session.id
                    ProductService(databaseConfig).create(
                        Product(
                            id = null,
                            productName = productPool.productName,
                            productNameKana = productPool.productNameKana,
                            price = productPool.price,
                            enabled = productPool.enabled.enabled,
                            createDate = date,
                            updateDate = date,
                            createAccount = id,
                            updateAccount = id
                        )
                    )

                    call.respondRedirect("$PRODUCT/list")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/{productId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)
                val productId = call.parameters["productId"] ?: ""

                if (session.id != null) {
                    val product = ProductService(databaseConfig).read(UUID.fromString(productId))
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "admin" to session.admin,
                        "product" to product
                    )

                    call.respond(MustacheContent("product/detail.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/edit/{productId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val productId = call.parameters["productId"] ?: ""
                    val product = ProductService(databaseConfig).read(UUID.fromString(productId))
                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "product" to product,
                        "new_product" to false
                    )

                    call.respond(MustacheContent("product/edit.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            post("/confirm/{productId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val productInfoParams = call.receiveParameters()

                    val productId = call.parameters["productId"] ?: ""
                    val productName = productInfoParams["product-name"] ?: ""
                    val numberOrString = productInfoParams["price"] ?: ""
                    val isNumeric = IsNumeric.isNumeric(numberOrString)
                    var enabled = Enabled.FALSE
                    if (productInfoParams["enabled"] == "on") enabled = Enabled.TRUE

                    if ((productName != "") or isNumeric) {
                        RegistryStorage.productRegistry = ProductRegistry(
                            productName = productName,
                            productNameKana = productInfoParams["product-name-kana"] ?: "",
                            price = numberOrString.toInt(),
                            enabled = enabled
                        )

                        val old = ProductService(databaseConfig).read(UUID.fromString(productId))
                        val model = mapOf(
                            "tab" to TAB,
                            "title" to TITLE,
                            "identifier" to session.identifier,
                            "old" to old,
                            "product" to RegistryStorage.productRegistry,
                            "new_product" to false
                        )

                        call.respond(MustacheContent("product/confirm.hbs", model))
                    } else {
                        call.respondRedirect("$PRODUCT/edit/$productId")
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/registry/{productId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val productId = call.parameters["productId"] ?: ""

                    val productPool = RegistryStorage.productRegistry
                    val date = Date().toString()
                    val id = session.id
                    ProductService(databaseConfig).update(
                        UUID.fromString(productId),
                        Product(
                            id = null,
                            productName = productPool.productName,
                            productNameKana = productPool.productNameKana,
                            price = productPool.price,
                            enabled = productPool.enabled.enabled,
                            createDate = date,
                            updateDate = date,
                            createAccount = id,
                            updateAccount = id
                        )
                    )

                    call.respondRedirect("$PRODUCT/list")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
        }
        route(ORDER) {
            get("/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = UUID.fromString(call.parameters["customerId"])

                    val customer = CustomerService(databaseConfig).read(customerId)
                    val orders = OrderService(databaseConfig).readByCustomer(customerId)

                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "customer" to customer,
                        "orders" to orders
                    )

                    call.respond(MustacheContent("order/list.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/{customerId}/{orderId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = UUID.fromString(call.parameters["customerId"])
                    val orderId = UUID.fromString(call.parameters["orderId"])

                    val orderData = mutableListOf<Map<String, Any>>()
                    val totalData = mutableListOf<Map<String, Any>>()
                    var amounts = 0

                    val customer = CustomerService(databaseConfig).read(customerId)
                    val order = OrderService(databaseConfig).read(orderId)
                    var product: Product? = null
                    Json.decodeFromString(ListSerializer(OrderCondition.serializer()), order!!.condition)
                        .forEach {
                            val amount = it.amount
                            if (amount != 0) {
                                product = ProductService(databaseConfig).read(UUID.fromString(it.id))

                                orderData.add(
                                    mapOf(
                                        "product_name" to product!!.productName,
                                        "product_price" to String.format("%,d", product!!.price),
                                        "product_amount" to amount,
                                        "product_prices" to String.format("%,d", amount * product!!.price)
                                    )
                                )
                                amounts += amount
                            }
                        }
                    val price = amounts * product!!.price
                    totalData.add(
                        mapOf(
                            "product_amounts" to amounts,
                            "product_prices" to String.format("%,d", price),
                            "product_taxes" to String.format("%,d", (price * TAX_INCLUDE).roundToInt())
                        )
                    )

                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "customer" to customer,
                        "order" to order,
                        "order_data" to orderData,
                        "total_data" to totalData
                    )

                    call.respond(MustacheContent("order/detail.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/edit/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = call.parameters["customerId"]
                    val customer = CustomerService(databaseConfig).read(UUID.fromString(customerId))
                    val products = ProductService(databaseConfig).read()

                    val model = mapOf(
                        "tab" to TAB,
                        "title" to TITLE,
                        "identifier" to session.identifier,
                        "customer" to customer,
                        "products" to products
                    )

                    call.respond(MustacheContent("order/edit.hbs", model))
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            post("/confirm/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = call.parameters["customerId"]

                    val orderParams = call.receiveParameters()
                    val purchaseDate = orderParams["purchase-date"] ?: ""
                    if (purchaseDate != "") {
                        val separated = purchaseDate.split("-")
                        val formattedPurchaseDate = "${separated[0]}年${separated[1]}月${separated[2]}日"
                        val ordersMap = mutableMapOf<UUID, Int>()
                        orderParams.forEach { name, values ->
                            if (name != "purchase-date") {
                                val amount = values[0].toInt()
                                ordersMap[UUID.fromString(name)] = amount
                            }
                        }
                        RegistryStorage.orderRegistry = OrderRegistry(
                            UUID.fromString(customerId),
                            ordersMap,
                            formattedPurchaseDate
                        )

                        val customer = CustomerService(databaseConfig).read(UUID.fromString(customerId))
                        val orders = arrayListOf<Map<String, Any>>()
                        var amounts = 0
                        var totalPrice = 0
                        ordersMap.forEach {
                            val product = ProductService(databaseConfig).read(it.key)
                            val amount = it.value

                            if ((product != null) and (amount != 0)) if (product != null) {
                                run {
                                    amounts += amount
                                    totalPrice += amount * product.price
                                    orders.add(
                                        mapOf(
                                            "product_name" to product.productName,
                                            "amount" to amount,
                                            "price" to String.format("%,d", amount * product.price)
                                        )
                                    )
                                }
                            }
                        }

                        if (orders.isNotEmpty()) {
                            val totals = arrayListOf<Map<String, Any>>(
                                mapOf(
                                    "amounts" to amounts,
                                    "price" to String.format("%,d", totalPrice),
                                    "price_tax" to String.format("%,d", (totalPrice * TAX_INCLUDE).roundToInt())
                                )
                            )

                            val model = mapOf(
                                "tab" to TAB,
                                "title" to TITLE,
                                "identifier" to session.identifier,
                                "customer" to customer,
                                "orders" to orders,
                                "totals" to totals,
                                "order_date" to formattedPurchaseDate
                            )

                            call.respond(MustacheContent("order/confirm.hbs", model))
                        } else {
                            call.respondRedirect("$ORDER/edit/$customerId")
                        }
                    } else {
                        call.respondRedirect("$ORDER/edit/$customerId")
                    }
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
            get("/registry/{customerId}") {
                val session = call.sessions.get() ?: AccountSession(null, null, null)

                if (session.id != null) {
                    val customerId = UUID.fromString(call.parameters["customerId"])
                    val date = Date().toString()
                    val accountId = session.id

                    val conditions = arrayListOf<OrderCondition>()
                    RegistryStorage.orderRegistry.orders.forEach {
                        conditions.add(OrderCondition(it.key.toString(), it.value))
                    }

                    OrderService(databaseConfig).create(
                        Order(
                            id = null,
                            customerId = customerId,
                            condition = Json.encodeToString(conditions),
                            orderDate = RegistryStorage.orderRegistry.orderDate,
                            createDate = date,
                            updateDate = date,
                            createAccount = accountId,
                            updateAccount = accountId
                        )
                    )

                    call.respondRedirect("$ORDER/$customerId")
                } else {
                    call.respondRedirect(LOGIN)
                }
            }
        }
        route(SETTING) {
            get("/top") {

            }
        }
        get(INDEX) {
            val session = call.sessions.get() ?: AccountSession(null, null, null)

            if (session.id != null) {
                val model = mapOf(
                    "tab" to TAB,
                    "title" to TITLE,
                    "identifier" to session.identifier,
                    "alert" to false
                )

                call.respond(MustacheContent("/index.hbs", model))
            } else {
                call.respondRedirect(LOGIN)
            }
        }
        get("/") {
            call.respondRedirect(LOGIN)
        }
        get("/logout") {
            call.sessions.set(AccountSession(null, null, null))
            call.respondRedirect(LOGIN)
        }
    }
}

/*

  val session = call.sessions.get() ?: AccountSession(null, null, null)

  if (session.id != null) {

  } else {
      call.respondRedirect(LOGIN)
  }

 */
