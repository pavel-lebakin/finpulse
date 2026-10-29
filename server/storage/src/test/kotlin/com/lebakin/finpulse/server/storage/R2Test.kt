package com.lebakin.finpulse.server.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class R2Test {
    @Test
    fun hostIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(host = "127.0.0.1/r2"))
        }
        assertEquals("R2 host is invalid.", error.message)
    }

    @Test
    fun regionIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(region = "US-EAST-1"))
        }
        assertEquals("R2 region is invalid.", error.message)
    }

    @Test
    fun bucketIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(bucket = "Finpulse"))
        }
        assertEquals("R2 bucket is invalid.", error.message)
    }

    @Test
    fun accessKeyIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(accessKey = "finpulse;drop"))
        }
        assertEquals("R2 access key is invalid.", error.message)
    }

    @Test
    fun portIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(port = 0))
        }
        assertEquals("R2 port is invalid.", error.message)
    }

    @Test
    fun blankPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(password = " "))
        }
        assertEquals("R2 password is empty.", error.message)
    }

    @Test
    fun quotedPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openR2(endpoint(password = "pw\"secret"))
        }
        assertEquals("R2 password is not plain text.", error.message)
    }

    @Test
    fun passwordStaysOutOfToString() {
        val password = "pw" + "a".repeat(PASSWORD_MARK)
        val endpoint = endpoint(password = password)
        assertFalse(endpoint.toString().contains(password))
        assertTrue(endpoint.toString().contains("accessKey=finpulse"))
    }

    private fun endpoint(
        host: String = "127.0.0.1",
        port: Int = R2_PORT,
        region: String = "us-east-1",
        bucket: String = "finpulse",
        accessKey: String = "finpulse",
        password: String = "pw" + "b".repeat(PASSWORD_MARK),
    ) = R2Endpoint(
        host = host,
        port = port,
        region = region,
        bucket = bucket,
        accessKey = accessKey,
        password = password,
    )

    private companion object {
        const val PASSWORD_MARK = 16
        const val R2_PORT = 9000
    }
}
