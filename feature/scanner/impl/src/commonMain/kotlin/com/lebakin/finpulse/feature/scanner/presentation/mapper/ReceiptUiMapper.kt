package com.lebakin.finpulse.feature.scanner.presentation.mapper

import com.lebakin.finpulse.feature.scanner.domain.ReceiptDraft
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSample

private const val MINOR_PER_MAJOR = 100
private const val FRACTION_DIGITS = 2

data class ReceiptSampleUiModel(
    val id: String,
    val title: String,
)

data class ReceiptDraftUiModel(
    val merchant: String,
    val amount: String,
    val occurredOn: String,
    val categoryName: String,
)

internal fun ReceiptSample.toUiModel(): ReceiptSampleUiModel = ReceiptSampleUiModel(id = id, title = title)

internal fun ReceiptDraft.toUiModel(): ReceiptDraftUiModel = ReceiptDraftUiModel(
    merchant = merchant,
    amount = formatAmount(amountMinor, currencyCode),
    occurredOn = occurredOn.toString(),
    categoryName = categoryName,
)

internal fun formatAmount(amountMinor: Long, currencyCode: String): String {
    val major = amountMinor / MINOR_PER_MAJOR
    val fraction = (amountMinor % MINOR_PER_MAJOR).toString().padStart(FRACTION_DIGITS, '0')
    return "$major.$fraction $currencyCode"
}
