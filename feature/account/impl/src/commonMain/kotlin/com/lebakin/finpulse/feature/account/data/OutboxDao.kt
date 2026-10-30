package com.lebakin.finpulse.feature.account.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY kind ASC, recordId ASC")
    abstract suspend fun load(): List<OutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: OutboxEntity)

    @Query("DELETE FROM outbox")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replace(rows: List<OutboxEntity>) {
        deleteAll()
        rows.forEach { row -> insert(row) }
    }
}
