package com.lebakin.finpulse.feature.onboarding.presentation.navigation

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter

internal interface OnboardingNavigator {
    fun openFlow()
}

internal class OnboardingNavigatorImpl(
    private val root: RootNavigator,
) : OnboardingNavigator {
    override fun openFlow() {
        root.replace(OnboardingDestination.Flow)
    }
}

internal class OnboardingEntryImpl(
    private val writer: OnboardingWriter,
    private val navigator: OnboardingNavigator,
) : OnboardingEntry {
    override suspend fun showAgain() {
        writer.showAgain()
        navigator.openFlow()
    }
}
