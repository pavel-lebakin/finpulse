package com.lebakin.finpulse.feature.insights.presentation.navigation

import com.lebakin.finpulse.core.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed interface InsightsDestination : Destination {
    @Serializable
    data object Dashboard : InsightsDestination

    @Serializable
    data class Receipt(val transactionId: String) : InsightsDestination
}
