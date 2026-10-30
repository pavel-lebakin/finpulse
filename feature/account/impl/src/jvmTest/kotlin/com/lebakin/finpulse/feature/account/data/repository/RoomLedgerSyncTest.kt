package com.lebakin.finpulse.feature.account.data.repository

import com.lebakin.finpulse.feature.account.api.SyncChange
import com.lebakin.finpulse.feature.account.api.SyncKind
import com.lebakin.finpulse.feature.account.api.SyncOrigin
import com.lebakin.finpulse.feature.account.data.openOutboxDatabase
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class RoomLedgerSyncTest {
    @Test
    fun showcaseIsNotStored() = runTest {
        withSync { sync ->
            val stored = sync.enqueue(change(origin = SyncOrigin.SHOWCASE))
            assertFalse(stored)
            assertTrue(sync.pending().isEmpty())
        }
    }

    @Test
    fun enqueueReplacesTheSameChange() = runTest {
        withSync { sync ->
            assertTrue(sync.enqueue(change(body = "18.00")))
            assertTrue(sync.enqueue(change(revision = 2, body = "19.00")))
            assertEquals(listOf(change(revision = 2, body = "19.00")), sync.pending())
        }
    }

    @Test
    fun mergeKeepsTheNewerCategoryAndLeavesATransactionConflict() = runTest {
        withSync { sync ->
            val category = change(id = "food", kind = SyncKind.CATEGORY, revision = 1, body = "Groceries")
            val transaction = change(revision = 1, body = "18.00")
            sync.enqueue(category)
            sync.enqueue(transaction)
            val merged = sync.merge(
                remote = listOf(
                    category.copy(revision = 4, body = "Food"),
                    transaction.copy(revision = 3, body = "20.00"),
                    change(id = "sample", origin = SyncOrigin.SHOWCASE),
                ),
            )
            assertEquals(category.copy(revision = 4, body = "Food"), merged.kept.first())
            assertEquals(transaction, merged.conflicts.single().local)
            assertEquals(
                listOf(category.copy(revision = 4, body = "Food"), transaction),
                sync.pending(),
            )
        }
    }

    private suspend fun withSync(block: suspend (RoomLedgerSync) -> Unit) {
        val directory = Files.createTempDirectory("finpulse-outbox")
        val database = openOutboxDatabase(directory.resolve("finpulse-outbox.db").toString())
        try {
            block(RoomLedgerSync(database))
        } finally {
            database.close()
        }
    }

    private fun change(
        id: String = "tx-1",
        kind: SyncKind = SyncKind.TRANSACTION,
        origin: SyncOrigin = SyncOrigin.USER,
        revision: Long = 1,
        body: String = "18.00",
    ): SyncChange = SyncChange(
        id = id,
        kind = kind,
        origin = origin,
        revision = revision,
        body = body,
    )
}
