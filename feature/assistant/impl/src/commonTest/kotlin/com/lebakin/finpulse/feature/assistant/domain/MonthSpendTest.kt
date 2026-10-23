package com.lebakin.finpulse.feature.assistant.domain

import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class MonthSpendTest {
    @Test
    fun anEmptyLedgerSpentNothing() {
        assertEquals("You spent nothing this month.", monthSpendAnswer(emptyList(), TODAY))
    }

    @Test
    fun thisMonthUsesTheNewestCurrency() {
        val answer = monthSpendAnswer(
            transactions = listOf(
                expense(id = "old", amountMinor = 100, currency = "USD", day = 1),
                expense(id = "new", amountMinor = 250, currency = "EUR", day = 3),
                expense(id = "income", amountMinor = 900, currency = "EUR", day = 4, direction = MoneyDirection.INCOME),
            ),
            today = TODAY,
        )

        assertEquals("You spent 2.50 EUR this month.", answer)
    }
}

private val TODAY = LocalDate(2026, 10, 29)

private fun expense(
    id: String,
    amountMinor: Long,
    currency: String,
    day: Int,
    direction: MoneyDirection = MoneyDirection.EXPENSE,
) = Transaction(
    id = id,
    categoryId = "general",
    amountMinor = amountMinor,
    currencyCode = currency,
    direction = direction,
    merchant = "Shop",
    occurredOn = LocalDate(2026, 10, day),
    origin = RecordOrigin.USER,
)
