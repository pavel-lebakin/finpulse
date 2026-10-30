package com.lebakin.finpulse.feature.account.api

data class SyncConflict(
    val local: SyncChange,
    val remote: SyncChange,
)

data class SyncMerge(
    val kept: List<SyncChange>,
    val conflicts: List<SyncConflict>,
)
