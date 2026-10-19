package com.lebakin.finpulse.feature.settings.api

import kotlinx.coroutines.flow.Flow

interface SettingsReader {
    fun observeConsent(): Flow<Boolean>
}
