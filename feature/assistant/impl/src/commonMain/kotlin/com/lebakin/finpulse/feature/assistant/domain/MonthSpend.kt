package com.lebakin.finpulse.feature.assistant.domain

import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val MINOR_PER_MAJOR = 100
private const val FRACTION_DIGITS = 2
private const val FIRST_DAY = 1

private const val NOTHING = "You spent nothing this month."

internal fun monthSpendAnswer(transactions: List<Transaction>, today: LocalDate): String = when {
    transactions.isEmpty() -> NOTHING
    else -> spent(transactions, today)
}

private fun spent(transactions: List<Transaction>, today: LocalDate): String {
    val currency = transactions.sortedWith(newestFirst()).first().currencyCode
    val start = LocalDate(today.year, today.month, FIRST_DAY)
    val next = start.plus(DatePeriod(months = 1))
    val minor = transactions
        .filter { it.fallsInMonth(currency, start, next) }
        .sumOf { it.amountMinor }
    return "You spent ${formatAmount(minor, currency)} this month."
}

private fun Transaction.fallsInMonth(currency: String, start: LocalDate, next: LocalDate): Boolean =
    currencyCode == currency &&
        direction == MoneyDirection.EXPENSE &&
        occurredOn >= start &&
        occurredOn < next

private fun newestFirst(): Comparator<Transaction> =
    compareByDescending<Transaction> { it.occurredOn }.thenByDescending { it.id }

internal fun formatAmount(amountMinor: Long, currencyCode: String): String {
    val major = amountMinor / MINOR_PER_MAJOR
    val fraction = (amountMinor % MINOR_PER_MAJOR).toString().padStart(FRACTION_DIGITS, '0')
    return "$major.$fraction $currencyCode"
}
