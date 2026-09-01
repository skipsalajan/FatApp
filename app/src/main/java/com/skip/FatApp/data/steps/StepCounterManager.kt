package com.skip.FatApp.data.steps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat

class StepCounterManager(
    context: Context
) {
    private val appContext = context.applicationContext

    private val sensorManager: SensorManager? =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    fun isStepCounterAvailable(): Boolean {
        return sensorManager
            ?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    fun hasActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }
}