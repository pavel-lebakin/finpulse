package com.lebakin.finpulse.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal const val PASSWORD_ITERATIONS = 600_000
internal const val SALT_BYTES = 16
internal const val HASH_BYTES = 32
private const val HASH_BITS = HASH_BYTES * Byte.SIZE_BITS
private const val ALPHABET = 26
private const val PLACEHOLDER_ID = "unknown-user"
private const val PLACEHOLDER_EMAIL = "unknown@auth.invalid"

fun passwordVerifier(password: CharArray): PasswordVerifier {
    try {
        require(validPassword(password)) { "Auth password is invalid." }
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = derive(password, salt, PASSWORD_ITERATIONS)
        val verifier = PasswordVerifier(salt, hash, PASSWORD_ITERATIONS)
        salt.fill(0)
        hash.fill(0)
        return verifier
    } finally {
        password.fill('\u0000')
    }
}

internal fun passwordMatches(password: CharArray, verifier: PasswordVerifier): Boolean {
    val derived = derive(password, verifier.salt, verifier.iterations)
    try {
        return MessageDigest.isEqual(derived, verifier.hash)
    } finally {
        derived.fill(0)
    }
}

internal val placeholderAccount: AuthAccount by lazy {
    val password = CharArray(MIN_PASSWORD_LENGTH)
    val random = SecureRandom()
    for (index in password.indices) {
        password[index] = 'a' + random.nextInt(ALPHABET)
    }
    AuthAccount(PLACEHOLDER_ID, PLACEHOLDER_EMAIL, passwordVerifier(password))
}

private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
    val spec = PBEKeySpec(password, salt, iterations, HASH_BITS)
    try {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    } finally {
        spec.clearPassword()
    }
}
