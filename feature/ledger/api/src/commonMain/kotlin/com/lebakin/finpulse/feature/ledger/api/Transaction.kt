package com.lebakin.finpulse.feature.ledger.api

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: String,
    val categoryId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val direction: MoneyDirection,
    val merchant: String,
    val occurredOn: LocalDate,
    val origin: RecordOrigin,
)
