package com.lebakin.finpulse.feature.settings.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lebakin.finpulse.feature.settings.presentation.host.ConsentHost
import com.lebakin.finpulse.feature.settings.presentation.host.KeyHost
import com.lebakin.finpulse.feature.settings.presentation.host.LicensesHost
import com.lebakin.finpulse.feature.settings.presentation.host.SettingsHost

fun EntryProviderScope<NavKey>.settingsGraph() {
    entry<SettingsDestination.Home> {
        SettingsSection()
    }
    entry<SettingsDestination.Consent> {
        ConsentSection()
    }
    entry<SettingsDestination.Key> {
        KeySection()
    }
    entry<SettingsDestination.Licenses> {
        LicensesSection()
    }
}

@Composable
fun SettingsSection() {
    SettingsHost()
}

@Composable
fun ConsentSection() {
    ConsentHost()
}

@Composable
fun KeySection() {
    KeyHost()
}

@Composable
fun LicensesSection() {
    LicensesHost()
}
