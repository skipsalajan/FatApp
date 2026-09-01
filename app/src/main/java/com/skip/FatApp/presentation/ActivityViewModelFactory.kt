package com.skip.FatApp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skip.FatApp.data.ActivityDao

class ActivityViewModelFactory(
    private val activityDao: ActivityDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            return ActivityViewModel(activityDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}