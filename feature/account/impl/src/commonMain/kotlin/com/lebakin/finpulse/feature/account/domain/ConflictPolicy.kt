package com.lebakin.finpulse.feature.account.domain

import com.lebakin.finpulse.feature.account.api.SyncChange
import com.lebakin.finpulse.feature.account.api.SyncConflict
import com.lebakin.finpulse.feature.account.api.SyncKind
import com.lebakin.finpulse.feature.account.api.SyncMerge
import com.lebakin.finpulse.feature.account.api.SyncOrigin

internal object ConflictPolicy {
    fun merge(local: List<SyncChange>, remote: List<SyncChange>): SyncMerge {
        val localRows = userRows(local)
        val remoteRows = userRows(remote)
        val keys = (localRows.keys + remoteRows.keys).sortedWith(compareBy({ it.kind }, { it.id }))
        val kept = mutableListOf<SyncChange>()
        val conflicts = mutableListOf<SyncConflict>()
        keys.forEach { key ->
            val left = localRows[key]
            val right = remoteRows[key]
            when {
                left == null && right != null -> kept += right
                left != null && right == null -> kept += left
                left != null && right != null -> decide(left, right, kept, conflicts)
            }
        }
        return SyncMerge(kept = kept, conflicts = conflicts)
    }

    private fun decide(
        local: SyncChange,
        remote: SyncChange,
        kept: MutableList<SyncChange>,
        conflicts: MutableList<SyncConflict>,
    ) {
        when (local.kind) {
            SyncKind.CATEGORY -> kept += if (remote.revision > local.revision) remote else local
            SyncKind.TRANSACTION -> {
                kept += local
                if (local.revision != remote.revision || local.body != remote.body) {
                    conflicts += SyncConflict(local = local, remote = remote)
                }
            }
        }
    }

    private fun userRows(changes: List<SyncChange>): Map<SyncKey, SyncChange> =
        changes
            .filter { change ->
                change.origin == SyncOrigin.USER &&
                    change.id.isNotBlank() &&
                    change.revision > 0 &&
                    change.body.isNotBlank()
            }
            .associateBy { change -> SyncKey(kind = change.kind, id = change.id) }

    private data class SyncKey(
        val kind: SyncKind,
        val id: String,
    )
}
