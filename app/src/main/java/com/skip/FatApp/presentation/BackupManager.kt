package com.skip.FatApp.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.skip.FatApp.data.FatAppDatabase
import com.skip.FatApp.data.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class BackupManager(
    private val context: Context
) {

    suspend fun exportAllWeights(
        entries: List<WeightEntry>
    ): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val database = FatAppDatabase.getInstance(context.applicationContext)
                val weightDao = database.weightDao()
                val allEntries = weightDao.getAllEntries()

                val jsonArray = JSONArray()
                for (entry in allEntries) {
                    val obj = JSONObject().apply {
                        put("weightKg", entry.weightKg)
                        put("dateMillis", entry.dateMillis)
                    }
                    jsonArray.put(obj)
                }

                val root = JSONObject().apply {
                    put("version", 1)
                    put("type", "fatapp_weights")
                    put("entries", jsonArray)
                }

                val fileName = "FatAppBackup_${System.currentTimeMillis()}.json"
                val backupsDir = File(context.filesDir, "backups")
                if (!backupsDir.exists()) {
                    backupsDir.mkdirs()
                }
                val file = File(backupsDir, fileName)

                FileWriter(file).use { writer ->
                    writer.write(root.toString(2))
                }

                val authority = context.applicationContext.packageName + ".fileprovider"
                println("AUTHORITY = $authority, FILE = $file")
                val uri = FileProvider.getUriForFile(context, authority, file)
                println("URI RESULT = $uri")
                uri

            } catch (e: Exception) {
                println("EXPORT ERROR: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    fun shareFile(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, "Share backup")
        )
    }
    suspend fun importBackupFile(
        uri: android.net.Uri
    ): android.util.Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: return@withContext android.util.Pair(false, "Could not read file")

                val root = org.json.JSONObject(text)
                val type = root.optString("type")
                if (type != "fatapp_weights") {
                    return@withContext android.util.Pair(false, "Unknown backup type: $type")
                }

                val entriesArray = root.getJSONArray("entries")
                val database = com.skip.FatApp.data.FatAppDatabase.getInstance(context.applicationContext)
                val weightDao = database.weightDao()

                // Get all existing entries from the database
                val existingEntries = weightDao.getAllEntries()
                val existingByDate = existingEntries.associateBy { it.dateMillis }

                var inserted = 0
                var skipped = 0

                for (i in 0 until entriesArray.length()) {
                    val obj = entriesArray.getJSONObject(i)
                    val weightKg = obj.getDouble("weightKg")
                    val dateMillis = obj.getLong("dateMillis")

                    // If an entry with the same date already exists, skip it
                    if (existingByDate.containsKey(dateMillis)) {
                        skipped++
                        continue
                    }

                    val entry = com.skip.FatApp.data.WeightEntry(
                        weightKg = weightKg,
                        dateMillis = dateMillis
                    )

                    weightDao.insert(entry)
                    inserted++
                }

                val message = if (skipped > 0) {
                    "Imported $inserted entries, skipped $skipped duplicates,fattie :X"
                } else {
                    "Imported $inserted entries,fattie"
                }

                android.util.Pair(true, message)
            } catch (e: Exception) {
                android.util.Pair(false, e.message ?: "Unknown error")
            }
        }
    }}