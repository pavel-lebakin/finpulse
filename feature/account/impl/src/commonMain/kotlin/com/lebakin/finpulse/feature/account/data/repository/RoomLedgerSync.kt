package com.lebakin.finpulse.feature.account.data.repository

import com.lebakin.finpulse.feature.account.api.LedgerSync
import com.lebakin.finpulse.feature.account.api.SyncChange
import com.lebakin.finpulse.feature.account.api.SyncMerge
import com.lebakin.finpulse.feature.account.api.SyncOrigin
import com.lebakin.finpulse.feature.account.data.OutboxDatabase
import com.lebakin.finpulse.feature.account.data.mapper.toDomain
import com.lebakin.finpulse.feature.account.data.mapper.toEntity
import com.lebakin.finpulse.feature.account.domain.ConflictPolicy

internal class RoomLedgerSync(
    private val database: OutboxDatabase,
) : LedgerSync {
    override suspend fun enqueue(change: SyncChange): Boolean {
        if (change.origin == SyncOrigin.SHOWCASE) {
            return false
        }
        require(change.id.isNotBlank()) { "Change id is required." }
        require(change.revision > 0) { "Revision must be positive." }
        require(change.body.isNotBlank()) { "Change body is required." }
        database.outboxDao().insert(change.toEntity())
        return true
    }

    override suspend fun pending(): List<SyncChange> =
        database.outboxDao().load().map { row -> row.toDomain() }

    override suspend fun merge(remote: List<SyncChange>): SyncMerge {
        val merged = ConflictPolicy.merge(pending(), remote)
        database.outboxDao().replace(merged.kept.map { change -> change.toEntity() })
        return merged
    }
}
