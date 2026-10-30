package com.lebakin.finpulse.feature.assistant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.assistant.presentation.host.AskHost
import com.lebakin.finpulse.feature.assistant.presentation.host.ConfirmHost

fun EntryProviderScope<NavKey>.assistantGraph() {
    entry<AssistantDestination.Ask> {
        AskSection()
    }
    entry<AssistantDestination.Confirm> {
        ConfirmSection()
    }
}

@Composable
fun AskSection() {
    AskHost()
}

@Composable
fun ConfirmSection() {
    ConfirmHost()
}
