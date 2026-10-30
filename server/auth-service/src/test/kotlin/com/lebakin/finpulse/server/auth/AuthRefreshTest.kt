package com.lebakin.finpulse.server.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthRefreshTest {
    @Test
    fun currentTokenRotatesAndClearsThePresentedArray() {
        val token = ByteArray(REFRESH_BYTES) { 7 }
        val family = refreshFamily("family-1", "user-1", token)
        val presented = token.copyOf()
        val rotated = rotate(presented, family)
        assertIs<Refresh.Rotated>(rotated)
        assertEquals("family-1", rotated.family.id)
        assertEquals("user-1", rotated.family.accountId)
        assertEquals(false, rotated.family.revoked)
        assertEquals(REFRESH_BYTES, rotated.token.size)
        assertFalse(token.contentEquals(rotated.token))
        assertFalse(token.contentEquals(family.current))
        assertTrue(presented.all { it == 0.toByte() })
        assertEquals(7.toByte(), token[0])
        assertIs<Refresh.Rotated>(rotate(rotated.token.copyOf(), rotated.family))
    }

    @Test
    fun rememberedTokenRevokesTheFamily() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        val rotated = rotate(token.copyOf(), family)
        assertIs<Refresh.Rotated>(rotated)
        val reuse = rotate(token, rotated.family)
        assertIs<Refresh.Reused>(reuse)
        assertEquals(true, reuse.family.revoked)
        assertEquals("Refresh.Reused", reuse.toString())
        assertEquals(Refresh.Rejected, rotate(rotated.token.copyOf(), reuse.family))
    }

    @Test
    fun mismatchLeavesTheCurrentTokenUsable() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        val other = refreshToken()
        val stranger = other.copyOf()
        assertEquals(Refresh.Rejected, rotate(other, family))
        val rotated = rotate(token.copyOf(), family)
        assertIs<Refresh.Rotated>(rotated)
        assertEquals(Refresh.Rejected, rotate(stranger, family))
    }

    @Test
    fun anotherFamilyRejectsTheToken() {
        val token = refreshToken()
        val family = refreshFamily("family-1", "user-1", token)
        val other = refreshFamily("family-2", "user-1", refreshToken())
        assertEquals(Refresh.Rejected, rotate(token, other))
        assertEquals(false, family.revoked)
    }

    @Test
    fun forgottenTokenDoesNotRevoke() {
        val original = refreshToken()
        var family = refreshFamily("family-1", "user-1", original)
        var current = original
        repeat(REFRESH_HISTORY + 1) {
            val rotated = rotate(current.copyOf(), family)
            assertIs<Refresh.Rotated>(rotated)
            family = rotated.family
            current = rotated.token
        }
        assertEquals(Refresh.Rejected, rotate(original.copyOf(), family))
        assertIs<Refresh.Rotated>(rotate(current.copyOf(), family))
    }

    @Test
    fun invalidTokenIsAFixedMessageAndIsCleared() {
        val family = refreshFamily("family-1", "user-1", refreshToken())
        val token = ByteArray(4) { 'A'.code.toByte() }
        val error = assertFailsWith<IllegalArgumentException> { rotate(token, family) }
        assertEquals("Auth refresh token is invalid.", error.message)
        assertTrue(token.all { it == 0.toByte() })
        assertFalse(error.message.orEmpty().contains("AAAA"))
    }

    @Test
    fun invalidFamilyIsAFixedMessage() {
        val family = RefreshFamily("bad id", "user-1", ByteArray(REFRESH_BYTES), emptyList(), false)
        val token = ByteArray(REFRESH_BYTES) { 9 }
        val error = assertFailsWith<IllegalArgumentException> { rotate(token, family) }
        assertEquals("Auth refresh family is invalid.", error.message)
        assertTrue(token.all { it == 0.toByte() })
        assertFalse(error.message.orEmpty().contains("bad"))
    }

    @Test
    fun textOmitsTheToken() {
        val token = ByteArray(REFRESH_BYTES) { 7 }
        val family = refreshFamily("family-1", "user-1", token)
        val rotated = rotate(token.copyOf(), family)
        assertIs<Refresh.Rotated>(rotated)
        assertEquals("RefreshFamily(id=family-1, accountId=user-1, revoked=false)", family.toString())
        assertEquals("Refresh.Rotated", rotated.toString())
        assertFalse(family.toString().contains("7"))
        assertFalse(rotated.toString().contains("7"))
    }
}
