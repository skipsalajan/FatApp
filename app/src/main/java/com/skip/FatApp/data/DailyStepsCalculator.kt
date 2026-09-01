package com.skip.FatApp.data

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PREFS_NAME = "fatapp_steps_daily"
private const val KEY_BASELINE_COUNTER = "baseline_counter"
private const val KEY_BASELINE_DATE = "baseline_date"

object DailyStepsCalculator {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTodaySteps(
        context: Context,
        currentCounter: Long
    ): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now().format(dateFormatter)
        val storedDate = prefs.getString(KEY_BASELINE_DATE, null)
        val baselineCounter = prefs.getLong(KEY_BASELINE_COUNTER, -1L)

        // No baseline yet
        if (baselineCounter < 0L || storedDate == null) {
            setBaseline(context, currentCounter, today)
            return 0
        }

        // New day → reset baseline
        if (storedDate != today) {
            setBaseline(context, currentCounter, today)
            return 0
        }

        // Counter reset (e.g. reboot)
        if (currentCounter < baselineCounter) {
            setBaseline(context, currentCounter, today)
            return 0
        }

        val steps = (currentCounter - baselineCounter).toInt()
        return if (steps < 0) 0 else steps
    }

    fun resetBaselineForNewDay(context: Context, currentCounter: Long) {
        val today = LocalDate.now().format(dateFormatter)
        setBaseline(context, currentCounter, today)
    }

    private fun setBaseline(context: Context, counter: Long, date: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_BASELINE_COUNTER, counter)
            .putString(KEY_BASELINE_DATE, date)
            .apply()
    }
}