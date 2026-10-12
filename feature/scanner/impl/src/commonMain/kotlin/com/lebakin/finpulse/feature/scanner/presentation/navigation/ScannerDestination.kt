package com.lebakin.finpulse.feature.scanner.presentation.navigation

import com.lebakin.finpulse.core.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScannerDestination : Destination {
    @Serializable
    data object Capture : ScannerDestination

    @Serializable
    data object Review : ScannerDestination
}
