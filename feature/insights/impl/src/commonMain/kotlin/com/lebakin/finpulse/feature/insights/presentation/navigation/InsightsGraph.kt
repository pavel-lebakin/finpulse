package com.lebakin.finpulse.feature.insights.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.insights.presentation.host.DashboardHost

fun EntryProviderScope<NavKey>.insightsGraph() {
    entry<InsightsDestination.Dashboard> {
        DashboardSection()
    }
}

@Composable
fun DashboardSection() {
    DashboardHost()
}
