package com.lebakin.finpulse.core.ai

import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import com.lebakin.finpulse.core.network.HttpReply
import com.lebakin.finpulse.core.network.HttpsClient
import com.lebakin.finpulse.core.security.SecretStore
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class ByokModelTest {
    @Test
    fun aMissingKeyDoesNotCallGemini() = runTest {
        val client = RecordingClient()

        assertNull(openByokModel(MemoryStore(), client))
        assertNull(openByokModel(MemoryStore("not a key"), client))
        assertNull(openByokModel(MemoryStore("line\nbreak"), client))
        assertEquals(0, client.calls)
    }

    @Test
    fun aSavedKeyPostsToGeminiAndReturnsTheAnswer() = runTest {
        val key = "test-gemini-key"
        val client = RecordingClient(
            HttpReply(
                statusCode = 200,
                body = """
                    {"candidates":[{"content":{"parts":[
                    {"thought":true,"text":"reasoning"},
                    {"text":"answer"}
                    ]}}]}
                """.trimIndent(),
            ),
        )
        val model = openByokModel(MemoryStore(key), client)

        val text = model?.complete("say \"hi\"")

        assertEquals("answer", text)
        assertEquals(1, client.calls)
        assertEquals(GEMINI_GENERATE_URL, client.url)
        assertTrue(client.url.orEmpty().startsWith("https://generativelanguage.googleapis.com/"))
        assertTrue(client.url.orEmpty().contains("gemini-3.8-flash"))
        assertTrue(client.url.orEmpty().endsWith(":generateContent"))
        assertFalse(client.url.orEmpty().contains('?'))
        assertFalse(client.url.orEmpty().contains(key))
        assertEquals(key, client.headers[BYOK_API_KEY_HEADER])
        assertEquals("application/json", client.headers["Content-Type"])
        assertEquals(setOf("Content-Type", BYOK_API_KEY_HEADER), client.headers.keys)
        assertFalse(client.body.orEmpty().contains(key))
        assertTrue(client.body.orEmpty().contains("say \\\"hi\\\""))
    }

    @Test
    fun aBlankPromptDoesNotCallGemini() = runTest {
        val client = RecordingClient()
        val model = openByokModel(MemoryStore("test-gemini-key"), client)

        assertFailsWith<IllegalArgumentException> {
            model?.complete("  ")
        }
        assertEquals(0, client.calls)
    }

    @Test
    fun aDeletedKeyDoesNotCallGemini() = runTest {
        val store = MemoryStore("test-gemini-key")
        val client = RecordingClient()
        val model = openByokModel(store, client)
        store.delete()

        val error = assertFailsWith<ByokFailed> {
            model?.complete("receipt")
        }
        assertEquals("BYOK model failed", error.message)
        assertNull(error.cause)
        assertEquals(0, client.calls)
    }

    @Test
    fun anUnreadableReplyIsNotATransaction() = runTest {
        val key = "test-gemini-key"
        val client = RecordingClient(HttpReply(statusCode = 200, body = "not-json $key"))
        val model = openByokModel(MemoryStore(key), client)

        val error = assertFailsWith<ByokFailed> {
            model?.complete("receipt")
        }
        assertEquals("BYOK model failed", error.message)
        assertFalse(error.message.orEmpty().contains(key))
        assertNull(error.cause)
    }

    @Test
    fun transportAndCancellationStayTyped() = runTest {
        val store = MemoryStore("test-gemini-key")
        val offline = RecordingClient(failure = NoConnectionException(IOException("offline")))
        val denied = RecordingClient(failure = ServerStatusException(401))
        val stopped = RecordingClient(failure = CancellationException("stop"))

        assertFailsWith<NoConnectionException> {
            openByokModel(store, offline)?.complete("receipt")
        }
        val server = assertFailsWith<ServerStatusException> {
            openByokModel(store, denied)?.complete("receipt")
        }
        assertEquals(401, server.statusCode)
        assertFalse(server.message.orEmpty().contains("test-gemini-key"))
        assertFailsWith<CancellationException> {
            openByokModel(store, stopped)?.complete("receipt")
        }
    }
}

private class RecordingClient(
    private val reply: HttpReply = HttpReply(statusCode = 200, body = ""),
    private val failure: Throwable? = null,
) : HttpsClient {
    var calls: Int = 0
    var url: String? = null
    var body: String? = null
    var headers: Map<String, String> = emptyMap()

    override suspend fun post(url: String, body: String, headers: Map<String, String>): HttpReply {
        calls += 1
        this.url = url
        this.body = body
        this.headers = headers
        failure?.let { throw it }
        return reply
    }
}

private class MemoryStore(initial: String? = null) : SecretStore {
    private var value: String? = initial

    override fun observeSaved(): Flow<Boolean> = MutableStateFlow(value != null)

    override suspend fun save(secret: String) {
        value = secret
    }

    override suspend fun read(): String? = value

    override suspend fun delete() {
        value = null
    }
}
