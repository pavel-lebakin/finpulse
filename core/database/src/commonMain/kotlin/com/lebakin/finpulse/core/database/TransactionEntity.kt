package com.lebakin.finpulse.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("occurredOn"),
        Index("origin"),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val amountMinor: Long,
    val currencyCode: String,
    val direction: String,
    val merchant: String,
    val occurredOn: String,
    val origin: String,
    @ColumnInfo(defaultValue = "''") val linesText: String = "",
)
