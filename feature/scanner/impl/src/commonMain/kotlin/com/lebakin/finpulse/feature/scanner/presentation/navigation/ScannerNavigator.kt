package com.lebakin.finpulse.feature.scanner.presentation.navigation

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.feature.scanner.api.ScannerEntry

internal interface ScannerNavigator {
    fun openCapture()

    fun openReview()

    fun back()
}

internal class ScannerNavigatorImpl(
    private val root: RootNavigator,
) : ScannerNavigator {
    override fun openCapture() {
        root.navigate(ScannerDestination.Capture)
    }

    override fun openReview() {
        root.navigate(ScannerDestination.Review)
    }

    override fun back() {
        root.back()
    }
}

internal class ScannerEntryImpl(
    private val navigator: ScannerNavigator,
) : ScannerEntry {
    override fun openCapture() {
        navigator.openCapture()
    }
}
