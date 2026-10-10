package com.lebakin.finpulse.feature.onboarding.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingState

@Composable
internal fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {
    val page = state.pages.getOrNull(state.index) ?: return
    val spacing = LocalFinPulseSpacing.current
    val last = state.index == state.pages.lastIndex
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(
            text = "${state.index + 1} of ${state.pages.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = page.title, style = MaterialTheme.typography.headlineSmall)
        Text(text = page.body, style = MaterialTheme.typography.bodyLarge)
        if (last) {
            Button(
                onClick = { onAction(OnboardingAction.SeeSample) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "See a sample")
            }
            Button(
                onClick = { onAction(OnboardingAction.StartEmpty) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Start from an empty ledger")
            }
        } else {
            Button(
                onClick = { onAction(OnboardingAction.Next) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Next")
            }
        }
        if (state.index > 0) {
            TextButton(onClick = { onAction(OnboardingAction.Back) }) {
                Text(text = "Back")
            }
        }
        MessageText(message = state.message, onDismiss = { onAction(OnboardingAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(OnboardingAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}
