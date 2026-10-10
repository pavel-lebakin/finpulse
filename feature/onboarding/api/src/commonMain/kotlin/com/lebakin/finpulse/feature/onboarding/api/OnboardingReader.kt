package com.lebakin.finpulse.feature.onboarding.api

import kotlinx.coroutines.flow.Flow

interface OnboardingReader {
    fun observeCompleted(): Flow<Boolean>
}
