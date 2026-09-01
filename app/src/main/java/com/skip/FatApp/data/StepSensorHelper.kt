package com.skip.FatApp.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepSensorHelper(
    context: Context,
    private val onStepCount: (Int) -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val stepCount = event.values[0].toInt()
                onStepCount(stepCount)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No action needed
        }
    }

    fun start() {
        if (stepSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                stepSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(sensorListener)
    }

    fun isSensorAvailable(): Boolean = stepSensor != null
}