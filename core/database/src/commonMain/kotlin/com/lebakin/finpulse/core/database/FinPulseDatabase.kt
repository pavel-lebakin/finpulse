package com.lebakin.finpulse.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(FinPulseDatabaseConstructor::class)
abstract class FinPulseDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
}

expect object FinPulseDatabaseConstructor : RoomDatabaseConstructor<FinPulseDatabase>
