package com.lebakin.finpulse.server.auth

import java.security.MessageDigest
import java.security.SecureRandom

internal const val REFRESH_BYTES = 32
internal const val REFRESH_HISTORY = 8

sealed interface Refresh {
    class Rotated internal constructor(
        val family: RefreshFamily,
        token: ByteArray,
    ) : Refresh {
        val token: ByteArray = token.copyOf()

        override fun toString(): String = "Refresh.Rotated"
    }

    class Reused internal constructor(
        val family: RefreshFamily,
    ) : Refresh {
        override fun toString(): String = "Refresh.Reused"
    }

    data object Rejected : Refresh
}

class RefreshFamily internal constructor(
    val id: String,
    val accountId: String,
    current: ByteArray,
    spent: List<ByteArray>,
    val revoked: Boolean,
) {
    internal val current: ByteArray = current.copyOf()
    internal val spent: List<ByteArray> = spent.map { it.copyOf() }

    override fun toString(): String = "RefreshFamily(id=$id, accountId=$accountId, revoked=$revoked)"
}

fun refreshToken(): ByteArray {
    val token = ByteArray(REFRESH_BYTES)
    SecureRandom().nextBytes(token)
    return token
}

fun refreshFamily(id: String, accountId: String, token: ByteArray): RefreshFamily {
    require(validId(id) && validId(accountId)) { "Auth refresh family is invalid." }
    require(token.size == REFRESH_BYTES) { "Auth refresh token is invalid." }
    val hash = sha256(token)
    val family = RefreshFamily(id, accountId, hash, emptyList(), false)
    hash.fill(0)
    return family
}

fun rotate(token: ByteArray, family: RefreshFamily): Refresh {
    try {
        require(validFamily(family)) { "Auth refresh family is invalid." }
        require(token.size == REFRESH_BYTES) { "Auth refresh token is invalid." }
        return decision(token, family)
    } finally {
        token.fill(0)
    }
}

private fun decision(token: ByteArray, family: RefreshFamily): Refresh {
    val hash = sha256(token)
    try {
        val currentMatch = MessageDigest.isEqual(hash, family.current)
        val spentMatch = matches(hash, family.spent)
        return when {
            family.revoked -> Refresh.Rejected
            currentMatch -> rotated(family)
            spentMatch -> reused(family)
            else -> Refresh.Rejected
        }
    } finally {
        hash.fill(0)
    }
}

private fun rotated(family: RefreshFamily): Refresh.Rotated {
    val token = refreshToken()
    val hash = sha256(token)
    val next = RefreshFamily(family.id, family.accountId, hash, remembered(family), false)
    hash.fill(0)
    val result = Refresh.Rotated(next, token)
    token.fill(0)
    return result
}

private fun reused(family: RefreshFamily): Refresh.Reused =
    Refresh.Reused(RefreshFamily(family.id, family.accountId, family.current, family.spent, true))

private fun remembered(family: RefreshFamily): List<ByteArray> = buildList {
    add(family.current.copyOf())
    for (older in family.spent) {
        if (size == REFRESH_HISTORY) {
            break
        }
        add(older.copyOf())
    }
}

private fun matches(hash: ByteArray, spent: List<ByteArray>): Boolean =
    spent.fold(false) { found, candidate -> MessageDigest.isEqual(hash, candidate) || found }

internal fun validFamily(family: RefreshFamily): Boolean {
    val idsOk = validId(family.id) && validId(family.accountId)
    val currentOk = family.current.size == REFRESH_BYTES
    val spentOk = family.spent.size <= REFRESH_HISTORY && family.spent.all { it.size == REFRESH_BYTES }
    return idsOk && currentOk && spentOk
}

private fun sha256(token: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(token)
