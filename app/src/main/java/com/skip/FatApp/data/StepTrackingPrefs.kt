package com.skip.FatApp.data

import android.content.Context

private const val PREFS_NAME = "fatapp_prefs"
private const val KEY_STEP_TRACKING = "step_tracking_enabled"

object StepTrackingPrefs {

    fun setStepTrackingEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_STEP_TRACKING, enabled).apply()
    }

    fun isStepTrackingEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STEP_TRACKING, false)
    }
}