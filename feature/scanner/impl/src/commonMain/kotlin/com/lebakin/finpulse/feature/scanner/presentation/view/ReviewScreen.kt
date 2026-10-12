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
import com.lebakin.finpulse.core.uikit.TransactionRow
import com.lebakin.finpulse.feature.scanner.presentation.mapper.ReceiptDraftUiModel
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewAction
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.ReviewState
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.toUserMessage

@Composable
internal fun ReviewScreen(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Review", style = MaterialTheme.typography.headlineSmall)
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> Text(text = content.error.toUserMessage().text)
            is Loadable.Ready -> ReviewBody(draft = content.value)
        }
        if (state.saved) {
            Button(onClick = { onAction(ReviewAction.Done) }) {
                Text(text = "Done")
            }
        } else if (state.content is Loadable.Ready) {
            Button(onClick = { onAction(ReviewAction.Confirm) }, enabled = !state.saving) {
                Text(text = "Confirm")
            }
        }
        if (!state.saved) {
            TextButton(onClick = { onAction(ReviewAction.Back) }) {
                Text(text = "Back")
            }
        }
        MessageText(message = state.message, onDismiss = { onAction(ReviewAction.DismissMessage) })
        if (state.sheet != null) {
            TextButton(onClick = { onAction(ReviewAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun ReviewBody(draft: ReceiptDraftUiModel) {
    Text(text = "Expense", style = MaterialTheme.typography.bodyLarge)
    TransactionRow(
        title = draft.merchant,
        subtitle = "${draft.categoryName} · ${draft.occurredOn}",
        amount = draft.amount,
    )
}
