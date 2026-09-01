package com.skip.FatApp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityEntry)

    @Delete
    suspend fun delete(entry: ActivityEntry)

    @Query("SELECT * FROM activity_entries ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<ActivityEntry>>

    @Query(
        """
    SELECT * FROM activity_entries
    WHERE dateMillis = :dateMillis
    ORDER BY id DESC
    """
    )
    suspend fun getEntriesForDate(dateMillis: Long): List<ActivityEntry>

    @Query("DELETE FROM activity_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM activity_entries ORDER BY dateMillis ASC, id ASC")
    suspend fun getAllEntries(): List<ActivityEntry>

    @Query(
        """
    SELECT * FROM activity_entries
    WHERE type = 'WALKING'
      AND dateMillis >= :dayStartMillis
      AND dateMillis < :dayEndMillis
    ORDER BY id DESC
    """
    )
    fun getWalkingEntriesForDay(dayStartMillis: Long, dayEndMillis: Long): List<ActivityEntry>
}