package com.lebakin.finpulse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.RootCommand
import com.lebakin.finpulse.core.uikit.FinPulseTheme
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsDestination
import com.lebakin.finpulse.feature.insights.presentation.navigation.insightsGraph
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingDestination
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.onboardingGraph
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerDestination
import com.lebakin.finpulse.feature.scanner.presentation.navigation.scannerGraph
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
        }
    }
}

@Composable
fun FinPulseApp() {
    val navigator = koinInject<ComposeRootNavigator>()
    FinPulseTheme {
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
        NavDisplay(
            backStack = backStack,
            onBack = { navigator.back() },
            entryProvider = entryProvider {
                onboardingGraph()
                insightsGraph()
                scannerGraph()
            },
        )
    }
}
