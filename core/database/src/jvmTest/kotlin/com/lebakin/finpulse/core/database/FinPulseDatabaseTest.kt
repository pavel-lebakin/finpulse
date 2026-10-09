package com.lebakin.finpulse.core.database

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class FinPulseDatabaseTest {
    @Test
    fun storesACategoryAndATransaction() = runTest {
        val directory = Files.createTempDirectory("finpulse-db")
        val database = openFinPulseDatabase(directory.resolve("finpulse.db").toString())
        try {
            database.ledgerDao().insertCategory(
                CategoryEntity(id = "groceries", name = "Groceries", origin = "USER"),
            )
            database.ledgerDao().insertTransaction(
                TransactionEntity(
                    id = "tx-1",
                    categoryId = "groceries",
                    amountMinor = 18_645,
                    currencyCode = "RUB",
                    direction = "EXPENSE",
                    merchant = "Market",
                    occurredOn = "2026-10-29",
                    origin = "USER",
                ),
            )
            val stored = database.ledgerDao().observeTransactions().first()
            assertEquals(listOf("tx-1"), stored.map { it.id })
            assertEquals(1, database.ledgerDao().countCategories("USER"))
        } finally {
            database.close()
        }
    }
}
