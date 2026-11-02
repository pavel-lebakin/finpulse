package com.lebakin.finpulse.feature.ledger.data.mapper

import com.lebakin.finpulse.core.database.CategoryEntity
import com.lebakin.finpulse.core.database.TransactionEntity
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.ledger.api.TransactionLine
import kotlinx.datetime.LocalDate

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    origin = RecordOrigin.valueOf(origin),
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    origin = origin.name,
)

internal fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    direction = MoneyDirection.valueOf(direction),
    merchant = merchant,
    occurredOn = LocalDate.parse(occurredOn),
    origin = RecordOrigin.valueOf(origin),
    lines = linesOf(linesText),
)

internal fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    categoryId = categoryId,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    direction = direction.name,
    merchant = merchant,
    occurredOn = occurredOn.toString(),
    origin = origin.name,
    linesText = linesTextOf(lines),
)

private fun linesTextOf(lines: List<TransactionLine>): String = lines.joinToString("\n") { line ->
    "${line.amountMinor}\t${line.name.replace('\t', ' ').replace('\n', ' ')}"
}

private fun linesOf(text: String): List<TransactionLine> = text.lineSequence().mapNotNull { row ->
    val tab = row.indexOf('\t')
    val minor = if (tab < 0) null else row.substring(0, tab).toLongOrNull()
    val name = if (tab < 0) "" else row.substring(tab + 1).trim()
    if (minor == null || minor <= 0L || name.isEmpty()) null else TransactionLine(name = name, amountMinor = minor)
}.toList()
