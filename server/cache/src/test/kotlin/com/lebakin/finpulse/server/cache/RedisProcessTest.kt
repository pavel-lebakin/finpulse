package com.lebakin.finpulse.server.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RedisProcessTest {
    @Test
    fun setThenGet() {
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                assertNull(redis.get(KEY))
                redis.set(KEY, VALUE)
                assertEquals(VALUE, redis.get(KEY))
            }
        }
    }

    @Test
    fun blankKeyIsRejected() {
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                val error = assertFailsWith<IllegalArgumentException> {
                    redis.set("", VALUE)
                }
                assertEquals("Redis key is invalid.", error.message)
            }
        }
    }

    @Test
    fun longValueIsRejected() {
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                val error = assertFailsWith<IllegalArgumentException> {
                    redis.set(KEY, "x".repeat(VALUE_LIMIT))
                }
                assertEquals("Redis value is too long.", error.message)
            }
        }
    }

    @Test
    fun wrongPasswordIsRejected() {
        RedisProcess.start().use { server ->
            val password = server.endpoint.password + "x"
            val endpoint = RedisEndpoint(
                host = server.endpoint.host,
                port = server.endpoint.port,
                user = server.endpoint.user,
                password = password,
            )
            val error = assertFailsWith<IllegalStateException> {
                openRedis(endpoint)
            }
            assertEquals("Redis authentication failed.", error.message)
            assertFalse(error.toString().contains(password))
            assertFalse(error.toString().contains(server.endpoint.password))
        }
    }

    private companion object {
        const val KEY = "marker"
        const val VALUE = "7"
        const val VALUE_LIMIT = 65_537
    }
}
