package com.skip.FatApp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skip.FatApp.data.WeightDao

class WeightViewModelFactory(
    private val weightDao: WeightDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            return WeightViewModel(weightDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}