package com.lebakin.finpulse.core.network

import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest

class HttpsClientTest {
    @Test
    fun httpsPostReturnsTheReply() = runTest {
        val exchange = FakeExchange(HttpReply(statusCode = 201, body = "{\"ok\":true}"))
        val client = DefaultHttpsClient(exchange)

        val reply = client.post(
            url = "https://example.com/v1",
            body = "{}",
            headers = mapOf("Accept" to "application/json"),
        )

        assertEquals(1, exchange.calls)
        assertEquals(201, reply.statusCode)
        assertEquals("{\"ok\":true}", reply.body)
    }

    @Test
    fun cleartextIsRejectedBeforeTheCall() = runTest {
        val exchange = FakeExchange(HttpReply(statusCode = 200, body = "nope"))
        val client = DefaultHttpsClient(exchange)

        assertFailsWith<IllegalArgumentException> {
            client.post("http://example.com", "{}", emptyMap())
        }
        assertEquals(0, exchange.calls)
    }

    @Test
    fun userinfoIsRejected() = runTest {
        val exchange = FakeExchange(HttpReply(statusCode = 200, body = "nope"))
        val client = DefaultHttpsClient(exchange)

        assertFailsWith<IllegalArgumentException> {
            client.post("https://user:secret@example.com/path", "{}", emptyMap())
        }
        assertEquals(0, exchange.calls)
    }

    @Test
    fun transportFailureBecomesNoConnection() = runTest {
        val cause = IOException("offline")
        val client = DefaultHttpsClient(FakeExchange(failure = cause))

        val error = assertFailsWith<NoConnectionException> {
            client.post("https://example.com", "{}", emptyMap())
        }
        assertEquals(cause, error.cause)
    }

    @Test
    fun statusAboveSuccessBecomesServerStatus() = runTest {
        val client = DefaultHttpsClient(FakeExchange(HttpReply(statusCode = 503, body = "busy")))

        val error = assertFailsWith<ServerStatusException> {
            client.post("https://example.com", "{}", emptyMap())
        }
        assertEquals(503, error.statusCode)
        assertFalse(error.message.orEmpty().contains("busy"))
    }

    @Test
    fun cancellationIsRethrown() = runTest {
        val client = DefaultHttpsClient(FakeExchange(failure = CancellationException("stop")))

        assertFailsWith<CancellationException> {
            client.post("https://example.com", "{}", emptyMap())
        }
    }

    private class FakeExchange(
        private val reply: HttpReply = HttpReply(statusCode = 200, body = ""),
        private val failure: Throwable? = null,
    ) : HttpExchange {
        var calls: Int = 0

        override suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply {
            calls += 1
            failure?.let { throw it }
            return reply
        }
    }
}
