package com.skip.FatApp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActivityType {
    WALKING,
    TENNIS,
    OTHER
}

@Entity(tableName = "activity_entries")
data class ActivityEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: ActivityType,

    val activityName: String? = null,

    val dateMillis: Long,

    val steps: Int? = null,

    val durationMinutes: Int? = null,

    val estimatedCalories: Int
)