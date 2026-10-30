package com.lebakin.finpulse.server.auth

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AuthTest {
    @Test
    fun matchingPasswordIsAcceptedAndCleared() {
        val verifier = passwordVerifier("password12".toCharArray())
        val account = AuthAccount("user-1", "Ada@finpulse.test", verifier)
        val password = "password12".toCharArray()
        val result = signIn("ada@finpulse.test", password, account)
        assertEquals(SignIn.Accepted, result)
        assertEquals(true, password.all { it == '\u0000' })
    }

    @Test
    fun otherPasswordIsRejected() {
        val account = account()
        val result = signIn("ada@finpulse.test", "password13".toCharArray(), account)
        assertEquals(SignIn.Rejected, result)
    }

    @Test
    fun otherEmailIsRejected() {
        val account = account()
        val result = signIn("other@finpulse.test", "password12".toCharArray(), account)
        assertEquals(SignIn.Rejected, result)
    }

    @Test
    fun shortPasswordIsRejectedAndCleared() {
        val account = account()
        val password = "short".toCharArray()
        val result = signIn("ada@finpulse.test", password, account)
        assertEquals(SignIn.Rejected, result)
        assertEquals(true, password.all { it == '\u0000' })
    }

    @Test
    fun invalidPasswordIsAFixedMessage() {
        val password = "short".toCharArray()
        val error = assertFailsWith<IllegalArgumentException> { passwordVerifier(password) }
        assertEquals("Auth password is invalid.", error.message)
        assertEquals(true, password.all { it == '\u0000' })
        assertFalse(error.message.orEmpty().contains("short"))
    }

    @Test
    fun invalidVerifierIsAFixedMessage() {
        val verifier = PasswordVerifier(ByteArray(SALT_BYTES), ByteArray(HASH_BYTES), 1)
        val account = AuthAccount("user-1", "ada@finpulse.test", verifier)
        val password = "password12".toCharArray()
        val error = assertFailsWith<IllegalArgumentException> { signIn("ada@finpulse.test", password, account) }
        assertEquals("Auth verifier is invalid.", error.message)
        assertEquals(true, password.all { it == '\u0000' })
    }

    @Test
    fun textOmitsTheSecret() {
        val verifier = PasswordVerifier(ByteArray(SALT_BYTES) { 7 }, ByteArray(HASH_BYTES) { 9 }, PASSWORD_ITERATIONS)
        val account = AuthAccount("user-1", "ada@finpulse.test", verifier)
        assertEquals("PasswordVerifier", verifier.toString())
        assertEquals("AuthAccount(id=user-1)", account.toString())
        assertFalse(account.toString().contains("@"))
        assertFalse(verifier.toString().contains("7"))
    }

    @Test
    fun jsonObjectCarriesBothFields() {
        val parsed = parseSignIn(""" { "password" : "password12", "email" : "ada@finpulse.test" } """)
        assertEquals("ada@finpulse.test", parsed?.email)
        assertEquals("password12", parsed?.password?.concatToString())
        assertNull(parseSignIn("""{"email":"ada@finpulse.test","password":"bad\word"}"""))
    }

    @Test
    fun matchingPostWithoutStoreAnswersUnauthorized() = testApplication {
        application { auth(listOf(account())) }
        val response = client.post("/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"ada@finpulse.test","password":"password12"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
        assertEquals("private, no-store", response.headers[HttpHeaders.CacheControl])
    }

    @Test
    fun wrongPasswordAnswersUnauthorized() = testApplication {
        application { auth(listOf(account())) }
        val response = client.post("/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"ada@finpulse.test","password":"password13"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun unknownEmailAnswersUnauthorized() = testApplication {
        application { auth(listOf(account())) }
        val response = client.post("/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"other@finpulse.test","password":"password12"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun malformedBodyAnswersUnauthorized() = testApplication {
        application { auth(listOf(account())) }
        val response = client.post("/sessions") {
            contentType(ContentType.Text.Plain)
            setBody("""{"email":"ada@finpulse.test","password":"password12"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun otherPathsAreNotFound() = testApplication {
        application { auth(emptyList()) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun emptyAccountListAnswersUnauthorized() = testApplication {
        application { auth(emptyList()) }
        val response = client.post("/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"ada@finpulse.test","password":"password12"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun repeatedEmailIsRejected() {
        val verifier = passwordVerifier("password12".toCharArray())
        val accounts = listOf(
            AuthAccount("user-1", "ada@finpulse.test", verifier),
            AuthAccount("user-2", "Ada@finpulse.test", verifier),
        )
        val error = assertFailsWith<IllegalArgumentException> {
            testApplication {
                application { auth(accounts) }
            }
        }
        assertEquals("Auth accounts repeat an email.", error.message)
    }

    private fun account(): AuthAccount {
        val verifier = passwordVerifier("password12".toCharArray())
        return AuthAccount("user-1", "ada@finpulse.test", verifier)
    }
}
