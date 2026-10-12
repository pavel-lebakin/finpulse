package com.lebakin.finpulse.feature.scanner.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.scanner.presentation.host.CaptureHost
import com.lebakin.finpulse.feature.scanner.presentation.host.ReviewHost

fun EntryProviderScope<NavKey>.scannerGraph() {
    entry<ScannerDestination.Capture> {
        CaptureHost()
    }
    entry<ScannerDestination.Review> {
        ReviewHost()
    }
}
