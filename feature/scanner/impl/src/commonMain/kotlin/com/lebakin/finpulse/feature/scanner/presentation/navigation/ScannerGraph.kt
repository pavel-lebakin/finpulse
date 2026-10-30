package com.lebakin.finpulse.feature.scanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.scanner.presentation.host.CaptureHost
import com.lebakin.finpulse.feature.scanner.presentation.host.ReviewHost

fun EntryProviderScope<NavKey>.scannerGraph() {
    entry<ScannerDestination.Capture> {
        CaptureSection()
    }
    entry<ScannerDestination.Review> {
        ReviewSection()
    }
}

@Composable
fun CaptureSection() {
    CaptureHost()
}

@Composable
fun ReviewSection() {
    ReviewHost()
}
