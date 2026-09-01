package com.skip.FatApp.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val context: Context,
    private val weightDao: WeightDao,
    private val activityDao: ActivityDao
) {

    private val gson = Gson()

    suspend fun exportBackup(uri: Uri) {
        val weightEntries = weightDao.getAllEntries()
        val activityEntries = activityDao.getAllEntries()

        val backupData = BackupData(
            weightEntries = weightEntries,
            activityEntries = activityEntries
        )

        val json = gson.toJson(backupData)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(json)
                writer.flush()
            }
        }
    }

    suspend fun importBackup(uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalStateException("Cannot read backup file")

        val type = object : TypeToken<BackupData>() {}.type
        val backupData: BackupData = gson.fromJson(json, type)

        // Clear existing data
        weightDao.deleteAll()
        activityDao.deleteAll()

        // Insert backed up data
        for (entry in backupData.weightEntries) {
            weightDao.insert(entry)
        }

        for (entry in backupData.activityEntries) {
            activityDao.insert(entry)
        }
    }
}