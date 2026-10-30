package com.lebakin.finpulse.feature.assistant.presentation.view

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
import com.lebakin.finpulse.core.uikit.TransactionRow
import com.lebakin.finpulse.feature.assistant.presentation.mapper.PhraseDraftUiModel
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmAction
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.ConfirmState
import com.lebakin.finpulse.feature.assistant.presentation.model.mvi.toUserMessage

@Composable
internal fun ConfirmScreen(
    state: ConfirmState,
    onAction: (ConfirmAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Confirm", style = MaterialTheme.typography.headlineSmall)
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> Text(text = content.error.toUserMessage().text)
            is Loadable.Ready -> ConfirmBody(draft = content.value)
        }
        if (state.saved) {
            Button(onClick = { onAction(ConfirmAction.Done) }) {
                Text(text = "Done")
            }
        } else if (state.content is Loadable.Ready) {
            Button(onClick = { onAction(ConfirmAction.Confirm) }, enabled = !state.saving) {
                Text(text = "Confirm")
            }
        }
        MessageText(message = state.message, onDismiss = { onAction(ConfirmAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(ConfirmAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun ConfirmBody(draft: PhraseDraftUiModel) {
    Text(text = "Expense", style = MaterialTheme.typography.bodyLarge)
    TransactionRow(
        title = draft.merchant,
        subtitle = "${draft.categoryName} · ${draft.occurredOn}",
        amount = draft.amount,
    )
}
