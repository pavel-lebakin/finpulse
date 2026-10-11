package com.lebakin.finpulse.feature.insights.presentation.mapper

import com.lebakin.finpulse.feature.insights.domain.Spending
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection

data class MonthBarUiModel(
    val label: String,
    val amount: Float,
)

data class CategorySpendUiModel(
    val name: String,
    val amount: String,
)

data class RecentMovementUiModel(
    val title: String,
    val subtitle: String,
    val amount: String,
)

data class SpendingUiModel(
    val monthExpense: String,
    val months: List<MonthBarUiModel>,
    val categories: List<CategorySpendUiModel>,
    val recent: List<RecentMovementUiModel>,
)

internal fun Spending.toUiModel(): SpendingUiModel = SpendingUiModel(
    monthExpense = formatMinor(monthExpenseMinor, currencyCode, expense = monthExpenseMinor > 0),
    months = months.map { month ->
        MonthBarUiModel(label = month.label, amount = month.expenseMinor.toFloat())
    },
    categories = categories.map { category ->
        CategorySpendUiModel(
            name = category.name,
            amount = formatMinor(category.expenseMinor, currencyCode, expense = true),
        )
    },
    recent = recent.map { movement ->
        RecentMovementUiModel(
            title = movement.merchant,
            subtitle = movement.occurredOn.toString(),
            amount = formatMinor(
                amountMinor = movement.amountMinor,
                currencyCode = movement.currencyCode,
                expense = movement.direction == MoneyDirection.EXPENSE,
            ),
        )
    },
)

internal fun formatMinor(amountMinor: Long, currencyCode: String, expense: Boolean): String {
    val major = amountMinor / MINOR_PER_MAJOR
    val fraction = (amountMinor % MINOR_PER_MAJOR).toString().padStart(FRACTION_DIGITS, '0')
    val prefix = if (expense && amountMinor > 0) "−" else ""
    val code = if (currencyCode.isEmpty()) "" else " $currencyCode"
    return "$prefix$major.$fraction$code"
}

private const val MINOR_PER_MAJOR = 100L
private const val FRACTION_DIGITS = 2
