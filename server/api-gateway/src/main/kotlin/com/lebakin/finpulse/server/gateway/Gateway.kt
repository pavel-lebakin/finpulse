package com.lebakin.finpulse.server.gateway

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

private const val GATEWAY_HOST = "127.0.0.1"
private const val GATEWAY_PORT = 8080

fun main() {
    embeddedServer(
        factory = Netty,
        port = GATEWAY_PORT,
        host = GATEWAY_HOST,
        module = Application::gateway,
    ).start(wait = true)
}

fun Application.gateway() {
    routing {
        get("/health") {
            call.respondText("ok")
        }
    }
}
