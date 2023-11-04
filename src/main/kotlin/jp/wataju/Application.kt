package jp.wataju

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.mustache.*
import io.ktor.server.netty.*
import io.ktor.server.sessions.*
import jp.wataju.session.AccountSession
import kotlin.collections.set

fun Application.module() {
    routing()

    install(Sessions) {
        cookie<AccountSession>("ACCOUNT_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
}

fun main(array: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(array)).start(wait = true)
}
