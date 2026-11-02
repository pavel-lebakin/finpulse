package com.lebakin.finpulse.feature.insights.domain

import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal data class MonthSpend(
    val label: String,
    val expenseMinor: Long,
)

internal data class CategorySpend(
    val name: String,
    val expenseMinor: Long,
)

internal data class RecentMovement(
    val id: String,
    val merchant: String,
    val occurredOn: LocalDate,
    val amountMinor: Long,
    val currencyCode: String,
    val direction: MoneyDirection,
    val lineCount: Int,
)

internal data class Spending(
    val currencyCode: String,
    val monthExpenseMinor: Long,
    val months: List<MonthSpend>,
    val categories: List<CategorySpend>,
    val recent: List<RecentMovement>,
)

internal fun interface InsightsClock {
    fun today(): LocalDate
}

internal fun spendingOf(
    transactions: List<Transaction>,
    categories: List<Category>,
    today: LocalDate,
): Spending {
    val currency = transactions
        .sortedWith(newestFirst())
        .firstOrNull()
        ?.currencyCode
        .orEmpty()
    val names = categories.associate { it.id to it.name }
    val window = monthWindow(today)
    val months = window.map { start ->
        MonthSpend(
            label = MONTH_LABELS[start.month.ordinal],
            expenseMinor = expenseIn(transactions, currency, start),
        )
    }
    val current = window.last()
    return Spending(
        currencyCode = currency,
        monthExpenseMinor = months.last().expenseMinor,
        months = months,
        categories = categorySpend(transactions, names, currency, current),
        recent = transactions.sortedWith(newestFirst()).take(RECENT_LIMIT).map { it.toRecent() },
    )
}

private fun expenseIn(
    transactions: List<Transaction>,
    currency: String,
    monthStart: LocalDate,
): Long = transactions
    .filter { it.fallsInMonth(currency, monthStart) }
    .sumOf { it.amountMinor }

private fun categorySpend(
    transactions: List<Transaction>,
    names: Map<String, String>,
    currency: String,
    monthStart: LocalDate,
): List<CategorySpend> = transactions
    .filter { it.fallsInMonth(currency, monthStart) }
    .groupBy { it.categoryId }
    .map { (id, rows) ->
        CategorySpend(
            name = names[id] ?: id,
            expenseMinor = rows.sumOf { it.amountMinor },
        )
    }
    .sortedWith(compareByDescending<CategorySpend> { it.expenseMinor }.thenBy { it.name })

private fun Transaction.fallsInMonth(currency: String, monthStart: LocalDate): Boolean {
    if (currencyCode != currency || direction != MoneyDirection.EXPENSE) {
        return false
    }
    val next = monthStart.plus(DatePeriod(months = 1))
    return occurredOn >= monthStart && occurredOn < next
}

private fun Transaction.toRecent(): RecentMovement = RecentMovement(
    id = id,
    merchant = merchant,
    occurredOn = occurredOn,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    direction = direction,
    lineCount = lines.size,
)

private fun newestFirst(): Comparator<Transaction> =
    compareByDescending<Transaction> { it.occurredOn }.thenByDescending { it.id }

private fun monthWindow(today: LocalDate): List<LocalDate> {
    val current = LocalDate(today.year, today.month, FIRST_DAY)
    return List(MONTH_COUNT) { offset ->
        current.minus(DatePeriod(months = MONTH_COUNT - 1 - offset))
    }
}

private const val MONTH_COUNT = 3
private const val RECENT_LIMIT = 5
private const val FIRST_DAY = 1

private val MONTH_LABELS = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)
