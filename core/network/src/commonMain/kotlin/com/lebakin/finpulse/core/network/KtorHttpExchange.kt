package com.lebakin.finpulse.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText

private const val CONNECT_TIMEOUT_MILLIS = 15_000L
private const val REQUEST_TIMEOUT_MILLIS = 30_000L

internal fun httpsEngineClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    followRedirects = false
    expectSuccess = false
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}

internal class KtorHttpExchange(
    private val client: HttpClient,
) : HttpExchange {
    override suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply =
        client.preparePost(url) {
            headers.forEach { (name, value) ->
                header(name, value)
            }
            setBody(body)
        }.execute { response ->
            HttpReply(
                statusCode = response.status.value,
                body = response.bodyAsText(),
            )
        }
}
