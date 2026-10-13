package com.lebakin.finpulse.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.io.encoding.Base64
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal expect val secretDispatcher: CoroutineDispatcher

internal fun openCiphertextSecretStore(
    path: String,
    cipher: SecretCipher,
    scope: CoroutineScope = CoroutineScope(secretDispatcher + SupervisorJob()),
): SecretStore {
    val store = PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { path.toPath() },
    )
    return CiphertextSecretStore(store, cipher)
}

internal fun seal(cipher: SecretCipher, secret: String): String {
    val payload = cipher.encrypt(secret.encodeToByteArray())
    return Base64.encode(payload)
}

internal fun unseal(cipher: SecretCipher, sealedValue: String): String {
    val payload = try {
        Base64.decode(sealedValue)
    } catch (error: IllegalArgumentException) {
        throw IllegalStateException("Secret could not be read.", error)
    }
    return cipher.decrypt(payload).decodeToString()
}

private class CiphertextSecretStore(
    private val store: DataStore<Preferences>,
    private val cipher: SecretCipher,
) : SecretStore {
    override fun observeSaved(): Flow<Boolean> = store.data.map { preferences ->
        preferences[CIPHERTEXT] != null
    }

    override suspend fun save(secret: String) {
        require(secret.isNotBlank()) { "Secret is empty." }
        val sealedValue = seal(cipher, secret)
        store.edit { preferences ->
            preferences[CIPHERTEXT] = sealedValue
        }
    }

    override suspend fun read(): String? {
        val sealedValue = store.data.first()[CIPHERTEXT] ?: return null
        return unseal(cipher, sealedValue)
    }

    override suspend fun delete() {
        store.edit { preferences ->
            preferences.remove(CIPHERTEXT)
        }
    }

    private companion object {
        val CIPHERTEXT = stringPreferencesKey("byok_ciphertext")
    }
}
