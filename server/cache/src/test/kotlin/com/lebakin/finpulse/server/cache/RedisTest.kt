package com.lebakin.finpulse.server.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RedisTest {
    @Test
    fun hostIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openRedis(endpoint(host = "127.0.0.1/redis"))
        }
        assertEquals("Redis host is invalid.", error.message)
    }

    @Test
    fun userIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openRedis(endpoint(user = "default;drop"))
        }
        assertEquals("Redis user is invalid.", error.message)
    }

    @Test
    fun portIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openRedis(endpoint(port = 0))
        }
        assertEquals("Redis port is invalid.", error.message)
    }

    @Test
    fun blankPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openRedis(endpoint(password = " "))
        }
        assertEquals("Redis password is empty.", error.message)
    }

    @Test
    fun passwordStaysOutOfToString() {
        val password = "pw" + "a".repeat(PASSWORD_MARK)
        val endpoint = endpoint(password = password)
        assertFalse(endpoint.toString().contains(password))
        assertTrue(endpoint.toString().contains("user=default"))
    }

    private fun endpoint(
        host: String = "127.0.0.1",
        port: Int = REDIS_PORT,
        user: String = "default",
        password: String = "pw" + "b".repeat(PASSWORD_MARK),
    ) = RedisEndpoint(
        host = host,
        port = port,
        user = user,
        password = password,
    )

    private companion object {
        const val PASSWORD_MARK = 16
        const val REDIS_PORT = 6379
    }
}
