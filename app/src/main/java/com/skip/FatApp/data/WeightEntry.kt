package com.skip.FatApp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val dateMillis: Long,

    val weightKg: Double
)