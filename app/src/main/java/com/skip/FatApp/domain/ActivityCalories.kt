package com.skip.FatApp.domain

object ActivityCalories {

    const val WALKING_KCAL_PER_STEP = 0.06546
    const val TENNIS_KCAL_PER_HOUR = 380.0

    fun walkingCalories(steps: Int): Int {
        return (steps * WALKING_KCAL_PER_STEP).toInt()
    }

    fun tennisCalories(durationMinutes: Int): Int {
        val hours = durationMinutes / 60.0
        return (hours * TENNIS_KCAL_PER_HOUR).toInt()
    }
}