package com.lebakin.finpulse.feature.onboarding.api

interface OnboardingWriter {
    suspend fun complete(choice: OnboardingChoice)

    suspend fun showAgain()
}
