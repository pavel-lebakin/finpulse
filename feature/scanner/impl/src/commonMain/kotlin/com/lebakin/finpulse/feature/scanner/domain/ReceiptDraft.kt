package com.lebakin.finpulse.feature.scanner.domain

import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.ledger.api.TransactionLine
import kotlinx.datetime.LocalDate

internal data class MoneyToken(
    val amountMinor: Long,
    val currencyCode: String,
    val explicit: Boolean = false,
)

internal data class ReceiptLine(
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val explicit: Boolean = false,
)

internal data class ReceiptDraft(
    val merchant: String,
    val amountMinor: Long,
    val currencyCode: String,
    val occurredOn: LocalDate,
    val categoryName: String,
    val lines: List<ReceiptLine> = emptyList(),
)

internal data class UserReceipt(
    val category: Category,
    val transaction: Transaction,
)

internal fun interface ReceiptIds {
    fun next(): String
}

internal fun ReceiptDraft.toUserReceipt(ids: ReceiptIds): UserReceipt {
    val categoryId = ids.next()
    return UserReceipt(
        category = Category(
            id = categoryId,
            name = categoryName,
            origin = RecordOrigin.USER,
        ),
        transaction = Transaction(
            id = ids.next(),
            categoryId = categoryId,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            direction = MoneyDirection.EXPENSE,
            merchant = merchant,
            occurredOn = occurredOn,
            origin = RecordOrigin.USER,
            lines = lines.map { line -> TransactionLine(name = line.name, amountMinor = line.amountMinor) },
        ),
    )
}
