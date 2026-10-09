package com.lebakin.finpulse.feature.ledger.data.repository

import com.lebakin.finpulse.core.database.openFinPulseDatabase
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.core.preferences.openShowcasePreferences
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.MoneyDirection
import com.lebakin.finpulse.feature.ledger.api.RecordOrigin
import com.lebakin.finpulse.feature.ledger.api.Transaction
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RoomLedgerRepositoryTest {
    @Test
    fun userWriteRemovesShowcaseInOneStep() = runTest {
        withRepository(today = LocalDate.parse("2026-10-29")) { repository, preferences ->
            repository.seedShowcase()
            assertTrue(preferences.observeShowcaseMode().first())
            val seeded = repository.observeTransactions().first()
            assertTrue(seeded.isNotEmpty())
            assertTrue(seeded.all { it.origin == RecordOrigin.SHOWCASE })

            val removed = repository.addUserTransaction(userCategory(), userTransaction())
            assertTrue(removed)
            assertFalse(preferences.observeShowcaseMode().first())
            val stored = repository.observeTransactions().first()
            assertEquals(listOf("user-1"), stored.map { it.id })
            assertEquals(RecordOrigin.USER, stored.single().origin)
            assertEquals(listOf("user-groceries"), repository.observeCategories().first().map { it.id })
        }
    }

    @Test
    fun clearRemovesShowcaseWithoutInserting() = runTest {
        withRepository(today = LocalDate.parse("2026-10-29")) { repository, preferences ->
            repository.seedShowcase()
            repository.clearShowcase()
            assertFalse(preferences.observeShowcaseMode().first())
            assertEquals(emptyList(), repository.observeTransactions().first())
            assertEquals(emptyList(), repository.observeCategories().first())
        }
    }

    @Test
    fun seedLeavesExistingUserRowsUntouched() = runTest {
        withRepository(today = LocalDate.parse("2026-10-29")) { repository, preferences ->
            assertFalse(repository.addUserTransaction(userCategory(), userTransaction()))
            repository.seedShowcase()
            assertFalse(preferences.observeShowcaseMode().first())
            assertEquals(listOf("user-1"), repository.observeTransactions().first().map { it.id })
        }
    }

    private suspend fun withRepository(
        today: LocalDate,
        block: suspend (RoomLedgerRepository, ShowcasePreferences) -> Unit,
    ) {
        val directory = Files.createTempDirectory("finpulse-ledger")
        val database = openFinPulseDatabase(directory.resolve("finpulse.db").toString())
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val preferences = openShowcasePreferences(
            path = directory.resolve("finpulse.preferences_pb").toString(),
            scope = scope,
        )
        val repository = RoomLedgerRepository(
            database = database,
            showcasePreferences = preferences,
            clock = LedgerClock { today },
        )
        try {
            block(repository, preferences)
        } finally {
            database.close()
            scope.cancel()
        }
    }

    private fun userCategory(): Category = Category(
        id = "user-groceries",
        name = "Groceries",
        origin = RecordOrigin.SHOWCASE,
    )

    private fun userTransaction(): Transaction = Transaction(
        id = "user-1",
        categoryId = "user-groceries",
        amountMinor = 12_000,
        currencyCode = "RUB",
        direction = MoneyDirection.EXPENSE,
        merchant = "Corner shop",
        occurredOn = LocalDate.parse("2026-10-29"),
        origin = RecordOrigin.SHOWCASE,
    )
}
