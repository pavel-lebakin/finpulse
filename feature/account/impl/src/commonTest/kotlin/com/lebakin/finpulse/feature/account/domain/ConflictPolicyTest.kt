package com.lebakin.finpulse.feature.account.domain

import com.lebakin.finpulse.feature.account.api.SyncChange
import com.lebakin.finpulse.feature.account.api.SyncKind
import com.lebakin.finpulse.feature.account.api.SyncOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConflictPolicyTest {
    @Test
    fun showcaseRowsAreIgnored() {
        val merged = ConflictPolicy.merge(
            local = listOf(change(origin = SyncOrigin.SHOWCASE)),
            remote = listOf(change(id = "remote", origin = SyncOrigin.SHOWCASE)),
        )
        assertTrue(merged.kept.isEmpty())
        assertTrue(merged.conflicts.isEmpty())
    }

    @Test
    fun categoryKeepsTheHigherRevision() {
        val local = change(kind = SyncKind.CATEGORY, revision = 1, body = "Groceries")
        val remote = change(kind = SyncKind.CATEGORY, revision = 2, body = "Food")
        val merged = ConflictPolicy.merge(local = listOf(local), remote = listOf(remote))
        assertEquals(listOf(remote), merged.kept)
        assertTrue(merged.conflicts.isEmpty())
    }

    @Test
    fun categoryKeepsLocalWhenTheRevisionIsNotHigher() {
        val local = change(kind = SyncKind.CATEGORY, revision = 2, body = "Groceries")
        val remote = change(kind = SyncKind.CATEGORY, revision = 2, body = "Food")
        val merged = ConflictPolicy.merge(local = listOf(local), remote = listOf(remote))
        assertEquals(listOf(local), merged.kept)
        assertTrue(merged.conflicts.isEmpty())
    }

    @Test
    fun transactionDifferenceStaysLocalAndIsAConflict() {
        val local = change(revision = 1, body = "18.00")
        val remote = change(revision = 2, body = "19.00")
        val merged = ConflictPolicy.merge(local = listOf(local), remote = listOf(remote))
        assertEquals(listOf(local), merged.kept)
        assertEquals(local, merged.conflicts.single().local)
        assertEquals(remote, merged.conflicts.single().remote)
    }

    @Test
    fun matchingTransactionIsNotAConflict() {
        val local = change(revision = 3, body = "18.00")
        val remote = change(revision = 3, body = "18.00")
        val merged = ConflictPolicy.merge(local = listOf(local), remote = listOf(remote))
        assertEquals(listOf(local), merged.kept)
        assertTrue(merged.conflicts.isEmpty())
    }

    @Test
    fun remoteOnlyUserChangeIsKept() {
        val remote = change(id = "rent", kind = SyncKind.CATEGORY, revision = 1, body = "Rent")
        val merged = ConflictPolicy.merge(local = emptyList(), remote = listOf(remote))
        assertEquals(listOf(remote), merged.kept)
    }

    @Test
    fun blankRemoteChangeIsDropped() {
        val remote = change(id = " ", revision = 1, body = "18.00")
        val merged = ConflictPolicy.merge(local = emptyList(), remote = listOf(remote))
        assertTrue(merged.kept.isEmpty())
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
