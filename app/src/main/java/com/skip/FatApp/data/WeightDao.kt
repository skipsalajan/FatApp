package com.skip.FatApp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM weight_entries ORDER BY dateMillis ASC, id ASC")
    suspend fun getAllEntries(): List<WeightEntry>
}