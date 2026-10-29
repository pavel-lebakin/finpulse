package com.lebakin.finpulse.server.storage

private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val MAX_HOST_LENGTH = 253
private const val MAX_NAME_LENGTH = 128
private const val MAX_PASSWORD_LENGTH = 1024
private const val MAX_REGION_LENGTH = 32
private const val MIN_BUCKET_LENGTH = 3
private const val MAX_BUCKET_LENGTH = 63
private const val MAX_KEY_LENGTH = 256
internal const val MAX_OBJECT_BYTES = 65_536

internal fun validate(endpoint: R2Endpoint) {
    require(validHost(endpoint.host)) { "R2 host is invalid." }
    require(endpoint.port in MIN_PORT..MAX_PORT) { "R2 port is invalid." }
    require(validRegion(endpoint.region)) { "R2 region is invalid." }
    require(validBucket(endpoint.bucket)) { "R2 bucket is invalid." }
    require(validName(endpoint.accessKey)) { "R2 access key is invalid." }
    require(endpoint.password.isNotBlank()) { "R2 password is empty." }
    require(endpoint.password.length <= MAX_PASSWORD_LENGTH) { "R2 password is too long." }
    require(endpoint.password.none(::isPasswordBreak)) { "R2 password is not plain text." }
}

internal fun validateKey(key: String) {
    val lengthOk = key.length in 1..MAX_KEY_LENGTH
    val charsOk = key.all(::isKeyChar)
    val reserved = key == "." || key == ".."
    require(lengthOk && charsOk && !reserved) { "R2 key is invalid." }
}

internal fun validateBytes(bytes: ByteArray) {
    require(bytes.size <= MAX_OBJECT_BYTES) { "R2 object is too long." }
}

private fun isPasswordBreak(char: Char): Boolean =
    char == '\u0000' || char == '\n' || char == '\r' || char == '"' || char == '\\'

private fun isKeyChar(char: Char): Boolean {
    val letter = char in 'A'..'Z' || char in 'a'..'z'
    val digit = char in '0'..'9'
    return letter || digit || char == '.' || char == '_' || char == '-'
}

private fun validHost(host: String): Boolean {
    val lengthOk = host.length in 1..MAX_HOST_LENGTH
    val dotted = host.contains("..") || host.startsWith('.') || host.endsWith('.')
    val dashed = host.startsWith('-') || host.endsWith('-')
    return lengthOk && !dotted && !dashed && host.all(::isHostChar)
}

private fun isHostChar(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '.' || char == '-'

private fun validName(value: String): Boolean {
    val lengthOk = value.length in 1..MAX_NAME_LENGTH
    val firstOk = value.firstOrNull()?.let { it in 'A'..'Z' || it in 'a'..'z' || it == '_' } ?: false
    val charsOk = value.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '_' }
    return lengthOk && firstOk && charsOk
}

private fun validRegion(region: String): Boolean {
    val lengthOk = region.length in 1..MAX_REGION_LENGTH
    val edges = region.startsWith('-') || region.endsWith('-')
    val charsOk = region.all { it in 'a'..'z' || it in '0'..'9' || it == '-' }
    return lengthOk && !edges && charsOk
}

private fun validBucket(bucket: String): Boolean {
    val lengthOk = bucket.length in MIN_BUCKET_LENGTH..MAX_BUCKET_LENGTH
    val charsOk = bucket.all { it in 'a'..'z' || it in '0'..'9' || it == '-' }
    val start = bucket.firstOrNull()?.let { it in 'a'..'z' || it in '0'..'9' } ?: false
    val end = bucket.lastOrNull()?.let { it in 'a'..'z' || it in '0'..'9' } ?: false
    return lengthOk && charsOk && start && end && !bucket.contains("..")
}
