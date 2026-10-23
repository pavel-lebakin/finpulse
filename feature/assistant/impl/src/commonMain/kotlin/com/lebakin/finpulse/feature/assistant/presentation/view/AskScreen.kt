package com.lebakin.finpulse.feature.assistant.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskAction
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskContent
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.AskState
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.toUserMessage

@Composable
internal fun AskScreen(
    state: AskState,
    onAction: (AskAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Assistant", style = MaterialTheme.typography.headlineSmall)
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> FailedContent(message = content.error.toUserMessage().text, onAction = onAction)
            is Loadable.Ready -> AskBody(content = content.value, onAction = onAction)
        }
        TextButton(onClick = { onAction(AskAction.Back) }) {
            Text(text = "Back")
        }
        MessageText(message = state.message, onDismiss = { onAction(AskAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(AskAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun FailedContent(
    message: String,
    onAction: (AskAction) -> Unit,
) {
    Text(text = message)
    Button(onClick = { onAction(AskAction.Retry) }) {
        Text(text = "Try again")
    }
}

@Composable
private fun AskBody(
    content: AskContent,
    onAction: (AskAction) -> Unit,
) {
    var phrase by remember { mutableStateOf("") }
    Text(
        text = "A phrase can become a draft. Nothing is saved until you confirm it.",
        style = MaterialTheme.typography.bodyLarge,
    )
    OutlinedTextField(
        value = phrase,
        onValueChange = { phrase = it },
        label = { Text(text = "Phrase") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = { onAction(AskAction.Send(phrase)) },
        enabled = !content.sending && phrase.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = if (content.sending) "Reading" else "Send")
    }
    val answer = content.answer
    if (answer != null) {
        Text(text = answer, style = MaterialTheme.typography.bodyLarge)
    }
}
