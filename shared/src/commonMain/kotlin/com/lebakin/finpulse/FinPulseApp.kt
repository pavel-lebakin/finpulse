package com.lebakin.finpulse

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.RootCommand
import com.lebakin.finpulse.core.preferences.ThemeMode
import com.lebakin.finpulse.core.preferences.ThemePreferences
import com.lebakin.finpulse.core.uikit.FinPulseTheme
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AskSection
import com.lebakin.finpulse.feature.assistant.presentation.navigation.ConfirmSection
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantDestination
import com.lebakin.finpulse.feature.assistant.presentation.navigation.assistantGraph
import com.lebakin.finpulse.feature.insights.presentation.navigation.DashboardSection
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsDestination
import com.lebakin.finpulse.feature.insights.presentation.navigation.insightsGraph
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingDestination
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.onboardingGraph
import com.lebakin.finpulse.feature.scanner.presentation.navigation.CaptureSection
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ReviewSection
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerDestination
import com.lebakin.finpulse.feature.scanner.presentation.navigation.scannerGraph
import com.lebakin.finpulse.feature.settings.presentation.navigation.ConsentSection
import com.lebakin.finpulse.feature.settings.presentation.navigation.KeySection
import com.lebakin.finpulse.feature.settings.presentation.navigation.LicensesSection
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsDestination
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsSection
import com.lebakin.finpulse.feature.settings.presentation.navigation.settingsGraph
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

private val navSavedState = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingDestination.Launch::class, OnboardingDestination.Launch.serializer())
            subclass(OnboardingDestination.Flow::class, OnboardingDestination.Flow.serializer())
            subclass(InsightsDestination.Dashboard::class, InsightsDestination.Dashboard.serializer())
            subclass(ScannerDestination.Capture::class, ScannerDestination.Capture.serializer())
            subclass(ScannerDestination.Review::class, ScannerDestination.Review.serializer())
            subclass(AssistantDestination.Ask::class, AssistantDestination.Ask.serializer())
            subclass(AssistantDestination.Confirm::class, AssistantDestination.Confirm.serializer())
            subclass(SettingsDestination.Home::class, SettingsDestination.Home.serializer())
            subclass(SettingsDestination.Consent::class, SettingsDestination.Consent.serializer())
            subclass(SettingsDestination.Key::class, SettingsDestination.Key.serializer())
            subclass(SettingsDestination.Licenses::class, SettingsDestination.Licenses.serializer())
        }
    }
}

@Composable
fun FinPulseApp() {
    val navigator = koinInject<ComposeRootNavigator>()
    val themeMode by koinInject<ThemePreferences>().observe().collectAsState(ThemeMode.System)
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    FinPulseTheme(dark = dark) {
        AppSystemBars(dark = dark)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            val backStack = rememberNavBackStack(navSavedState, OnboardingDestination.Launch)
            DisposableEffect(navigator, backStack) {
                navigator.attach { command ->
                    when (command) {
                        is RootCommand.Open -> backStack.add(command.destination)
                        is RootCommand.Replace -> {
                            backStack.clear()
                            backStack.add(command.destination)
                        }
                        RootCommand.Back -> if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    }
                }
                onDispose { navigator.detach() }
            }
            AppFrame(
                navigator = navigator,
                backStack = backStack,
                section = { page -> SectionPage(page) },
                detail = { key -> DetailPage(key) },
                onboarding = {
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.fillMaxSize(),
                        onBack = { navigator.back() },
                        transitionSpec = opaquePush(),
                        popTransitionSpec = opaquePop(),
                        predictivePopTransitionSpec = opaquePredictivePop(),
                        entryProvider = entryProvider {
                            onboardingGraph()
                            insightsGraph()
                            scannerGraph()
                            assistantGraph()
                            settingsGraph()
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun SectionPage(page: Int) {
    when (page) {
        PAGE_HOME -> DashboardSection()
        PAGE_SCAN -> CaptureSection()
        PAGE_ASK -> AskSection()
        else -> SettingsSection()
    }
}

@Composable
private fun DetailPage(key: NavKey) {
    when (key) {
        ScannerDestination.Review -> ReviewSection()
        AssistantDestination.Confirm -> ConfirmSection()
        SettingsDestination.Consent -> ConsentSection()
        SettingsDestination.Key -> KeySection()
        SettingsDestination.Licenses -> LicensesSection()
        else -> Box(modifier = Modifier.fillMaxSize())
    }
}

private const val PAGE_HOME = 0

private const val PAGE_SCAN = 1

private const val PAGE_ASK = 2
