package jp.wataju.model

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class CustomerService(
    databaseConfig: ApplicationConfig
): Service(Customers, databaseConfig) {

    object Customers: Table("customers") {
        val id = uuid("id").autoGenerate()
        val firstName = text("first_name")
        val familyName = text("family_name")
        val firstNameKana = text("first_name_kana")
        val familyNameKana = text("family_name_kana")
        val zipcodeWard = varchar("zipcode_ward", 10)
        val zipcodeTown = varchar("zipcode_town", 10)
        val prefecture = varchar("prefecture", 50)
        val address1 = text("address_1")
        val address2 = text("address_2")
        val address3 = text("address_3")
        val phoneNumber = text("phone_number")
        val mailAddress = text("mail_address")
        val createDate = varchar("create_date", 50)
        val updateDate = varchar("update_date", 50)
        val createAccount = uuid("create_account") references AccountService.Accounts.id
        val updateAccount = uuid("update_account") references AccountService.Accounts.id

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(customer: Customer): UUID = dbQuery {
        Customers.insert {
            it[firstName] = customer.firstName
            it[familyName] = customer.familyName
            it[firstNameKana] = customer.firstNameKana
            it[familyNameKana] = customer.familyNameKana
            it[zipcodeWard] = customer.zipcodeWard
            it[zipcodeTown] = customer.zipcodeTown
            it[prefecture] = customer.prefecture
            it[address1] = customer.address1
            it[address2] = customer.address2
            it[address3] = customer.address3
            it[phoneNumber] = customer.phoneNumber
            it[mailAddress] = customer.mailAddress
            it[createDate] = customer.createDate
            it[updateDate] = customer.updateDate
            it[createAccount] = customer.createAccount
            it[updateAccount] = customer.updateAccount
        }[Customers.id]
    }

    suspend fun read(): MutableList<Customer?> {
        val customer = mutableListOf<Customer?>()
        dbQuery {
            Customers.selectAll().orderBy(Customers.familyNameKana)
                .forEach {
                    customer.add(
                        Customer(
                            it[Customers.id],
                            it[Customers.firstName],
                            it[Customers.familyName],
                            it[Customers.firstNameKana],
                            it[Customers.familyNameKana],
                            it[Customers.zipcodeWard],
                            it[Customers.zipcodeTown],
                            it[Customers.prefecture],
                            it[Customers.address1],
                            it[Customers.address2],
                            it[Customers.address3],
                            it[Customers.phoneNumber],
                            it[Customers.mailAddress],
                            it[Customers.createDate],
                            it[Customers.updateDate],
                            it[Customers.createAccount],
                            it[Customers.updateAccount]
                        )
                    )
                }
        }
        return customer
    }
    suspend fun read(id: UUID): Customer? = dbQuery {
        Customers.select{ Customers.id eq id }
            .map {
                Customer(
                    it[Customers.id],
                    it[Customers.firstName],
                    it[Customers.familyName],
                    it[Customers.firstNameKana],
                    it[Customers.familyNameKana],
                    it[Customers.zipcodeWard],
                    it[Customers.zipcodeTown],
                    it[Customers.prefecture],
                    it[Customers.address1],
                    it[Customers.address2],
                    it[Customers.address3],
                    it[Customers.phoneNumber],
                    it[Customers.mailAddress],
                    it[Customers.createDate],
                    it[Customers.updateDate],
                    it[Customers.createAccount],
                    it[Customers.updateAccount]
                )
            }
            .singleOrNull()
    }

    suspend fun update(id: UUID, customer: Customer) {
        dbQuery {
            Customers.update({ Customers.id eq id }) {
                it[firstName] = customer.firstName
                it[familyName] = customer.familyName
                it[firstNameKana] = customer.firstNameKana
                it[familyNameKana] = customer.familyNameKana
                it[zipcodeWard] = customer.zipcodeWard
                it[zipcodeTown] = customer.zipcodeTown
                it[prefecture] = customer.prefecture
                it[address1] = customer.address1
                it[address2] = customer.address2
                it[address3] = customer.address3
                it[phoneNumber] = customer.phoneNumber
                it[mailAddress] = customer.mailAddress
                it[updateDate] = customer.updateDate
                it[updateAccount] = customer.updateAccount
            }
        }
    }

    suspend fun delete(id: UUID) {
        dbQuery {
            Customers.deleteWhere { Customers.id eq id }
        }
    }

}

data class Customer(
    val id: UUID?,
    val firstName: String,
    val familyName: String,
    val firstNameKana: String,
    val familyNameKana: String,
    val zipcodeWard: String,
    val zipcodeTown: String,
    val prefecture: String,
    val address1: String,
    val address2: String,
    val address3: String,
    val phoneNumber: String,
    val mailAddress: String,
    val createDate: String,
    val updateDate: String,
    val createAccount: UUID,
    val updateAccount: UUID
)
