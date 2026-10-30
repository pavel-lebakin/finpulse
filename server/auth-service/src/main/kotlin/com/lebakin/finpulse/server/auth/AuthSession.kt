package com.lebakin.finpulse.server.auth

import java.security.SecureRandom

internal const val SESSION_ID_BYTES = 16

class Session internal constructor(
    val family: RefreshFamily,
    token: ByteArray,
) {
    val token: ByteArray = token.copyOf()

    override fun toString(): String = "Session"
}

fun issueSession(accountId: String): Session {
    require(validId(accountId)) { "Auth session is invalid." }
    val token = refreshToken()
    try {
        return Session(refreshFamily(sessionId(), accountId, token), token)
    } finally {
        token.fill(0)
    }
}

internal fun sessionBody(session: Session): String {
    val text = session.family.id + "." + hex(session.token)
    session.token.fill(0)
    return text
}

private fun sessionId(): String {
    val bytes = ByteArray(SESSION_ID_BYTES)
    SecureRandom().nextBytes(bytes)
    val id = hex(bytes)
    bytes.fill(0)
    return id
}
