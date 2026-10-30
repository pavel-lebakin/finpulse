package com.lebakin.finpulse.core.preferences

import kotlinx.coroutines.flow.Flow

interface ThemePreferences {
    fun observe(): Flow<ThemeMode>

    suspend fun set(mode: ThemeMode)
}
