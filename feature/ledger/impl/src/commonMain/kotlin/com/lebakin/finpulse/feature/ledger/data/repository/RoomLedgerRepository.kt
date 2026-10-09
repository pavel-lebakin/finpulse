package com.lebakin.finpulse.feature.ledger.data.repository

import com.lebakin.finpulse.core.database.FinPulseDatabase
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.ledger.data.datasource.showcase.ShowcaseHistory
import com.lebakin.finpulse.feature.ledger.data.mapper.toDomain
import com.lebakin.finpulse.feature.ledger.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomLedgerRepository(
    private val database: FinPulseDatabase,
    private val showcasePreferences: ShowcasePreferences,
    private val clock: LedgerClock,
) : LedgerReader, LedgerWriter {
    override fun observeTransactions(): Flow<List<Transaction>> =
        database.ledgerDao().observeTransactions().map { rows -> rows.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> =
        database.ledgerDao().observeCategories().map { rows -> rows.map { it.toDomain() } }

    override suspend fun seedShowcase() {
        val snapshot = ShowcaseHistory.create(clock.today())
        val seeded = database.ledgerDao().replaceOriginWhenAbsent(
            blockingOrigin = RecordOrigin.USER.name,
            replacedOrigin = RecordOrigin.SHOWCASE.name,
            categories = snapshot.categories.map { it.toEntity() },
            transactions = snapshot.transactions.map { it.toEntity() },
        )
        if (seeded) {
            showcasePreferences.setShowcaseMode(true)
        }
    }

    override suspend fun clearShowcase() {
        database.ledgerDao().deleteOrigin(RecordOrigin.SHOWCASE.name)
        showcasePreferences.setShowcaseMode(false)
    }

    override suspend fun addUserTransaction(category: Category, transaction: Transaction): Boolean {
        requireUserWrite(category, transaction)
        val sampleRemoved = database.ledgerDao().replaceOrigin(
            removedOrigin = RecordOrigin.SHOWCASE.name,
            category = category.copy(origin = RecordOrigin.USER).toEntity(),
            transaction = transaction.copy(origin = RecordOrigin.USER).toEntity(),
        )
        showcasePreferences.setShowcaseMode(false)
        return sampleRemoved
    }

    private fun requireUserWrite(category: Category, transaction: Transaction) {
        require(category.id.isNotBlank()) { "Category id is required." }
        require(transaction.id.isNotBlank()) { "Transaction id is required." }
        require(category.id == transaction.categoryId) { "Transaction category does not match." }
        require(transaction.amountMinor > 0) { "Amount must be positive." }
        require(transaction.currencyCode.isNotBlank()) { "Currency is required." }
        require(transaction.merchant.isNotBlank()) { "Merchant is required." }
    }
}
