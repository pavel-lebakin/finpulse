package com.lebakin.finpulse.feature.assistant.domain

import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import kotlinx.datetime.LocalDate

internal data class PhraseDraft(
    val merchant: String,
    val amountMinor: Long,
    val currencyCode: String,
    val occurredOn: LocalDate,
    val categoryName: String,
)

internal data class UserPhrase(
    val category: Category,
    val transaction: Transaction,
)

internal fun interface PhraseIds {
    fun next(): String
}

internal fun PhraseDraft.toUserPhrase(ids: PhraseIds): UserPhrase {
    val categoryId = ids.next()
    return UserPhrase(
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
        ),
    )
}
