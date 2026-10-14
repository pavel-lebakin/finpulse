package com.lebakin.finpulse.core.network

import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import kotlin.coroutines.cancellation.CancellationException

data class HttpReply(
    val statusCode: Int,
    val body: String,
)

interface HttpsClient {
    suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply
}

internal interface HttpExchange {
    suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply
}

internal fun requireHttps(url: String) {
    require(url.isNotEmpty() && url.none { it.isWhitespace() }) { "Only HTTPS is allowed." }
    val schemeSplit = url.split("://", limit = 2)
    require(schemeSplit.size == 2 && schemeSplit[0].equals("https", ignoreCase = true)) {
        "Only HTTPS is allowed."
    }
    val afterScheme = schemeSplit[1]
    require(afterScheme.isNotEmpty() && !afterScheme.startsWith("/")) { "Only HTTPS is allowed." }
    val authority = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
    require(authority.isNotEmpty() && !authority.contains('@')) { "Only HTTPS is allowed." }
    val host = if (authority.startsWith("[")) {
        val end = authority.indexOf(']')
        require(end > 1) { "Only HTTPS is allowed." }
        authority.substring(1, end)
    } else {
        authority.substringBefore(':')
    }
    require(host.isNotEmpty()) { "Only HTTPS is allowed." }
}

private const val SUCCESS_FROM = 200
private const val SUCCESS_TO = 299

internal class DefaultHttpsClient(
    private val exchange: HttpExchange,
) : HttpsClient {
    override suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply {
        requireHttps(url)
        val reply = read(url, body, headers)
        if (reply.statusCode !in SUCCESS_FROM..SUCCESS_TO) {
            throw ServerStatusException(reply.statusCode)
        }
        return reply
    }

    // Transport failures become NoConnectionException. CancellationException is rethrown above.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun read(url: String, body: String, headers: Map<String, String>): HttpReply = try {
        exchange.post(url, body, headers)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        throw NoConnectionException(error)
    }
}

expect fun openHttpsClient(): HttpsClient
