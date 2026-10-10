package com.lebakin.finpulse.feature.onboarding.presentation.navigation

import com.lebakin.finpulse.core.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingDestination : Destination {
    @Serializable
    data object Launch : OnboardingDestination

    @Serializable
    data object Flow : OnboardingDestination

    @Serializable
    data object Home : OnboardingDestination
}
