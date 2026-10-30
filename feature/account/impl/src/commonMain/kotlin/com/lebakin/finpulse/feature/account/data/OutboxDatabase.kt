package com.lebakin.finpulse.feature.account.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [OutboxEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(OutboxDatabaseConstructor::class)
abstract class OutboxDatabase : RoomDatabase() {
    abstract fun outboxDao(): OutboxDao
}

expect object OutboxDatabaseConstructor : RoomDatabaseConstructor<OutboxDatabase>
