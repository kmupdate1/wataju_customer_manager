package jp.wataju.model

import io.ktor.server.config.*
import jp.wataju.TAX_INCLUDE
import jp.wataju.util.Properties
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*
import kotlin.math.roundToInt

class ProductService(
    databaseConfig: ApplicationConfig
): Service(Products, databaseConfig) {

    object Products: Table("products") {
        val id = uuid("id").autoGenerate()
        val productName = varchar("product_name", 100)
        val productNameKana = varchar("product_name_kana", 100)
        val price = integer("price")
        val enabled = bool("enabled")
        val createDate = varchar("create_date", 50)
        val updateDate = varchar("update_date", 50)
        val createAccount = uuid("create_account") references AccountService.Accounts.id
        val updateAccount = uuid("update_account") references AccountService.Accounts.id

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(product: Product): UUID = dbQuery {
        Products.insert {
            it[productName] = product.productName
            it[productNameKana] = product.productNameKana
            it[price] = product.price
            it[enabled] = product.enabled
            it[createDate] = product.createDate
            it[updateDate] = product.updateDate
            it[createAccount] = product.createAccount
            it[updateAccount] = product.updateAccount
        }[Products.id]
    }

    suspend fun read(): MutableList<Product?> {
        val products = mutableListOf<Product?>()
        dbQuery {
            Products.selectAll().orderBy(Products.enabled, SortOrder.DESC)
                .forEach {
                    products.add(
                        Product(
                            it[Products.id],
                            it[Products.productName],
                            it[Products.productNameKana],
                            it[Products.price],
                            it[Products.enabled],
                            it[Products.createDate],
                            it[Products.updateDate],
                            it[Products.createAccount],
                            it[Products.updateAccount]
                        )
                    )
                }
        }
        return products
    }

    suspend fun read(id: UUID) = dbQuery {
        Products.select { Products.id eq id }
            .map {
                Product(
                    it[Products.id],
                    it[Products.productName],
                    it[Products.productNameKana],
                    it[Products.price],
                    it[Products.enabled],
                    it[Products.createDate],
                    it[Products.updateDate],
                    it[Products.createAccount],
                    it[Products.updateAccount]
                )
            }
            .singleOrNull()
    }

    suspend fun update(id: UUID, product: Product) {
        dbQuery {
            Products.update({ Products.id eq id }) {
                it[productName] = product.productName
                it[productNameKana] = product.productNameKana
                it[price] = product.price
                it[enabled] = product.enabled
                it[updateDate] = product.updateDate
                it[updateAccount] = product.updateAccount
            }
        }
    }

    suspend fun delete(id: UUID) {
        dbQuery {
            Products.deleteWhere { Products.id eq id }
        }
    }

}

data class Product(
    val id: UUID?,
    val productName: String,
    val productNameKana: String,
    val price: Int,
    val enabled: Boolean,
    val createDate: String,
    val updateDate: String,
    val createAccount: UUID,
    val updateAccount: UUID
) {
    val priceFormatted = String.format("%,d", price)
    val priceTaxFormatted = String.format("%,d", (price * TAX_INCLUDE).roundToInt())
    val condition = if (enabled) {
        Properties.getText("TRUE")
    } else {
        Properties.getText("FALSE")
    }
}
