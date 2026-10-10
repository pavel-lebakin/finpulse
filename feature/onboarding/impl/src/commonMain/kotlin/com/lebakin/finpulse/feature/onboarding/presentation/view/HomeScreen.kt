package com.lebakin.finpulse.feature.onboarding.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.EmptyState
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.NoticeBanner
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeContent
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.HomeState
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.toUserMessage

@Composable
internal fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> {
                Text(text = content.error.toUserMessage().text)
                Button(onClick = { onAction(HomeAction.Retry) }) {
                    Text(text = "Try again")
                }
            }
            is Loadable.Ready -> HomeBody(content = content.value, onAction = onAction)
        }
        TextButton(onClick = { onAction(HomeAction.ShowOnboardingAgain) }) {
            Text(text = "Show onboarding again")
        }
        MessageText(message = state.message, onDismiss = { onAction(HomeAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(HomeAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun HomeBody(
    content: HomeContent,
    onAction: (HomeAction) -> Unit,
) {
    when (content) {
        HomeContent.Showcase -> NoticeBanner(
            message = "This is sample data. It is removed when you add your first transaction.",
            actionLabel = "Clear now",
            onAction = { onAction(HomeAction.ClearSample) },
        )
        HomeContent.Empty -> EmptyState(
            title = "Nothing here yet",
            body = "Scan a receipt or write a phrase when you want to add a transaction.",
        )
        HomeContent.Ready -> EmptyState(
            title = "FinPulse",
            body = "Your transactions stay on this device.",
        )
    }
}
