package jp.wataju.model

import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

open class Service(table: Table, databaseConfig: ApplicationConfig) {
    private val database = Database.connect(
        url = databaseConfig.property("url").getString(),
        driver = databaseConfig.property("driver").getString(),
        user = databaseConfig.property("user").getString(),
        password = databaseConfig.property("password").getString()
    )

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }

    init {
        transaction(database) {
            SchemaUtils.create(table)
        }
    }
}
