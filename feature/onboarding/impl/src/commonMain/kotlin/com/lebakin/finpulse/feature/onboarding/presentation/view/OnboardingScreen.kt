package com.lebakin.finpulse.feature.onboarding.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lebakin.finpulse.core.uikit.BackArrow
import com.lebakin.finpulse.core.uikit.FinPulseIcons
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingAction
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingPageUiModel
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingState

private val PageMarkSize = 160.dp

private val PageIconSize = 72.dp

private val BackSlot = 48.dp

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
            .padding(spacing.l),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(width = BackSlot, height = BackSlot)
                .align(Alignment.Start),
        ) {
            if (state.index > 0) {
                BackArrow(onBack = { onAction(OnboardingAction.Back) })
            }
        }
        PageBody(
            page = page,
            index = state.index,
            count = state.pages.size,
            modifier = Modifier.weight(1f),
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
            MessageText(message = state.message, onDismiss = { onAction(OnboardingAction.DismissMessage) })
            if (state.sheet != null) {
                TextButton(onClick = { onAction(OnboardingAction.DismissSheet) }) {
                    Text(text = "Close")
                }
            }
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
        }
    }
}

@Composable
private fun PageBody(
    page: OnboardingPageUiModel,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PageMark(index = index)
        Text(
            text = page.title,
            modifier = Modifier.padding(top = spacing.l),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = page.body,
            modifier = Modifier.padding(top = spacing.m),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "${index + 1} of $count",
            modifier = Modifier.padding(top = spacing.m),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PageMark(index: Int) {
    val icon = when (index) {
        0 -> FinPulseIcons.Private
        1 -> FinPulseIcons.Scan
        2 -> FinPulseIcons.Ask
        else -> FinPulseIcons.Sample
    }
    Box(
        modifier = Modifier
            .size(PageMarkSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(PageIconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
