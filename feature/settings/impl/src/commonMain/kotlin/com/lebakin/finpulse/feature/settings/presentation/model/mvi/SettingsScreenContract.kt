package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.mvi.ScreenAction
import com.lebakin.finpulse.core.mvi.ScreenState
import com.lebakin.finpulse.core.preferences.ThemeMode

sealed interface SettingsSheet

data class SettingsState(
    val theme: ThemeMode,
    val message: UserMessage?,
    val sheet: SettingsSheet?,
) : ScreenState

sealed interface SettingsAction : ScreenAction {
    data object OpenConsent : SettingsAction

    data object OpenKey : SettingsAction

    data class ChooseTheme(val mode: ThemeMode) : SettingsAction

    data object ShowOnboardingAgain : SettingsAction

    data object DismissMessage : SettingsAction

    data object DismissSheet : SettingsAction
}

object SettingsReducer {
    fun initial(): SettingsState = SettingsState(
        theme = ThemeMode.System,
        message = null,
        sheet = null,
    )

    fun SettingsState.withTheme(theme: ThemeMode): SettingsState = copy(theme = theme)

    fun SettingsState.withMessage(message: UserMessage?): SettingsState = copy(message = message)

    fun SettingsState.withSheet(sheet: SettingsSheet?): SettingsState = copy(sheet = sheet)
}
