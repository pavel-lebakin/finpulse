package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.core.preferences.ThemePreferences
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsReducer
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlinx.coroutines.flow.collect

internal class SettingsScreenViewModel(
    private val navigator: SettingsNavigator,
    private val onboarding: OnboardingEntry,
    private val theme: ThemePreferences,
) : ScreenViewModel<SettingsState, SettingsAction>() {
    override fun createInitialState(): SettingsState = SettingsReducer.initial()

    init {
        launchCatching(onError = { error ->
            reduce { SettingsReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            theme.observe().collect { mode ->
                reduce { SettingsReducer.run { withTheme(mode) } }
            }
        }
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OpenConsent -> navigator.openConsent()
            SettingsAction.OpenKey -> navigator.openKey()
            SettingsAction.OpenLicenses -> navigator.openLicenses()
            is SettingsAction.ChooseTheme -> chooseTheme(action)
            SettingsAction.ShowOnboardingAgain -> showAgain()
            SettingsAction.DismissMessage -> reduce { SettingsReducer.run { withMessage(null) } }
            SettingsAction.DismissSheet -> reduce { SettingsReducer.run { withSheet(null) } }
        }
    }

    private fun chooseTheme(action: SettingsAction.ChooseTheme) {
        launchCatching(onError = { error ->
            reduce { SettingsReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            theme.set(action.mode)
        }
    }

    private fun showAgain() {
        launchCatching(onError = { error ->
            reduce { SettingsReducer.run { withMessage(error.toUserMessage()) } }
        }) {
            onboarding.showAgain()
        }
    }
}
