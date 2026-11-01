package com.lebakin.finpulse.feature.scanner.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.MessageSnackbar
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
    Box(modifier = Modifier.fillMaxSize()) {
        ReviewColumn(state = state, onAction = onAction, modifier = Modifier.fillMaxSize())
        MessageSnackbar(
            message = state.message?.text,
            onDismiss = { onAction(ReviewAction.DismissMessage) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ReviewColumn(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        Text(text = "Review", style = MaterialTheme.typography.headlineSmall)
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> Text(text = content.error.toUserMessage().text)
            is Loadable.Ready -> ReviewBody(
                draft = content.value,
                saved = state.saved,
                onAction = onAction,
            )
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
        if (state.sheet != null) {
            TextButton(onClick = { onAction(ReviewAction.DismissSheet) }) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun ReviewBody(
    draft: ReceiptDraftUiModel,
    saved: Boolean,
    onAction: (ReviewAction) -> Unit,
) {
    Text(text = "Currency", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(LocalFinPulseSpacing.current.s)) {
        CURRENCIES.forEach { code ->
            if (code == draft.currencyCode) {
                Button(onClick = { onAction(ReviewAction.SelectCurrency(code)) }, enabled = !saved) {
                    Text(text = code)
                }
            } else {
                TextButton(onClick = { onAction(ReviewAction.SelectCurrency(code)) }, enabled = !saved) {
                    Text(text = code)
                }
            }
        }
    }
    if (draft.lines.isEmpty()) {
        Text(text = "Expense", style = MaterialTheme.typography.bodyLarge)
        TransactionRow(
            title = draft.merchant,
            subtitle = "${draft.categoryName} · ${draft.occurredOn}",
            amount = draft.amount,
        )
    } else {
        Text(text = draft.merchant, style = MaterialTheme.typography.bodyLarge)
        draft.lines.forEach { line ->
            TransactionRow(
                title = line.name,
                subtitle = draft.merchant,
                amount = line.amount,
            )
        }
        TransactionRow(
            title = "Total",
            subtitle = "${draft.categoryName} · ${draft.occurredOn}",
            amount = draft.amount,
        )
    }
}

private val CURRENCIES = listOf("USD", "EUR", "GBP", "RUB")
