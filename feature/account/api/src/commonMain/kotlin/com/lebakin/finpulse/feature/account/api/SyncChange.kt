package com.lebakin.finpulse.feature.account.api

data class SyncChange(
    val id: String,
    val kind: SyncKind,
    val origin: SyncOrigin,
    val revision: Long,
    val body: String,
)
