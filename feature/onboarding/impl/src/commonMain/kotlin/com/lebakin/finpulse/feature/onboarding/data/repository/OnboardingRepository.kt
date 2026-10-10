package com.lebakin.finpulse.feature.onboarding.data.repository

import com.lebakin.finpulse.core.preferences.OnboardingPreferences
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.onboarding.api.OnboardingChoice
import com.lebakin.finpulse.feature.onboarding.api.OnboardingReader
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter
import kotlinx.coroutines.flow.Flow

internal class OnboardingRepository(
    private val preferences: OnboardingPreferences,
    private val ledger: LedgerWriter,
) : OnboardingReader, OnboardingWriter {
    override fun observeCompleted(): Flow<Boolean> = preferences.observeCompleted()

    override suspend fun complete(choice: OnboardingChoice) {
        when (choice) {
            OnboardingChoice.Sample -> ledger.seedShowcase()
            OnboardingChoice.Empty -> ledger.clearShowcase()
        }
        preferences.setCompleted(true)
    }

    override suspend fun showAgain() {
        preferences.setCompleted(false)
    }
}
