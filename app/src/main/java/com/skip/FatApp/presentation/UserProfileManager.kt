package com.skip.FatApp.presentation

import android.content.Context

data class UserProfile(
    val name: String,
    val heightCm: Double?
)

class UserProfileManager(
    context: Context
) {
    private val preferences = context.getSharedPreferences(
        "fatapp_profile",
        Context.MODE_PRIVATE
    )

    fun loadProfile(): UserProfile {
        val name = preferences.getString("name", "") ?: ""
        val heightCm = preferences.getFloat("height_cm", 0f)
            .takeIf { it > 0f }
            ?.toDouble()

        return UserProfile(
            name = name,
            heightCm = heightCm
        )
    }

    fun saveProfile(
        name: String,
        heightCm: Double?
    ) {
        preferences.edit()
            .putString("name", name.trim())
            .putFloat("height_cm", heightCm?.toFloat() ?: 0f)
            .apply()
    }
}