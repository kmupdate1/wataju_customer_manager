package jp.wataju.model

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class OrderService(
    databaseConfig: ApplicationConfig
): Service(Orders, databaseConfig) {

    object Orders: Table("orders") {
        val id = uuid("id").autoGenerate()
        val customerId = uuid("customer_id") references CustomerService.Customers.id
        val condition = largeText("condition")
        val orderDate = varchar("order_date", 50)
        val createDate = varchar("create_date", 50)
        val updateDate = varchar("update_date", 50)
        val createAccount = uuid("create_account") references AccountService.Accounts.id
        val updateAccount = uuid("update_account") references AccountService.Accounts.id

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(order: Order): UUID = dbQuery {
        Orders.insert {
            it[customerId] = order.customerId
            it[condition] = order.condition
            it[orderDate] = order.orderDate
            it[createDate] = order.createDate
            it[updateDate] = order.updateDate
            it[createAccount] = order.createAccount
            it[updateAccount] = order.updateAccount
        }[Orders.id]
    }

    suspend fun read(): MutableList<Order?> {
        val orders = mutableListOf<Order?>()
        dbQuery {
            Orders.selectAll().orderBy(Orders.orderDate)
                .forEach {
                    orders.add(
                        Order(
                            it[Orders.id],
                            it[Orders.customerId],
                            it[Orders.condition],
                            it[Orders.orderDate],
                            it[Orders.createDate],
                            it[Orders.updateDate],
                            it[Orders.createAccount],
                            it[Orders.updateAccount]
                        )
                    )
                }
        }
        return orders
    }

    suspend fun read(id: UUID): Order? = dbQuery {
        Orders.select { Orders.id eq id }
            .map {
                Order(
                    it[Orders.id],
                    it[Orders.customerId],
                    it[Orders.condition],
                    it[Orders.orderDate],
                    it[Orders.createDate],
                    it[Orders.updateDate],
                    it[Orders.createAccount],
                    it[Orders.updateAccount]
                )
            }
            .singleOrNull()
    }

    suspend fun readByCustomer(customerId: UUID): MutableList<Order?> {
        val orders = mutableListOf<Order?>()
        dbQuery {
            Orders.select { Orders.customerId eq customerId }.orderBy(Orders.orderDate)
                .forEach {
                    orders.add(
                        Order(
                            it[Orders.id],
                            it[Orders.customerId],
                            it[Orders.condition],
                            it[Orders.orderDate],
                            it[Orders.createDate],
                            it[Orders.updateDate],
                            it[Orders.createAccount],
                            it[Orders.updateAccount]
                        )
                    )
                }
        }
        return orders
    }

    suspend fun update(id: UUID, order: Order) {
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[customerId] = order.customerId
                it[condition] = order.condition
                it[orderDate] = order.orderDate
                it[updateDate] = order.updateDate
                it[updateAccount] = order.updateAccount
            }
        }
    }

    suspend fun delete(id: UUID) {
        dbQuery {
            Orders.deleteWhere { Orders.id eq id }
        }
    }
}

data class Order(
    val id: UUID?,
    val customerId: UUID,
    val condition: String,
    val orderDate: String,
    val createDate: String,
    val updateDate: String,
    val createAccount: UUID,
    val updateAccount: UUID
)
