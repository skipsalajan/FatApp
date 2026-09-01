package com.skip.FatApp.data.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectStepsRepository(
    private val context: Context
) {
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun getStepsForDate(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long = withContext(Dispatchers.IO) {
        val startOfDay = ZonedDateTime.of(date, LocalTime.MIDNIGHT, zoneId).toInstant()
        val endOfDay = ZonedDateTime.of(date, LocalTime.MAX, zoneId).toInstant()

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
        )

        val response = healthConnectClient.readRecords(request)

        var total = 0L
        for (record in response.records) {
            total += record.count
        }
        total
    }

    suspend fun getYesterdaySteps(): Long {
        val yesterday = LocalDate.now().minusDays(1)
        return getStepsForDate(yesterday)
    }

    fun isHealthConnectAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            true // built-in on Android 14+
        } else {
            val status = HealthConnectClient.getSdkStatus(context)
            // SDK_STATUS_AVAILABLE = 0 in Health Connect
            status == 0
        }
    }
}