package com.lebakin.finpulse.feature.settings.presentation.navigation

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.feature.settings.api.SettingsEntry

internal interface SettingsNavigator {
    fun openConsent()

    fun openKey()

    fun back()
}

internal class SettingsNavigatorImpl(
    private val root: RootNavigator,
) : SettingsNavigator {
    override fun openConsent() {
        root.navigate(SettingsDestination.Consent)
    }

    override fun openKey() {
        root.navigate(SettingsDestination.Key)
    }

    override fun back() {
        root.back()
    }
}

internal class SettingsEntryImpl(
    private val navigator: SettingsNavigator,
) : SettingsEntry {
    override fun openConsent() {
        navigator.openConsent()
    }

    override fun openKey() {
        navigator.openKey()
    }
}
