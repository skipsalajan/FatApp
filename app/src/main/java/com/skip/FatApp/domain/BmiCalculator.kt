package com.skip.FatApp.domain

import java.util.Locale

enum class BmiCategory(
    val label: String,
    val rangeLabel: String
) {
    UNDERWEIGHT(
        label = "Underweight",
        rangeLabel = "Below 18.5"
    ),

    HEALTHY_WEIGHT(
        label = "Healthy weight",
        rangeLabel = "18.5 – 24.9"
    ),

    OVERWEIGHT(
        label = "Overweight",
        rangeLabel = "25.0 – 29.9"
    ),

    OBESITY_CLASS_I(
        label = "Obesity class I",
        rangeLabel = "30.0 – 34.9"
    ),

    OBESITY_CLASS_II(
        label = "Obesity class II",
        rangeLabel = "35.0 – 39.9"
    ),

    OBESITY_CLASS_III(
        label = "Obesity class III",
        rangeLabel = "40.0 or higher"
    )
}

data class BmiResult(
    val value: Double,
    val category: BmiCategory
)

object BmiCalculator {

    fun calculate(
        weightKg: Double,
        heightCm: Double
    ): BmiResult? {
        if (weightKg <= 0.0 || heightCm <= 0.0) {
            return null
        }

        val heightMetres = heightCm / 100.0
        val bmi = weightKg / (heightMetres * heightMetres)

        return BmiResult(
            value = bmi,
            category = categoryFor(bmi)
        )
    }

    fun categoryFor(bmi: Double): BmiCategory {
        return when {
            bmi < 18.5 -> BmiCategory.UNDERWEIGHT
            bmi < 25.0 -> BmiCategory.HEALTHY_WEIGHT
            bmi < 30.0 -> BmiCategory.OVERWEIGHT
            bmi < 35.0 -> BmiCategory.OBESITY_CLASS_I
            bmi < 40.0 -> BmiCategory.OBESITY_CLASS_II
            else -> BmiCategory.OBESITY_CLASS_III
        }
    }

    fun format(value: Double): String {
        return String.format(
            Locale.getDefault(),
            "%.1f",
            value
        )
    }
}