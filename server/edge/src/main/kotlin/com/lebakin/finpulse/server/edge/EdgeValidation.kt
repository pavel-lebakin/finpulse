package com.lebakin.finpulse.server.edge

private const val MAX_HOST_LENGTH = 253
private const val MAX_LABEL_LENGTH = 63
private const val MAX_TOKEN_LENGTH = 1024
private const val MAX_ORIGIN_CA_LENGTH = 8192
private const val IPV4_LABELS = 4

internal fun validateTunnel(tunnel: EdgeTunnel) {
    require(validHostname(tunnel.hostname)) { "Edge hostname is invalid." }
    require(tunnel.proxied) { "Edge DNS is not proxied." }
    require(closedOrigin(tunnel)) { "Edge origin is not closed." }
    require(tunnel.tunnelToken.isNotBlank()) { "Edge tunnel token is empty." }
    require(tunnel.tunnelToken.length <= MAX_TOKEN_LENGTH) { "Edge tunnel token is too long." }
    require(tunnel.tunnelToken.none(::isSecretBreak)) { "Edge tunnel token is not plain text." }
}

internal fun validateShield(shield: EdgeShield) {
    require(shield.tls == TLS_FULL_STRICT) { "Edge TLS is not full strict." }
    require(shield.hsts) { "Edge HSTS is off." }
    require(shield.waf) { "Edge WAF is off." }
    require(shield.botProtection) { "Edge bot protection is off." }
    require(shield.rateLimit == RATE_LIMIT) { "Edge rate limit is invalid." }
    require(shield.originCa.isNotBlank()) { "Edge origin CA is empty." }
    require(shield.originCa.length <= MAX_ORIGIN_CA_LENGTH) { "Edge origin CA is too long." }
    require(shield.originCa.none { it == '\u0000' }) { "Edge origin CA is not plain text." }
}

private fun closedOrigin(tunnel: EdgeTunnel): Boolean =
    tunnel.originHost == ORIGIN_HOST && tunnel.originPort == ORIGIN_PORT

private fun isSecretBreak(char: Char): Boolean =
    char == '\u0000' || char == '\n' || char == '\r' || char == '"' || char == '\\'

private fun validHostname(host: String): Boolean {
    if (!validHost(host) || !host.contains('.')) {
        return false
    }
    val labels = host.split('.')
    val ipv4 = labels.size == IPV4_LABELS && labels.all { label -> label.all { it in '0'..'9' } }
    return !ipv4 && labels.all(::validLabel)
}

private fun validHost(host: String): Boolean {
    val lengthOk = host.length in 1..MAX_HOST_LENGTH
    val dotted = host.contains("..") || host.startsWith('.') || host.endsWith('.')
    val dashed = host.startsWith('-') || host.endsWith('-')
    return lengthOk && !dotted && !dashed && host.all(::isHostChar)
}

private fun validLabel(label: String): Boolean {
    val lengthOk = label.length in 1..MAX_LABEL_LENGTH
    val edges = label.startsWith('-') || label.endsWith('-')
    return lengthOk && !edges && label.all(::isHostChar)
}

private fun isHostChar(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '.' || char == '-'
