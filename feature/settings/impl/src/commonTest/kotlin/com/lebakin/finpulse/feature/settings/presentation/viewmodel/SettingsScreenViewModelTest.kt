package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.preferences.ThemeMode
import com.lebakin.finpulse.core.preferences.ThemePreferences
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SettingsAction
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun openConsentLeavesTheList() {
        val navigator = SettingsListNavigator()
        val model = settingsModel(navigator)
        model.onAction(SettingsAction.OpenConsent)
        assertEquals(listOf("open"), navigator.calls)
    }

    @Test
    fun openKeyLeavesTheList() {
        val navigator = SettingsListNavigator()
        val model = settingsModel(navigator)
        model.onAction(SettingsAction.OpenKey)
        assertEquals(listOf("key"), navigator.calls)
    }

    @Test
    fun chooseThemeStoresTheMode() {
        val theme = MemoryTheme()
        val model = settingsModel(theme = theme)
        model.onAction(SettingsAction.ChooseTheme(ThemeMode.Dark))
        assertEquals(listOf(ThemeMode.Dark), theme.writes)
        assertEquals(ThemeMode.Dark, model.state.value.theme)
    }

    @Test
    fun showAgainOpensOnboarding() {
        val onboarding = SettingsListOnboarding()
        val model = settingsModel(onboarding = onboarding)
        model.onAction(SettingsAction.ShowOnboardingAgain)
        assertEquals(1, onboarding.calls)
    }

    private fun settingsModel(
        navigator: SettingsListNavigator = SettingsListNavigator(),
        onboarding: SettingsListOnboarding = SettingsListOnboarding(),
        theme: MemoryTheme = MemoryTheme(),
    ) = SettingsScreenViewModel(
        navigator = navigator,
        onboarding = onboarding,
        theme = theme,
    )
}

private class MemoryTheme : ThemePreferences {
    private val mode = MutableStateFlow(ThemeMode.System)
    val writes = mutableListOf<ThemeMode>()

    override fun observe(): Flow<ThemeMode> = mode

    override suspend fun set(mode: ThemeMode) {
        writes += mode
        this.mode.value = mode
    }
}

private class SettingsListOnboarding : OnboardingEntry {
    var calls: Int = 0

    override suspend fun showAgain() {
        calls += 1
    }
}

private class SettingsListNavigator : SettingsNavigator {
    val calls = mutableListOf<String>()

    override fun openConsent() {
        calls += "open"
    }

    override fun openKey() {
        calls += "key"
    }

    override fun back() {
        calls += "back"
    }
}
