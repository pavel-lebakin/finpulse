package com.lebakin.finpulse.feature.settings.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.settings.presentation.host.ConsentHost
import com.lebakin.finpulse.feature.settings.presentation.host.KeyHost
import com.lebakin.finpulse.feature.settings.presentation.host.LicensesHost

fun EntryProviderScope<NavKey>.settingsGraph() {
    entry<SettingsDestination.Consent> {
        ConsentHost()
    }
    entry<SettingsDestination.Key> {
        KeyHost()
    }
    entry<SettingsDestination.Licenses> {
        LicensesHost()
    }
}
