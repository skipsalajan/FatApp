package com.skip.FatApp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skip.FatApp.data.ActivityDao
import com.skip.FatApp.data.ActivityEntry
import com.skip.FatApp.data.ActivityType
import com.skip.FatApp.domain.ActivityCalories
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val activityDao: ActivityDao
) : ViewModel() {

    val entries: StateFlow<List<ActivityEntry>> =
        activityDao.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveWalking(
        stepsText: String,
        dateMillis: Long
    ) {
        val steps = stepsText
            .replace(",", "")
            .replace(" ", "")
            .toIntOrNull()

        if (steps == null || steps <= 0) {
            return
        }

        val calories = ActivityCalories.walkingCalories(steps)

        viewModelScope.launch {
            activityDao.insert(
                ActivityEntry(
                    type = ActivityType.WALKING,
                    dateMillis = dateMillis,
                    steps = steps,
                    estimatedCalories = calories
                )
            )
        }
    }

    fun saveTennis(
        durationMinutes: Int,
        dateMillis: Long
    ) {
        if (durationMinutes <= 0) {
            return
        }

        val calories = ActivityCalories.tennisCalories(durationMinutes)

        viewModelScope.launch {
            activityDao.insert(
                ActivityEntry(
                    type = ActivityType.TENNIS,
                    dateMillis = dateMillis,
                    durationMinutes = durationMinutes,
                    estimatedCalories = calories
                )
            )
        }
    }

    fun saveOther(
        activityName: String,
        durationMinutes: Int,
        caloriesText: String,
        dateMillis: Long
    ) {
        val calories = caloriesText
            .replace(",", "")
            .replace(" ", "")
            .toIntOrNull()

        if (
            activityName.isBlank() ||
            durationMinutes <= 0 ||
            calories == null ||
            calories <= 0
        ) {
            return
        }

        viewModelScope.launch {
            activityDao.insert(
                ActivityEntry(
                    type = ActivityType.OTHER,
                    activityName = activityName.trim(),
                    dateMillis = dateMillis,
                    durationMinutes = durationMinutes,
                    estimatedCalories = calories
                )
            )
        }
    }

    fun deleteActivity(entry: ActivityEntry) {
        viewModelScope.launch {
            activityDao.delete(entry)
        }
    }
}