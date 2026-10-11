package com.lebakin.finpulse.feature.insights.presentation.navigation

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.feature.insights.api.InsightsEntry

internal interface InsightsNavigator {
    fun openDashboard()
}

internal class InsightsNavigatorImpl(
    private val root: RootNavigator,
) : InsightsNavigator {
    override fun openDashboard() {
        root.replace(InsightsDestination.Dashboard)
    }
}

internal class InsightsEntryImpl(
    private val navigator: InsightsNavigator,
) : InsightsEntry {
    override fun openDashboard() {
        navigator.openDashboard()
    }
}
