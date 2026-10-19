package com.lebakin.finpulse.feature.settings.api

interface SettingsWriter {
    suspend fun grantConsent()

    suspend fun declineConsent()
}
