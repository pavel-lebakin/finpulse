package com.lebakin.finpulse.server.edge

internal const val ORIGIN_HOST = "127.0.0.1"
internal const val ORIGIN_PORT = 8080
internal const val TLS_FULL_STRICT = "full-strict"
internal const val RATE_LIMIT = 100
internal const val RATE_WINDOW_SECONDS = 60
internal const val PUBLIC_CACHE = "public, max-age=86400"
internal const val PRIVATE_CACHE = "private, no-store"

enum class EdgeObject {
    PublicFile,
    Receipt,
}

class EdgeTunnel(
    val hostname: String,
    val proxied: Boolean,
    val originHost: String,
    val originPort: Int,
    tunnelToken: String,
) {
    internal val tunnelToken: String = tunnelToken

    override fun toString(): String =
        "EdgeTunnel(hostname=$hostname, proxied=$proxied, originHost=$originHost, originPort=$originPort)"
}

class EdgeShield(
    val tls: String,
    val hsts: Boolean,
    val waf: Boolean,
    val rateLimit: Int,
    val botProtection: Boolean,
    originCa: String,
) {
    internal val originCa: String = originCa

    override fun toString(): String =
        "EdgeShield(tls=$tls, hsts=$hsts, waf=$waf, rateLimit=$rateLimit, botProtection=$botProtection)"
}

class Edge internal constructor(
    val hostname: String,
    val rateLimit: Int,
    val rateWindowSeconds: Int,
) {
    fun cacheControl(kind: EdgeObject): String =
        when (kind) {
            EdgeObject.PublicFile -> PUBLIC_CACHE
            EdgeObject.Receipt -> PRIVATE_CACHE
        }

    override fun toString(): String = "Edge(hostname=$hostname, rateLimit=$rateLimit)"
}

fun openEdge(tunnel: EdgeTunnel, shield: EdgeShield): Edge {
    validateTunnel(tunnel)
    validateShield(shield)
    return Edge(
        hostname = tunnel.hostname,
        rateLimit = shield.rateLimit,
        rateWindowSeconds = RATE_WINDOW_SECONDS,
    )
}
