package com.lebakin.finpulse.feature.scanner.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureAction
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureContent
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureState
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.toUserMessage

@Composable
internal fun CaptureScreen(
    state: CaptureState,
    onAction: (CaptureAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Scan a receipt", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Take a photo or choose one from the gallery. Nothing is saved until you confirm.",
            style = MaterialTheme.typography.bodyLarge,
        )
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> Text(text = content.error.toUserMessage().text)
            is Loadable.Ready -> CaptureBody(content = content.value, onAction = onAction)
        }
        MessageText(message = state.message, onDismiss = { onAction(CaptureAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(CaptureAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun CaptureBody(
    content: CaptureContent,
    onAction: (CaptureAction) -> Unit,
) {
    if (content.reading) {
        Text(text = "Reading the receipt")
    }
    Button(onClick = { onAction(CaptureAction.TakePhoto) }, enabled = !content.reading) {
        Text(text = "Take a photo")
    }
    Button(onClick = { onAction(CaptureAction.PickFromGallery) }, enabled = !content.reading) {
        Text(text = "Choose from gallery")
    }
}
