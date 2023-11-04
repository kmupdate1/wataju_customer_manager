package jp.wataju.model

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class AccountService(
    databaseConfig: ApplicationConfig
): Service(Accounts, databaseConfig) {

    object Accounts: Table("accounts") {
        val id = uuid("id")
        val identifier = text("identifier")
        val password = text("password")
        val admin = bool("admin")
        val createDate = varchar("create_date", 50)
        val updateDate = varchar("update_date", 50)
        val createAccount = uuid("create_account") references id
        val updateAccount = uuid("update_account") references id

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun create(account: Account): UUID = dbQuery {
        Accounts.insert {
            it[id] = account.id
            it[identifier] = account.identifier
            it[password] = account.password
            it[admin] = account.admin
            it[createDate] = account.createDate
            it[updateDate] = account.updateDate
            it[createAccount] = account.createAccount
            it[updateAccount] = account.updateAccount
        }[Accounts.id]
    }

    suspend fun read(): MutableList<Account?> {
        val accounts = mutableListOf<Account?>()
        dbQuery {
            Accounts.selectAll().orderBy(Accounts.identifier)
                .forEach {
                    accounts.add(
                        Account(
                            it[Accounts.id],
                            it[Accounts.identifier],
                            it[Accounts.password],
                            it[Accounts.admin],
                            it[Accounts.createDate],
                            it[Accounts.updateDate],
                            it[Accounts.createAccount],
                            it[Accounts.updateAccount]
                        )
                    )
                }
        }
        return accounts
    }

    suspend fun read(identifier: String): Account? = dbQuery {
        Accounts.select { Accounts.identifier eq identifier }
            .map {
                Account(
                    it[Accounts.id],
                    it[Accounts.identifier],
                    it[Accounts.password],
                    it[Accounts.admin],
                    it[Accounts.createDate],
                    it[Accounts.updateDate],
                    it[Accounts.createAccount],
                    it[Accounts.updateAccount]
                )
            }
            .singleOrNull()
    }

    suspend fun update(id: UUID, account: Account) {
        dbQuery {
            Accounts.update({ Accounts.id eq id }) {
                it[identifier] = account.identifier
                it[password] = account.password
                it[admin] = account.admin
                it[updateDate] = account.updateDate
                it[updateAccount] = account.updateAccount
            }
        }
    }

    suspend fun delete(id: UUID) {
        dbQuery {
            Accounts.deleteWhere { Accounts.id eq id }
        }
    }

}

data class Account(
    val id: UUID,
    val identifier: String,
    val password: String,
    val admin: Boolean,
    val createDate: String,
    val updateDate: String,
    val createAccount: UUID,
    val updateAccount: UUID
)
