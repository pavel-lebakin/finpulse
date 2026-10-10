package com.lebakin.finpulse.feature.onboarding.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.LaunchAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.LaunchState
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.toUserMessage

@Composable
internal fun LaunchScreen(
    state: LaunchState,
    onAction: (LaunchAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier.fillMaxSize().padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        val phase = state.phase
        if (phase is Loadable.Failed) {
            Text(text = phase.error.toUserMessage().text)
            Button(onClick = { onAction(LaunchAction.Retry) }) {
                Text(text = "Try again")
            }
        }
        MessageText(message = state.message, onDismiss = { onAction(LaunchAction.DismissMessage) })
        if (state.sheet != null) {
            Button(onClick = { onAction(LaunchAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}
