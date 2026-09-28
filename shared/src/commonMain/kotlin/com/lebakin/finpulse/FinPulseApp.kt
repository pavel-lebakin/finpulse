package com.lebakin.finpulse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.RootCommand
import com.lebakin.finpulse.core.uikit.EmptyState
import com.lebakin.finpulse.core.uikit.FinPulseTheme
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

private val navSavedState = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeDestination::class, HomeDestination.serializer())
        }
    }
}

@Composable
fun FinPulseApp() {
    val navigator = koinInject<ComposeRootNavigator>()
    FinPulseTheme {
        val backStack = rememberNavBackStack(navSavedState, HomeDestination)
        DisposableEffect(navigator, backStack) {
            navigator.attach { command ->
                when (command) {
                    is RootCommand.Open -> backStack.add(command.destination)
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
                entry<HomeDestination> {
                    ShellScreen()
                }
            },
        )
    }
}

@Composable
private fun ShellScreen() {
    val spacing = LocalFinPulseSpacing.current
    Box(
        modifier = Modifier.fillMaxSize().padding(spacing.l),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            title = "FinPulse",
            body = "Money stays on the device, with no account and no server.",
        )
    }
}
