package com.lebakin.finpulse.feature.settings.data.repository

import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.security.SecretStore
import com.lebakin.finpulse.feature.settings.api.KeySave
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import kotlinx.coroutines.flow.Flow

internal class SettingsRepository(
    private val preferences: ConsentPreferences,
    private val secrets: SecretStore,
) : SettingsReader, SettingsWriter {
    override fun observeConsent(): Flow<Boolean> = preferences.observeGranted()

    override fun observeKeySaved(): Flow<Boolean> = secrets.observeSaved()

    override suspend fun grantConsent() {
        preferences.setGranted(true)
    }

    override suspend fun declineConsent() {
        preferences.setGranted(false)
    }

    override suspend fun saveKey(key: String): KeySave = when {
        !preferences.isGranted() -> KeySave.NeedsConsent
        !acceptedKey(key) -> KeySave.Rejected
        else -> {
            secrets.save(key)
            KeySave.Saved
        }
    }

    override suspend fun deleteKey() {
        secrets.delete()
    }

    private companion object {
        const val MAX_KEY_CHARS = 512

        fun acceptedKey(key: String): Boolean {
            if (key.length !in 1..MAX_KEY_CHARS) return false
            return key.all { character -> character in '!'..'~' }
        }
    }
}
