package com.skip.FatApp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WeightEntry::class, ActivityEntry::class],
    version = 2,
    exportSchema = false
)
abstract class FatAppDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var instance: FatAppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS activity_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                activityName TEXT,
                dateMillis INTEGER NOT NULL,
                steps INTEGER,
                durationMinutes INTEGER,
                estimatedCalories INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }
        fun getInstance(context: Context): FatAppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FatAppDatabase::class.java,
                    "fat_app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}