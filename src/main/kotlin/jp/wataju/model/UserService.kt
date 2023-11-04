package jp.wataju.model

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class UserService(
    databaseConfig: ApplicationConfig
): Service(Users, databaseConfig) {

    object Users : Table("users") {
        val id = uuid("id").autoGenerate()
        val accountId = uuid("account_id") references AccountService.Accounts.id
        val firstName = text("first_name")
        val familyName = text("family_name")
        val firstNameKana = text("first_name_kana")
        val familyNameKana = text("family_name_kana")
        val phoneNumber = text("phone_number")
        val mailAddress = text("mail_address")
        val createDate = varchar("create_date", 50)
        val updateDate = varchar("update_date", 50)
        val createAccount = uuid("create_account") references AccountService.Accounts.id
        val updateAccount = uuid("update_account") references AccountService.Accounts.id

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(user: User): UUID = dbQuery {
        Users
            .insert {
                it[accountId] = user.accountId
                it[firstName] = user.firstName
                it[familyName] = user.familyName
                it[firstNameKana] = user.firstNameKana
                it[familyNameKana] = user.familyNameKana
                it[phoneNumber] = user.phoneNumber
                it[mailAddress] = user.mailAddress
                it[createDate] = user.createDate
                it[updateDate] = user.updateDate
                it[createAccount] = user.createAccount
                it[updateAccount] = user.updateAccount
            }[Users.id]
    }

    suspend fun read(): MutableList<User?> {
        val users = mutableListOf<User?>()
        dbQuery {
            Users.selectAll().orderBy(Users.familyNameKana)
                .forEach {
                    users.add(
                        User(
                            it[Users.id],
                            it[Users.accountId],
                            it[Users.firstName],
                            it[Users.familyName],
                            it[Users.firstNameKana],
                            it[Users.familyNameKana],
                            it[Users.phoneNumber],
                            it[Users.mailAddress],
                            it[Users.createDate],
                            it[Users.updateDate],
                            it[Users.createAccount],
                            it[Users.updateAccount]
                        )
                    )
                }
        }
        return users
    }

    suspend fun read(id: UUID): User? = dbQuery {
        Users.select { Users.id eq id }
            .map {
                User(
                    it[Users.id],
                    it[Users.accountId],
                    it[Users.firstName],
                    it[Users.familyName],
                    it[Users.firstNameKana],
                    it[Users.familyNameKana],
                    it[Users.phoneNumber],
                    it[Users.mailAddress],
                    it[Users.createDate],
                    it[Users.updateDate],
                    it[Users.createAccount],
                    it[Users.updateAccount]
                )
            }
            .singleOrNull()
    }


    suspend fun update(id: UUID, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[firstName] = user.firstName
                it[familyName] = user.familyName
                it[firstNameKana] = user.firstNameKana
                it[familyNameKana] = user.familyNameKana
                it[phoneNumber] = user.phoneNumber
                it[mailAddress] = user.mailAddress
                it[updateDate] = user.updateDate
                it[updateAccount] = user.updateAccount
            }
        }
    }

    suspend fun delete(id: UUID) {
        dbQuery {
            Users.deleteWhere { Users.id eq id }
        }
    }

}

data class User(
    val id: UUID,
    val accountId: UUID,
    val firstName: String,
    val familyName: String,
    val firstNameKana: String,
    val familyNameKana: String,
    val phoneNumber: String,
    val mailAddress: String,
    val createDate: String,
    val updateDate: String,
    val createAccount: UUID,
    val updateAccount: UUID
)
