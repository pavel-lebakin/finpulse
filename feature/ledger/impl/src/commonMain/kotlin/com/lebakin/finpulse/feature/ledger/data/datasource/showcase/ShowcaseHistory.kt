package com.lebakin.finpulse.feature.ledger.data.datasource.showcase

import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal data class ShowcaseSnapshot(
    val categories: List<Category>,
    val transactions: List<Transaction>,
)

internal object ShowcaseHistory {
    const val CURRENCY = "RUB"
    private const val MONTHS_BEFORE_TODAY = 2
    private const val EXPENSE_STEP_DAYS = 3
    private const val PAYDAY = 5
    private const val MARKET_MINOR = 186_450L
    private const val METRO_MINOR = 6_400L
    private const val CAFE_MINOR = 45_900L
    private const val PHARMACY_MINOR = 32_000L
    private const val UTILITIES_MINOR = 210_000L
    private const val CINEMA_MINOR = 89_000L
    private const val SALARY_MINOR = 18_500_000L

    private val categoryNames = listOf(
        "groceries" to "Groceries",
        "transport" to "Transport",
        "eating-out" to "Eating out",
        "home" to "Home",
        "health" to "Health",
        "fun" to "Fun",
        "income" to "Income",
    )
    private val expenseRows = listOf(
        ExpenseRow("groceries", "Market", MARKET_MINOR, MoneyDirection.EXPENSE),
        ExpenseRow("transport", "Metro", METRO_MINOR, MoneyDirection.EXPENSE),
        ExpenseRow("eating-out", "Cafe", CAFE_MINOR, MoneyDirection.EXPENSE),
        ExpenseRow("health", "Pharmacy", PHARMACY_MINOR, MoneyDirection.EXPENSE),
        ExpenseRow("home", "Utilities", UTILITIES_MINOR, MoneyDirection.EXPENSE),
        ExpenseRow("fun", "Cinema", CINEMA_MINOR, MoneyDirection.EXPENSE),
    )
    private val salary = ExpenseRow("income", "Salary", SALARY_MINOR, MoneyDirection.INCOME)

    fun create(today: LocalDate): ShowcaseSnapshot {
        val start = monthStart(today).minus(DatePeriod(months = MONTHS_BEFORE_TODAY))
        return ShowcaseSnapshot(
            categories = categories(),
            transactions = expenses(start, today) + income(start, today),
        )
    }

    private fun categories(): List<Category> = categoryNames.map { (id, name) ->
        Category(id = id, name = name, origin = RecordOrigin.SHOWCASE)
    }

    private fun expenses(start: LocalDate, today: LocalDate): List<Transaction> = buildList {
        var date = start
        var index = 0
        while (date <= today) {
            val row = expenseRows[index % expenseRows.size]
            add(transaction(id = "showcase-expense-$index", date = date, row = row))
            date = date.plus(DatePeriod(days = EXPENSE_STEP_DAYS))
            index++
        }
    }

    private fun income(start: LocalDate, today: LocalDate): List<Transaction> = buildList {
        var month = start
        while (month <= today) {
            val payday = LocalDate(month.year, month.monthNumber, PAYDAY)
            if (payday <= today) {
                add(
                    transaction(
                        id = "showcase-income-${payday.year}-${payday.monthNumber}",
                        date = payday,
                        row = salary,
                    ),
                )
            }
            month = monthStart(month).plus(DatePeriod(months = 1))
        }
    }

    private fun transaction(id: String, date: LocalDate, row: ExpenseRow): Transaction = Transaction(
        id = id,
        categoryId = row.categoryId,
        amountMinor = row.amountMinor,
        currencyCode = CURRENCY,
        direction = row.direction,
        merchant = row.merchant,
        occurredOn = date,
        origin = RecordOrigin.SHOWCASE,
    )

    private fun monthStart(day: LocalDate): LocalDate = LocalDate(day.year, day.monthNumber, 1)

    private data class ExpenseRow(
        val categoryId: String,
        val merchant: String,
        val amountMinor: Long,
        val direction: MoneyDirection,
    )
}
