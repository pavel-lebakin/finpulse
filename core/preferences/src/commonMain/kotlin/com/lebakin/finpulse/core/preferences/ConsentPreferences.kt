package com.lebakin.finpulse.core.preferences

import kotlinx.coroutines.flow.Flow

interface ConsentPreferences {
    fun observeGranted(): Flow<Boolean>

    suspend fun isGranted(): Boolean

    suspend fun setGranted(granted: Boolean)
}
