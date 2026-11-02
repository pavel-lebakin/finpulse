package com.lebakin.finpulse.feature.insights.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.uikit.LocalFinPulseSpacing
import com.lebakin.finpulse.core.uikit.TransactionRow
import com.lebakin.finpulse.feature.insights.presentation.mapper.ReceiptDetailUiModel
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.ReceiptAction
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.ReceiptState
import com.lebakin.finpulse.feature.insights.presentation.model.mvi.toUserMessage

@Composable
internal fun ReceiptScreen(
    state: ReceiptState,
    onAction: (ReceiptAction) -> Unit,
) {
    val spacing = LocalFinPulseSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        TextButton(onClick = { onAction(ReceiptAction.Back) }) {
            Text(text = "Back")
        }
        Text(text = "Receipt", style = MaterialTheme.typography.headlineSmall)
        when (val content = state.content) {
            Loadable.Loading -> Text(text = "Loading")
            is Loadable.Failed -> Text(text = content.error.toUserMessage().text)
            is Loadable.Ready -> ReceiptBody(receipt = content.value)
        }
    }
}

@Composable
private fun ReceiptBody(receipt: ReceiptDetailUiModel) {
    Text(text = receipt.merchant, style = MaterialTheme.typography.bodyLarge)
    receipt.lines.forEach { line ->
        TransactionRow(title = line.name, subtitle = receipt.merchant, amount = line.amount)
    }
    TransactionRow(title = "Total", subtitle = receipt.occurredOn, amount = receipt.total)
}
