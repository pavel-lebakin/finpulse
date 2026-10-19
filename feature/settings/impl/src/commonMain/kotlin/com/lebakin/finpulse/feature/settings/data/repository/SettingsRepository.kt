package com.lebakin.finpulse.feature.settings.data.repository

import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import kotlinx.coroutines.flow.Flow

internal class SettingsRepository(
    private val preferences: ConsentPreferences,
) : SettingsReader, SettingsWriter {
    override fun observeConsent(): Flow<Boolean> = preferences.observeGranted()

    override suspend fun grantConsent() {
        preferences.setGranted(true)
    }

    override suspend fun declineConsent() {
        preferences.setGranted(false)
    }
}
