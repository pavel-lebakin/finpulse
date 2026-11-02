package com.lebakin.finpulse.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val Migration1To2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `transactions` ADD COLUMN `linesText` TEXT NOT NULL DEFAULT ''")
    }
}
