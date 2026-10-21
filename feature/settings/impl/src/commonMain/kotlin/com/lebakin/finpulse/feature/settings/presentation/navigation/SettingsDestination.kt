package com.lebakin.finpulse.feature.settings.presentation.navigation

import com.lebakin.finpulse.core.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsDestination : Destination {
    @Serializable
    data object Consent : SettingsDestination

    @Serializable
    data object Key : SettingsDestination

    @Serializable
    data object Licenses : SettingsDestination
}
