package com.lebakin.finpulse.core.security

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class CiphertextSecretStoreTest {
    @Test
    fun saveReadsBackAndDeleteClearsTheFlag() = runTest {
        val directory = Files.createTempDirectory("finpulse-secrets")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = openCiphertextSecretStore(
            path = directory.resolve("finpulse.secrets.preferences_pb").toString(),
            cipher = XorCipher(),
            scope = scope,
        )
        try {
            assertFalse(store.observeSaved().first())
            assertNull(store.read())
            store.save("super-secret-value")
            assertTrue(store.observeSaved().first())
            assertEquals("super-secret-value", store.read())
            store.delete()
            assertFalse(store.observeSaved().first())
            assertNull(store.read())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun blankSecretIsRejectedAndNothingIsStored() = runTest {
        val directory = Files.createTempDirectory("finpulse-secrets-blank")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = openCiphertextSecretStore(
            path = directory.resolve("finpulse.secrets.preferences_pb").toString(),
            cipher = XorCipher(),
            scope = scope,
        )
        try {
            assertFailsWith<IllegalArgumentException> {
                store.save("   ")
            }
            assertFalse(store.observeSaved().first())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun fileDoesNotContainThePlainSecret() = runTest {
        val directory = Files.createTempDirectory("finpulse-secrets-file")
        val path = directory.resolve("finpulse.secrets.preferences_pb")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = openCiphertextSecretStore(
            path = path.toString(),
            cipher = XorCipher(),
            scope = scope,
        )
        try {
            store.save("super-secret-value")
            store.read()
            val stored = Files.readAllBytes(path).toString(Charsets.ISO_8859_1)
            assertFalse(stored.contains("super-secret-value"))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun sealHidesTheSecretAndUnsealRestoresIt() {
        val cipher = XorCipher()
        val sealedValue = seal(cipher, "super-secret-value")
        assertFalse(sealedValue.contains("super-secret-value"))
        assertEquals("super-secret-value", unseal(cipher, sealedValue))
    }
}

private class XorCipher : SecretCipher {
    override fun encrypt(plainText: ByteArray): ByteArray = plainText.map { byte ->
        (byte.toInt() xor MASK).toByte()
    }.toByteArray()

    override fun decrypt(payload: ByteArray): ByteArray = encrypt(payload)

    private companion object {
        const val MASK = 0x5A
    }
}
