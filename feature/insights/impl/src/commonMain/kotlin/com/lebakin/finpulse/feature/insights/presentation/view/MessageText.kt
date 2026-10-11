package com.lebakin.finpulse.feature.insights.presentation.view

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.UserMessage

@Composable
internal fun MessageText(
    message: UserMessage?,
    onDismiss: () -> Unit,
) {
    val text = message?.text ?: return
    Text(text = text)
    TextButton(onClick = onDismiss) {
        Text(text = "Dismiss")
    }
}
