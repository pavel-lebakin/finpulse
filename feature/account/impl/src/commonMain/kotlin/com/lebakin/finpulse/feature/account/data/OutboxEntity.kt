package com.lebakin.finpulse.feature.account.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val rowKey: String,
    val recordId: String,
    val kind: String,
    val origin: String,
    val revision: Long,
    val body: String,
)
