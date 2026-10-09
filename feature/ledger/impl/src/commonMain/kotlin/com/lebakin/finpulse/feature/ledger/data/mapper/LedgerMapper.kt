package com.lebakin.finpulse.feature.ledger.data.mapper

import com.lebakin.finpulse.core.database.CategoryEntity
import com.lebakin.finpulse.core.database.TransactionEntity
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
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
)
