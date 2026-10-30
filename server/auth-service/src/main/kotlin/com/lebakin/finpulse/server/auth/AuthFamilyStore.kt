package com.lebakin.finpulse.server.auth

import com.lebakin.finpulse.server.cache.Redis

private const val FIELD_COUNT = 6
private const val VERSION = 0
private const val FAMILY = 1
private const val ACCOUNT = 2
private const val REVOKED = 3
private const val CURRENT = 4
private const val SPENT = 5
private const val RECORD_VERSION = "1"
private const val KEY_PREFIX = "refresh:"
private const val NONE = "-"
private const val HASH_HEX = REFRESH_BYTES * 2
private const val NIBBLE = 4
private const val LOW_NIBBLE = 0x0F
private const val BYTE_MASK = 0xFF
private const val HEX = "0123456789abcdef"

private class RecordHeader(
    val accountId: String,
    val revoked: Boolean,
)

fun saveFamily(redis: Redis, family: RefreshFamily) {
    require(validFamily(family)) { "Auth refresh family is invalid." }
    redis.set("$KEY_PREFIX${family.id}", encode(family))
}

fun loadFamily(redis: Redis, id: String): RefreshFamily? {
    require(validId(id)) { "Auth refresh family is invalid." }
    val stored = redis.get("$KEY_PREFIX$id") ?: return null
    val family = parsed(id, stored)
    require(family != null) { "Auth refresh record is invalid." }
    return family
}

private fun encode(family: RefreshFamily): String {
    val revoked = if (family.revoked) "1" else "0"
    val spent = if (family.spent.isEmpty()) NONE else family.spent.joinToString(",") { hex(it) }
    val fields = listOf(RECORD_VERSION, family.id, family.accountId, revoked, hex(family.current), spent)
    return fields.joinToString(".")
}

private fun parsed(id: String, stored: String): RefreshFamily? {
    val parts = stored.split('.', limit = FIELD_COUNT)
    val header = recordHeader(id, parts)
    val current = parts.getOrNull(CURRENT)?.let(::unhex)
    val spent = parts.getOrNull(SPENT)?.let(::unhexList)
    val family = if (header != null && current != null && spent != null) {
        RefreshFamily(id, header.accountId, current, spent, header.revoked)
    } else {
        null
    }
    current?.fill(0)
    spent?.forEach { hash -> hash.fill(0) }
    return family
}

private fun recordHeader(id: String, parts: List<String>): RecordHeader? {
    val accountId = parts.getOrNull(ACCOUNT)
    val revoked = parts.getOrNull(REVOKED)?.let(::revokedFlag)
    val versionOk = parts.size == FIELD_COUNT && parts.getOrNull(VERSION) == RECORD_VERSION
    val idOk = parts.getOrNull(FAMILY) == id && accountId != null && validId(accountId)
    val header = if (versionOk && idOk && revoked != null) accountId else null
    return if (header != null && revoked != null) RecordHeader(header, revoked) else null
}

private fun revokedFlag(text: String): Boolean? = when (text) {
    "0" -> false
    "1" -> true
    else -> null
}

private fun unhexList(text: String): List<ByteArray>? = when (text) {
    NONE -> emptyList()
    else -> parseSpent(text)
}

private fun parseSpent(text: String): List<ByteArray>? {
    val parts = text.split(',')
    val hashes = ArrayList<ByteArray>(parts.size)
    var ok = parts.size <= REFRESH_HISTORY && parts.none { it == NONE }
    parts.forEach { part ->
        val hash = unhex(part)
        if (hash == null) {
            ok = false
        } else {
            hashes.add(hash)
        }
    }
    if (!ok) {
        hashes.forEach { hash -> hash.fill(0) }
    }
    return if (ok) hashes else null
}

private fun unhex(text: String): ByteArray? {
    val bytes = ByteArray(REFRESH_BYTES)
    var ok = text.length == HASH_HEX
    text.forEach { char ->
        if (hexValue(char) == null) {
            ok = false
        }
    }
    if (ok) {
        repeat(REFRESH_BYTES) { index ->
            val high = hexValue(text[index * 2]) ?: 0
            val low = hexValue(text[index * 2 + 1]) ?: 0
            bytes[index] = ((high shl NIBBLE) or low).toByte()
        }
    }
    return if (ok) bytes else null
}

private fun hex(bytes: ByteArray): String = buildString(HASH_HEX) {
    bytes.forEach { byte ->
        val value = byte.toInt() and BYTE_MASK
        append(HEX[value ushr NIBBLE])
        append(HEX[value and LOW_NIBBLE])
    }
}

private fun hexValue(char: Char): Int? {
    val digit = HEX.indexOf(char)
    return if (digit >= 0) digit else null
}
