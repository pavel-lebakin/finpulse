package com.lebakin.finpulse.feature.assistant.presentation.navigation

import com.lebakin.finpulse.core.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed interface AssistantDestination : Destination {
    @Serializable
    data object Ask : AssistantDestination

    @Serializable
    data object Confirm : AssistantDestination
}
