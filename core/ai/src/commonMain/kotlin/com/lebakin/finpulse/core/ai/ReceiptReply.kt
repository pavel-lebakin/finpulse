package com.lebakin.finpulse.core.ai

import kotlinx.datetime.LocalDate

enum class ReceiptCategory(val label: String) {
    Groceries("Groceries"),
    Transport("Transport"),
    EatingOut("Eating out"),
    General("General"),
}

data class ReceiptReply(
    val merchant: String,
    val amountMinor: Long,
    val currencyCode: String,
    val occurredOn: LocalDate,
    val category: ReceiptCategory,
)

sealed interface ReceiptCheck {
    data class Accepted(val reply: ReceiptReply) : ReceiptCheck

    data object Rejected : ReceiptCheck
}
