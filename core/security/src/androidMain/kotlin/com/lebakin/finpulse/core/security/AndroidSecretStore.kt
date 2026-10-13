package com.lebakin.finpulse.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val secretDispatcher: CoroutineDispatcher = Dispatchers.IO

private var filesDirectory: String? = null

fun bindAndroidSecretDirectory(directory: String) {
    filesDirectory = directory
}

actual fun openSecretStore(): SecretStore {
    val directory = checkNotNull(filesDirectory) {
        "Call bindAndroidSecretDirectory before opening the secret store."
    }
    return openCiphertextSecretStore(
        path = "$directory/finpulse.secrets.preferences_pb",
        cipher = KeystoreSecretCipher(),
    )
}

private class KeystoreSecretCipher : SecretCipher {
    override fun encrypt(plainText: ByteArray): ByteArray = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText)
        iv + encrypted
    } catch (error: GeneralSecurityException) {
        throw IllegalStateException("Secret could not be saved.", error)
    }

    override fun decrypt(payload: ByteArray): ByteArray {
        if (payload.size <= IV_BYTES) {
            throw IllegalStateException("Secret could not be read.")
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = payload.copyOfRange(0, IV_BYTES)
            val body = payload.copyOfRange(IV_BYTES, payload.size)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(body)
        } catch (error: GeneralSecurityException) {
            throw IllegalStateException("Secret could not be read.", error)
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            try {
                load(null)
            } catch (error: GeneralSecurityException) {
                throw IllegalStateException("Secret could not be read.", error)
            } catch (error: IOException) {
                throw IllegalStateException("Secret could not be read.", error)
            }
        }
        val existing = keyStore.getKey(ALIAS, null) as? SecretKey
        if (existing != null) {
            return existing
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "finpulse_byok"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val KEY_BITS = 256
    }
}
