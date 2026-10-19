package com.lebakin.finpulse.feature.settings.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.settings.presentation.host.ConsentHost

fun EntryProviderScope<NavKey>.settingsGraph() {
    entry<SettingsDestination.Consent> {
        ConsentHost()
    }
}
