package com.lebakin.finpulse.feature.insights.presentation.mapper

import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.Transaction

data class ReceiptLineUiModel(
    val name: String,
    val amount: String,
)

data class ReceiptDetailUiModel(
    val merchant: String,
    val occurredOn: String,
    val total: String,
    val lines: List<ReceiptLineUiModel>,
)

internal fun Transaction.toDetail(): ReceiptDetailUiModel = ReceiptDetailUiModel(
    merchant = merchant,
    occurredOn = occurredOn.toString(),
    total = formatMinor(amountMinor, currencyCode, expense = direction == MoneyDirection.EXPENSE),
    lines = lines.map { line ->
        ReceiptLineUiModel(
            name = line.name,
            amount = formatMinor(line.amountMinor, currencyCode, expense = true),
        )
    },
)
