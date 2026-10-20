package com.lebakin.finpulse.feature.settings.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyContent
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyState
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.toUserMessage

@Composable
internal fun KeyScreen(
    state: KeyState,
    onAction: (KeyAction) -> Unit,
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
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> FailedContent(message = content.error.toUserMessage().text, onAction = onAction)
            is Loadable.Ready -> KeyBody(content = content.value, onAction = onAction)
        }
        MessageText(message = state.message, onDismiss = { onAction(KeyAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(KeyAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun FailedContent(
    message: String,
    onAction: (KeyAction) -> Unit,
) {
    Text(text = message)
    Button(onClick = { onAction(KeyAction.Retry) }) {
        Text(text = "Try again")
    }
}

@Composable
private fun KeyBody(
    content: KeyContent,
    onAction: (KeyAction) -> Unit,
) {
    when {
        content.saved -> SavedContent(onAction = onAction)
        content.granted -> EntryContent(onAction = onAction)
        else -> ConsentContent(onAction = onAction)
    }
}

@Composable
private fun ConsentContent(
    onAction: (KeyAction) -> Unit,
) {
    Text(text = NEEDS_CONSENT, style = MaterialTheme.typography.bodyLarge)
    Button(
        onClick = { onAction(KeyAction.OpenConsent) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Allow sending to Gemini")
    }
}

@Composable
private fun EntryContent(
    onAction: (KeyAction) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    Text(text = ENTRY, style = MaterialTheme.typography.bodyLarge)
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        label = { Text(text = "Gemini key") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = {
            val key = draft
            draft = ""
            onAction(KeyAction.Save(key))
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Save")
    }
}

@Composable
private fun SavedContent(
    onAction: (KeyAction) -> Unit,
) {
    Text(text = SAVED, style = MaterialTheme.typography.bodyLarge)
    Button(
        onClick = { onAction(KeyAction.Delete) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Delete")
    }
}

private const val TITLE = "Gemini key"

private const val NEEDS_CONSENT =
    "Agree to send receipt text before a Gemini key can be saved."

private const val ENTRY =
    "The key stays on this device. FinPulse does not show it again after you save."

private const val SAVED = "A Gemini key is saved."
