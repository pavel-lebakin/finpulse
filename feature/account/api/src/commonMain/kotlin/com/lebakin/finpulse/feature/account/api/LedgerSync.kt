package com.lebakin.finpulse.feature.account.api

interface LedgerSync {
    /**
     * Stores one user change. A showcase change is refused and returns false.
     */
    suspend fun enqueue(change: SyncChange): Boolean

    suspend fun pending(): List<SyncChange>

    /**
     * Applies [remote] onto the pending outbox.
     * Categories keep the higher revision. A transaction that differs stays local and is a conflict.
     * Showcase changes are ignored.
     */
    suspend fun merge(remote: List<SyncChange>): SyncMerge
}
