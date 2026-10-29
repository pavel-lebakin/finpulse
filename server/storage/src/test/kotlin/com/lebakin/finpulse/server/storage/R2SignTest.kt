package com.lebakin.finpulse.server.storage

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class R2SignTest {
    @Test
    fun putSignatureMatchesFixture() {
        val instant = Instant.parse("2026-10-30T12:00:00Z")
        val request = putRequest(fixture(), "marker", ByteArray(0), instant)
        val authorization = request.headers.first { it.name == "Authorization" }.value
        val cache = request.headers.first { it.name == "cache-control" }.value
        assertEquals(CACHE_CONTROL, cache)
        assertTrue(authorization.endsWith("Signature=$PUT_SIGNATURE"))
        assertFalse(authorization.contains(FIXTURE_PASSWORD))
    }

    @Test
    fun signMatchesFixture() {
        val instant = Instant.parse("2026-10-30T12:00:00Z")
        val url = presignGet(fixture(), "marker", instant)
        assertTrue(url.startsWith("http://127.0.0.1:9000/finpulse/marker?"))
        assertTrue(url.contains("X-Amz-Expires=$SIGN_SECONDS"))
        assertTrue(url.contains("X-Amz-Credential=finpulse%2F20261030%2Fus-east-1%2Fs3%2Faws4_request"))
        assertTrue(url.endsWith("X-Amz-Signature=$GET_SIGNATURE"))
        assertFalse(url.contains(FIXTURE_PASSWORD))
    }

    private fun fixture() = R2Endpoint(
        host = "127.0.0.1",
        port = FIXTURE_PORT,
        region = "us-east-1",
        bucket = "finpulse",
        accessKey = "finpulse",
        password = FIXTURE_PASSWORD,
    )

    private companion object {
        const val FIXTURE_PORT = 9000
        const val FIXTURE_PASSWORD = "secret"
        const val PUT_SIGNATURE = "f4b00d2cfde565d8e8b9ad51b5dabef3d71b03d3a3db31423e3b7147d5016ca1"
        const val GET_SIGNATURE = "fb9fcffae11aa2a510e5d947b88fbc15983a98de99bac65ae12762fd7dc4fe23"
    }
}
