package com.lebakin.finpulse.server.auth

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.charset
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

private const val AUTH_HOST = "127.0.0.1"
private const val AUTH_PORT = 8081
internal const val MIN_PASSWORD_LENGTH = 8
internal const val MAX_PASSWORD_LENGTH = 128
internal const val MIN_EMAIL_LENGTH = 5
internal const val MAX_EMAIL_LENGTH = 254
internal const val MAX_ID_LENGTH = 64
private const val MAX_BODY_LENGTH = 1024

sealed interface SignIn {
    data object Accepted : SignIn

    data object Rejected : SignIn
}

class PasswordVerifier internal constructor(
    salt: ByteArray,
    hash: ByteArray,
    val iterations: Int,
) {
    internal val salt: ByteArray = salt.copyOf()
    internal val hash: ByteArray = hash.copyOf()

    override fun toString(): String = "PasswordVerifier"
}

internal class ParsedSignIn(
    val email: String,
    val password: CharArray,
)

class AuthAccount(
    val id: String,
    val email: String,
    verifier: PasswordVerifier,
) {
    internal val verifier: PasswordVerifier = verifier

    override fun toString(): String = "AuthAccount(id=$id)"
}

fun main() {
    embeddedServer(
        factory = Netty,
        port = AUTH_PORT,
        host = AUTH_HOST,
        module = { auth(emptyList()) },
    ).start(wait = true)
}

fun Application.auth(accounts: List<AuthAccount>) {
    require(distinctEmails(accounts)) { "Auth accounts repeat an email." }
    accounts.forEach(::validateAccount)
    routing {
        post("/sessions") {
            call.answer(call.statusFor(accounts))
        }
    }
}

fun signIn(email: String, password: CharArray, account: AuthAccount): SignIn {
    try {
        validateAccount(account)
        val shapeOk = validEmail(email) && validPassword(password)
        val passwordOk = shapeOk && passwordMatches(password, account.verifier)
        val accepted = shapeOk && passwordOk && sameEmail(email, account.email)
        return if (accepted) SignIn.Accepted else SignIn.Rejected
    } finally {
        password.fill('\u0000')
    }
}

private suspend fun ApplicationCall.answer(status: HttpStatusCode) {
    if (status == HttpStatusCode.NoContent) {
        respond(status)
    } else {
        respondText(text = "", status = status)
    }
}

private suspend fun ApplicationCall.statusFor(accounts: List<AuthAccount>): HttpStatusCode {
    val request = readSignIn()
    val account = request?.let { parsed -> accounts.firstOrNull { sameEmail(it.email, parsed.email) } }
    val result = request?.let { parsed -> signIn(parsed.email, parsed.password, account ?: placeholderAccount) }
    val accepted = account != null && result == SignIn.Accepted
    return if (accepted) HttpStatusCode.NoContent else HttpStatusCode.Unauthorized
}

private suspend fun ApplicationCall.readSignIn(): ParsedSignIn? {
    val type = request.contentType()
    val charset = type.charset()
    val charsetOk = charset == null || charset == Charsets.UTF_8
    val length = request.headers["Content-Length"]?.toIntOrNull()
    val lengthOk = length == null || length in 0..MAX_BODY_LENGTH
    if (!type.match(ContentType.Application.Json) || !charsetOk || !lengthOk) {
        return null
    }
    val body = receiveText()
    return if (body.length <= MAX_BODY_LENGTH) parseSignIn(body) else null
}

private fun distinctEmails(accounts: List<AuthAccount>): Boolean =
    accounts.distinctBy { it.email.lowercase() }.size == accounts.size
