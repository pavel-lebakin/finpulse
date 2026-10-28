package com.lebakin.finpulse.server.queue

private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val MAX_HOST_LENGTH = 253
private const val MAX_NAME_LENGTH = 63
private const val MAX_PASSWORD_LENGTH = 1024
private const val MAX_TOPIC_LENGTH = 249
private const val MAX_VALUE_LENGTH = 65_536

internal fun validate(endpoint: KafkaEndpoint) {
    require(validHost(endpoint.host)) { "Kafka host is invalid." }
    require(endpoint.port in MIN_PORT..MAX_PORT) { "Kafka port is invalid." }
    require(validName(endpoint.user)) { "Kafka user is invalid." }
    require(endpoint.password.isNotBlank()) { "Kafka password is empty." }
    require(endpoint.password.length <= MAX_PASSWORD_LENGTH) { "Kafka password is too long." }
    require(endpoint.password.none(::isPasswordBreak)) { "Kafka password is not plain text." }
}

internal fun validateTopic(topic: String) {
    val lengthOk = topic.length in 1..MAX_TOPIC_LENGTH
    val charsOk = topic.all(::isTopicChar)
    val reserved = topic == "." || topic == ".." || topic.startsWith("__")
    require(lengthOk && charsOk && !reserved) { "Kafka topic is invalid." }
}

internal fun validateValue(value: String) {
    require(value.length <= MAX_VALUE_LENGTH) { "Kafka value is too long." }
}

private fun isPasswordBreak(char: Char): Boolean =
    char == '\u0000' || char == '\n' || char == '\r' || char == '"' || char == '\\'

private fun isTopicChar(char: Char): Boolean {
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
