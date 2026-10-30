package com.lebakin.finpulse.server.edge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class EdgeTest {
    @Test
    fun hostnameIsRejected() {
        assertTunnelRejected(tunnel(hostname = "127.0.0.1"), "Edge hostname is invalid.")
        assertTunnelRejected(tunnel(hostname = "localhost"), "Edge hostname is invalid.")
        assertTunnelRejected(tunnel(hostname = "api.finpulse.test/edge"), "Edge hostname is invalid.")
    }

    @Test
    fun dnsMustBeProxied() {
        assertTunnelRejected(tunnel(proxied = false), "Edge DNS is not proxied.")
    }

    @Test
    fun originMustBeLoopback() {
        assertTunnelRejected(tunnel(originHost = "0.0.0.0"), "Edge origin is not closed.")
        assertTunnelRejected(tunnel(originPort = OPEN_PORT), "Edge origin is not closed.")
    }

    @Test
    fun shieldIsRejected() {
        assertShieldRejected(shield(tls = "flexible"), "Edge TLS is not full strict.")
        assertShieldRejected(shield(hsts = false), "Edge HSTS is off.")
        assertShieldRejected(shield(waf = false), "Edge WAF is off.")
        assertShieldRejected(shield(botProtection = false), "Edge bot protection is off.")
        assertShieldRejected(shield(rateLimit = 0), "Edge rate limit is invalid.")
    }

    @Test
    fun secretsStayOutOfText() {
        val token = "tok" + "a".repeat(SECRET_MARK)
        val originCa = "-----BEGIN CERTIFICATE-----\nMIIB\n-----END CERTIFICATE-----"
        val tunnel = tunnel(tunnelToken = token)
        val shield = shield(originCa = originCa)
        assertFalse(tunnel.toString().contains(token))
        assertFalse(shield.toString().contains("BEGIN CERTIFICATE"))
        val edge = openEdge(tunnel, shield)
        assertFalse(edge.toString().contains(token))
        assertFalse(edge.toString().contains("BEGIN CERTIFICATE"))
    }

    @Test
    fun rejectedCallDoesNotEchoSecrets() {
        val token = "tok" + "b".repeat(SECRET_MARK)
        val originCa = "ca" + "c".repeat(SECRET_MARK)
        val error = assertFailsWith<IllegalArgumentException> {
            openEdge(tunnel(originHost = "0.0.0.0", tunnelToken = token), shield(originCa = originCa))
        }
        assertEquals("Edge origin is not closed.", error.message)
        val text = error.message.orEmpty()
        assertFalse(text.contains(token))
        assertFalse(text.contains(originCa))
    }

    @Test
    fun blankSecretsAreRejected() {
        assertTunnelRejected(tunnel(tunnelToken = " "), "Edge tunnel token is empty.")
        assertTunnelRejected(tunnel(tunnelToken = "tok\nsecret"), "Edge tunnel token is not plain text.")
        assertShieldRejected(shield(originCa = " "), "Edge origin CA is empty.")
        assertShieldRejected(shield(originCa = "ca\u0000"), "Edge origin CA is not plain text.")
    }

    @Test
    fun acceptedPolicySetsCacheAndLimit() {
        val edge = openEdge(tunnel(), shield())
        assertEquals(HOSTNAME, edge.hostname)
        assertEquals(RATE_LIMIT, edge.rateLimit)
        assertEquals(RATE_WINDOW_SECONDS, edge.rateWindowSeconds)
        assertEquals(PUBLIC_CACHE, edge.cacheControl(EdgeObject.PublicFile))
        assertEquals(PRIVATE_CACHE, edge.cacheControl(EdgeObject.Receipt))
    }

    private companion object {
        const val HOSTNAME = "api.finpulse.test"
        const val SECRET_MARK = 16
        const val OPEN_PORT = 443

        private fun assertTunnelRejected(tunnel: EdgeTunnel, message: String) {
            val error = assertFailsWith<IllegalArgumentException> { openEdge(tunnel, shield()) }
            assertEquals(message, error.message)
        }

        private fun assertShieldRejected(shield: EdgeShield, message: String) {
            val error = assertFailsWith<IllegalArgumentException> { openEdge(tunnel(), shield) }
            assertEquals(message, error.message)
        }

        private fun tunnel(
            hostname: String = HOSTNAME,
            proxied: Boolean = true,
            originHost: String = ORIGIN_HOST,
            originPort: Int = ORIGIN_PORT,
            tunnelToken: String = "tok" + "d".repeat(SECRET_MARK),
        ) = EdgeTunnel(
            hostname = hostname,
            proxied = proxied,
            originHost = originHost,
            originPort = originPort,
            tunnelToken = tunnelToken,
        )

        private fun shield(
            tls: String = TLS_FULL_STRICT,
            hsts: Boolean = true,
            waf: Boolean = true,
            rateLimit: Int = RATE_LIMIT,
            botProtection: Boolean = true,
            originCa: String = "ca" + "e".repeat(SECRET_MARK),
        ) = EdgeShield(
            tls = tls,
            hsts = hsts,
            waf = waf,
            rateLimit = rateLimit,
            botProtection = botProtection,
            originCa = originCa,
        )
    }
}
