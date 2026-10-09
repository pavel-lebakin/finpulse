package com.lebakin.finpulse.feature.ledger.api

import kotlinx.coroutines.flow.Flow

interface LedgerReader {
    fun observeTransactions(): Flow<List<Transaction>>

    fun observeCategories(): Flow<List<Category>>
}
