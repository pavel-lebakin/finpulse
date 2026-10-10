package com.lebakin.finpulse.core.preferences

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferences {
    fun observeCompleted(): Flow<Boolean>

    suspend fun setCompleted(completed: Boolean)
}
