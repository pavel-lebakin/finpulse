package com.lebakin.finpulse.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index("origin")],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val origin: String,
)
