package com.lebakin.finpulse.core.security

internal interface SecretCipher {
    fun encrypt(plainText: ByteArray): ByteArray

    fun decrypt(payload: ByteArray): ByteArray
}
