package com.lebakin.finpulse.feature.insights.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.insights.presentation.host.DashboardHost
import com.lebakin.finpulse.feature.insights.presentation.host.ReceiptHost

fun EntryProviderScope<NavKey>.insightsGraph() {
    entry<InsightsDestination.Dashboard> {
        DashboardSection()
    }
    entry<InsightsDestination.Receipt> { key ->
        ReceiptSection(transactionId = key.transactionId)
    }
}

@Composable
fun DashboardSection() {
    DashboardHost()
}

@Composable
fun ReceiptSection(transactionId: String) {
    ReceiptHost(transactionId = transactionId)
}
