package com.skip.FatApp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skip.FatApp.data.WeightDao
import com.skip.FatApp.data.WeightEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightViewModel(
    private val weightDao: WeightDao
) : ViewModel() {

    val entries: StateFlow<List<WeightEntry>> =
        weightDao.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveWeight(weightText: String, dateMillis: Long) {
        val normalisedText = weightText.replace(',', '.')
        val weight = normalisedText.toDoubleOrNull()

        if (weight == null || weight <= 0.0 || weight > 500.0) {
            return
        }

        viewModelScope.launch {
            weightDao.insert(
                WeightEntry(
                    dateMillis = dateMillis,
                    weightKg = weight
                )
            )
        }
    }
    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.delete(entry)
        }
    }
}