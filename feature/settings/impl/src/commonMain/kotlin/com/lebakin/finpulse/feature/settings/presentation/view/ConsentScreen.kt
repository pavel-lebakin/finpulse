package com.lebakin.finpulse.feature.settings.presentation.view

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
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage

@Composable
internal fun ConsentScreen(
    state: ConsentState,
    onAction: (ConsentAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = TITLE, style = MaterialTheme.typography.headlineSmall)
        when (val choice = state.choice) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> FailedContent(message = choice.error.toUserMessage().text, onAction = onAction)
            is Loadable.Ready -> ChoiceContent(granted = choice.value, onAction = onAction)
        }
        MessageText(message = state.message, onDismiss = { onAction(ConsentAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(ConsentAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun FailedContent(
    message: String,
    onAction: (ConsentAction) -> Unit,
) {
    Text(text = message)
    Button(onClick = { onAction(ConsentAction.Retry) }) {
        Text(text = "Try again")
    }
}

@Composable
private fun ChoiceContent(
    granted: Boolean,
    onAction: (ConsentAction) -> Unit,
) {
    Text(
        text = if (granted) AGREED else DISCLOSURE,
        style = MaterialTheme.typography.bodyLarge,
    )
    Button(
        onClick = { onAction(ConsentAction.Agree) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Agree")
    }
    Button(
        onClick = { onAction(ConsentAction.Decline) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Decline")
    }
}

private const val TITLE = "Send receipt text to Gemini"

private const val DISCLOSURE =
    "Agreeing lets FinPulse send receipt text to Google Gemini. The text leaves this device. " +
        "Nothing is sent until you agree. Declining keeps parsing on this device."

private const val AGREED =
    "You agreed. Receipt text may be sent to Google Gemini. Decline to stop that."
