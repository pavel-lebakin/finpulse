package com.lebakin.finpulse.feature.onboarding.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.onboarding.presentation.host.HomeHost
import com.lebakin.finpulse.feature.onboarding.presentation.host.LaunchHost
import com.lebakin.finpulse.feature.onboarding.presentation.host.OnboardingHost

fun EntryProviderScope<NavKey>.onboardingGraph() {
    entry<OnboardingDestination.Launch> {
        LaunchHost()
    }
    entry<OnboardingDestination.Flow> {
        OnboardingHost()
    }
    entry<OnboardingDestination.Home> {
        HomeHost()
    }
}
