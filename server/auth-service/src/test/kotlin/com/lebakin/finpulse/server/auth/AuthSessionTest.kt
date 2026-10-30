package com.lebakin.finpulse.server.auth

import com.lebakin.finpulse.server.cache.openRedis
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthSessionTest {
    @Test
    fun textOmitsTheToken() {
        val session = issueSession("user-1")
        assertEquals("Session", session.toString())
        assertFalse(session.toString().contains("user-1"))
        assertEquals(SESSION_ID_BYTES * 2, session.family.id.length)
        assertEquals(REFRESH_BYTES, session.token.size)
        assertFalse(session.token.contentEquals(session.family.current))
        session.token.fill(0)
    }

    @Test
    fun invalidAccountIsAFixedMessage() {
        val error = assertFailsWith<IllegalArgumentException> { issueSession("bad id") }
        assertEquals("Auth session is invalid.", error.message)
        assertFalse(error.message.orEmpty().contains("bad"))
    }

    @Test
    fun anotherCallUsesAnotherFamily() {
        val first = issueSession("user-1")
        val second = issueSession("user-1")
        assertFalse(first.family.id == second.family.id)
        assertFalse(first.token.contentEquals(second.token))
        first.token.fill(0)
        second.token.fill(0)
    }

    @Test
    fun sessionBodyIsTheIdAndTokenAndClearsTheBytes() {
        val session = issueSession("user-1")
        val id = session.family.id
        val tokenHex = hex(session.token)
        val text = sessionBody(session)
        assertEquals("$id.$tokenHex", text)
        assertEquals(true, session.token.all { it == 0.toByte() })
        assertFalse(text.contains("user-1"))
        assertFalse(text.contains("@"))
    }

    @Test
    fun matchingPostIssuesOneSession() {
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                testApplication {
                    application { auth(listOf(account()), redis) }
                    val rejected = client.post("/sessions") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"email":"ada@finpulse.test","password":"password13"}""")
                    }
                    assertEquals(HttpStatusCode.Unauthorized, rejected.status)
                    assertEquals("", rejected.bodyAsText())
                    assertEquals("private, no-store", rejected.headers[HttpHeaders.CacheControl])
                    val response = client.post("/sessions") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"email":"ada@finpulse.test","password":"password12"}""")
                    }
                    assertEquals(HttpStatusCode.OK, response.status)
                    assertEquals("private, no-store", response.headers[HttpHeaders.CacheControl])
                    val issued = requireNotNull(readIssued(response.bodyAsText()))
                    val stored = redis.get("refresh:${issued.id}").orEmpty()
                    assertTrue(stored.startsWith("1.${issued.id}.user-1.0."))
                    assertTrue(stored.endsWith(".-"))
                    assertFalse(stored.contains(issued.tokenHex))
                    assertFalse(issued.text.contains("user-1"))
                    assertFalse(issued.text.contains("password12"))
                    val loaded = requireNotNull(loadFamily(redis, issued.id))
                    assertEquals("user-1", loaded.accountId)
                    assertEquals(false, loaded.revoked)
                    val hash = MessageDigest.getInstance("SHA-256").digest(issued.token)
                    assertTrue(MessageDigest.isEqual(hash, loaded.current))
                    hash.fill(0)
                    assertIs<Refresh.Rotated>(rotate(issued.token, loaded))
                }
            }
        }
    }

    private fun account(): AuthAccount {
        val verifier = passwordVerifier("password12".toCharArray())
        return AuthAccount("user-1", "ada@finpulse.test", verifier)
    }
}

private class Issued(
    val id: String,
    val token: ByteArray,
    val tokenHex: String,
    val text: String,
)

private fun readIssued(body: String): Issued? {
    val dot = body.indexOf('.')
    val oneDot = dot > 0 && dot == body.lastIndexOf('.')
    val id = if (oneDot) body.substring(0, dot) else ""
    val tokenHex = if (oneDot) body.substring(dot + 1) else ""
    val token = decodeHex(tokenHex)
    val idOk = id.length == SESSION_ID_BYTES * 2 && id.all { hexValue(it) != null }
    val ready = oneDot && idOk && token != null
    return if (ready && token != null && tokenHex == hex(token)) Issued(id, token, tokenHex, body) else null
}

private fun decodeHex(text: String): ByteArray? {
    val bytes = ByteArray(REFRESH_BYTES)
    var ok = text.length == REFRESH_BYTES * 2
    if (ok) {
        repeat(REFRESH_BYTES) { index ->
            val high = hexValue(text[index * 2])
            val low = hexValue(text[index * 2 + 1])
            if (high == null || low == null) {
                ok = false
            } else {
                bytes[index] = ((high shl NIBBLE) or low).toByte()
            }
        }
    }
    return if (ok) bytes else null
}

private fun hexValue(char: Char): Int? {
    val digit = HEX.indexOf(char)
    return if (digit >= 0) digit else null
}

private const val NIBBLE = 4
private const val HEX = "0123456789abcdef"
