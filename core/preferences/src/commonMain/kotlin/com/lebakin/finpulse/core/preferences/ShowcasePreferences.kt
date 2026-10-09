package com.lebakin.finpulse.core.preferences

import kotlinx.coroutines.flow.Flow

interface ShowcasePreferences {
    fun observeShowcaseMode(): Flow<Boolean>

    suspend fun setShowcaseMode(enabled: Boolean)
}
