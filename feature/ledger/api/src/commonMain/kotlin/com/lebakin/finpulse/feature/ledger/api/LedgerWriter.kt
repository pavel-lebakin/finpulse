package com.lebakin.finpulse.feature.ledger.api

interface LedgerWriter {
    suspend fun seedShowcase()

    suspend fun clearShowcase()

    /**
     * Inserts a user category and transaction.
     * Returns true when this write deleted the showcase sample.
     */
    suspend fun addUserTransaction(category: Category, transaction: Transaction): Boolean
}
