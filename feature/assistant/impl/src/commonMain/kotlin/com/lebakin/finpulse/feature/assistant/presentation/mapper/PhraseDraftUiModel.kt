package com.lebakin.finpulse.feature.assistant.presentation.mapper

import com.lebakin.finpulse.feature.assistant.domain.PhraseDraft
import com.lebakin.finpulse.feature.assistant.domain.formatAmount

data class PhraseDraftUiModel(
    val merchant: String,
    val amount: String,
    val occurredOn: String,
    val categoryName: String,
)

internal fun PhraseDraft.toUiModel(): PhraseDraftUiModel = PhraseDraftUiModel(
    merchant = merchant,
    amount = formatAmount(amountMinor, currencyCode),
    occurredOn = occurredOn.toString(),
    categoryName = categoryName,
)
