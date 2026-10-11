package com.lebakin.finpulse.feature.insights.domain

import com.lebakin.finpulse.feature.insights.presentation.mapper.toUiModel
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class SpendingTest {
    @Test
    fun expensesFollowTheNewestCurrencyAndTheCurrentMonth() {
        val spending = spendingOf(
            transactions = listOf(
                transaction(
                    id = "market",
                    categoryId = "groceries",
                    amountMinor = 10_000,
                    currencyCode = "RUB",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Market",
                    occurredOn = LocalDate(2026, AUGUST, 2),
                ),
                transaction(
                    id = "metro",
                    categoryId = "transport",
                    amountMinor = 5_000,
                    currencyCode = "USD",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Metro",
                    occurredOn = LocalDate(2026, SEPTEMBER, 15),
                ),
                transaction(
                    id = "cafe",
                    categoryId = "eating-out",
                    amountMinor = 18_645,
                    currencyCode = "RUB",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Cafe",
                    occurredOn = LocalDate(2026, OCTOBER, 1),
                ),
                transaction(
                    id = "salary",
                    categoryId = "income",
                    amountMinor = 500_000,
                    currencyCode = "RUB",
                    direction = MoneyDirection.INCOME,
                    merchant = "Salary",
                    occurredOn = LocalDate(2026, OCTOBER, 5),
                ),
            ),
            categories = listOf(
                Category(id = "groceries", name = "Groceries", origin = RecordOrigin.USER),
                Category(id = "eating-out", name = "Eating out", origin = RecordOrigin.USER),
                Category(id = "transport", name = "Transport", origin = RecordOrigin.USER),
                Category(id = "income", name = "Income", origin = RecordOrigin.USER),
            ),
            today = LocalDate(2026, OCTOBER, 29),
        )
        assertEquals("RUB", spending.currencyCode)
        assertEquals(18_645, spending.monthExpenseMinor)
        assertEquals(listOf("Aug", "Sep", "Oct"), spending.months.map { it.label })
        assertEquals(listOf(10_000L, 0L, 18_645L), spending.months.map { it.expenseMinor })
        assertEquals(listOf("Eating out"), spending.categories.map { it.name })
        assertEquals(listOf("Salary", "Cafe", "Metro", "Market"), spending.recent.map { it.merchant })
        val ui = spending.toUiModel()
        assertEquals("−186.45 RUB", ui.monthExpense)
        assertEquals("5000.00 RUB", ui.recent.first().amount)
    }

    @Test
    fun januaryIncludesDecemberOfThePreviousYear() {
        val spending = spendingOf(
            transactions = listOf(
                transaction(
                    id = "pharmacy",
                    categoryId = "health",
                    amountMinor = 3_000,
                    currencyCode = "RUB",
                    direction = MoneyDirection.EXPENSE,
                    merchant = "Pharmacy",
                    occurredOn = LocalDate(2025, DECEMBER, 20),
                ),
            ),
            categories = emptyList(),
            today = LocalDate(2026, JANUARY, 10),
        )
        assertEquals(listOf("Nov", "Dec", "Jan"), spending.months.map { it.label })
        assertEquals(listOf(0L, 3_000L, 0L), spending.months.map { it.expenseMinor })
        assertEquals(emptyList(), spending.categories)
        val empty = spendingOf(emptyList(), emptyList(), LocalDate(2026, JANUARY, 10))
        assertEquals("", empty.currencyCode)
        assertEquals(listOf(0L, 0L, 0L), empty.months.map { it.expenseMinor })
    }
}

private fun transaction(
    id: String,
    categoryId: String,
    amountMinor: Long,
    currencyCode: String,
    direction: MoneyDirection,
    merchant: String,
    occurredOn: LocalDate,
): Transaction = Transaction(
    id = id,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    direction = direction,
    merchant = merchant,
    occurredOn = occurredOn,
    origin = RecordOrigin.USER,
)

private const val JANUARY = 1
private const val AUGUST = 8
private const val SEPTEMBER = 9
private const val OCTOBER = 10
private const val DECEMBER = 12
