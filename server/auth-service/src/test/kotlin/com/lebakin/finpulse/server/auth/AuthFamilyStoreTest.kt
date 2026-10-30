package com.lebakin.finpulse.server.auth

import com.lebakin.finpulse.server.cache.openRedis
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthFamilyStoreTest {
    @Test
    fun saveThenLoadKeepsTheCurrentTokenOutOfRedis() {
        val token = ByteArray(REFRESH_BYTES) { 7 }
        val family = refreshFamily("family-1", "user-1", token)
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                saveFamily(redis, family)
                val stored = redis.get("refresh:family-1")
                assertTrue(stored != null)
                assertTrue(stored.orEmpty().startsWith("1.family-1.user-1.0."))
                assertTrue(stored.orEmpty().endsWith(".-"))
                assertFalse(stored.orEmpty().contains("07".repeat(REFRESH_BYTES)))
                val loaded = loadFamily(redis, "family-1")
                assertTrue(loaded != null)
                assertEquals("family-1", loaded?.id)
                assertEquals("user-1", loaded?.accountId)
                assertEquals(false, loaded?.revoked)
                assertEquals("RefreshFamily(id=family-1, accountId=user-1, revoked=false)", loaded.toString())
                assertTrue(sameHash(family.current, loaded?.current))
                val rotated = rotate(token.copyOf(), requireNotNull(loaded))
                assertIs<Refresh.Rotated>(rotated)
            }
        }
        assertEquals(7.toByte(), token[0])
    }

    @Test
    fun rememberedTokenRevokesAfterReload() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        val rotated = rotate(token.copyOf(), family)
        assertIs<Refresh.Rotated>(rotated)
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                saveFamily(redis, rotated.family)
                val loaded = loadFamily(redis, "family-1")
                assertTrue(loaded != null)
                val reuse = rotate(token, requireNotNull(loaded))
                assertIs<Refresh.Reused>(reuse)
                assertEquals(true, reuse.family.revoked)
                saveFamily(redis, reuse.family)
                val revoked = loadFamily(redis, "family-1")
                assertEquals(true, revoked?.revoked)
                assertEquals(Refresh.Rejected, rotate(rotated.token.copyOf(), requireNotNull(revoked)))
            }
        }
    }

    @Test
    fun missingKeyIsNullAndAnotherIdStaysSeparate() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                assertNull(loadFamily(redis, "family-1"))
                saveFamily(redis, family)
                assertNull(loadFamily(redis, "family-2"))
                assertTrue(loadFamily(redis, "family-1") != null)
            }
        }
    }

    @Test
    fun overwriteReplacesTheCurrentHash() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        val rotated = rotate(token.copyOf(), family)
        assertIs<Refresh.Rotated>(rotated)
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                saveFamily(redis, family)
                saveFamily(redis, rotated.family)
                val loaded = requireNotNull(loadFamily(redis, "family-1"))
                assertTrue(sameHash(rotated.family.current, loaded.current))
                assertIs<Refresh.Rotated>(rotate(rotated.token.copyOf(), loaded))
            }
        }
    }

    @Test
    fun corruptValueIsAFixedMessage() {
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                redis.set("refresh:family-1", "not-a-hash")
                val error = assertFailsWith<IllegalArgumentException> {
                    loadFamily(redis, "family-1")
                }
                assertEquals("Auth refresh record is invalid.", error.message)
                assertFalse(error.message.orEmpty().contains("not-a-hash"))
            }
        }
    }

    @Test
    fun invalidFamilyAndIdAreFixedMessages() {
        val family = RefreshFamily("bad id", "user-1", ByteArray(REFRESH_BYTES), emptyList(), false)
        RedisProcess.start().use { server ->
            openRedis(server.endpoint).use { redis ->
                val saved = assertFailsWith<IllegalArgumentException> { saveFamily(redis, family) }
                assertEquals("Auth refresh family is invalid.", saved.message)
                assertFalse(saved.message.orEmpty().contains("bad"))
                val loaded = assertFailsWith<IllegalArgumentException> { loadFamily(redis, "bad id") }
                assertEquals("Auth refresh family is invalid.", loaded.message)
                assertFalse(loaded.message.orEmpty().contains("bad"))
                assertNull(redis.get("refresh:bad id"))
            }
        }
    }
}

private fun sameHash(left: ByteArray?, right: ByteArray?): Boolean =
    left != null && right != null && MessageDigest.isEqual(left, right)
