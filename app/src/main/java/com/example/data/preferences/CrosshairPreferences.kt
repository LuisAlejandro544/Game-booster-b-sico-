package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles gamer tactical crosshair overlay preferences (style, color, size, activation).
 */
class CrosshairPreferences(private val prefs: SharedPreferences) {

    fun getGameCrosshairEnabled(packageName: String): Boolean {
        return prefs.getBoolean("crosshair_enabled_$packageName", false)
    }

    fun setGameCrosshairEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("crosshair_enabled_$packageName", enabled).apply()
    }

    fun isGlobalCrosshairActive(): Boolean {
        return prefs.getBoolean("global_crosshair_active", false)
    }

    fun setGlobalCrosshairActive(active: Boolean) {
        prefs.edit().putBoolean("global_crosshair_active", active).apply()
    }

    fun getCrosshairStyle(): String {
        return prefs.getString("crosshair_style", "CROSS") ?: "CROSS"
    }

    fun setCrosshairStyle(style: String) {
        prefs.edit().putString("crosshair_style", style).apply()
    }

    fun getCrosshairColor(): Long {
        return prefs.getLong("crosshair_color", 0xFF00F0FFL) // NeonCyan
    }

    fun setCrosshairColor(color: Long) {
        prefs.edit().putLong("crosshair_color", color).apply()
    }

    fun getCrosshairSize(): Int {
        return prefs.getInt("crosshair_size", 24)
    }

    fun setCrosshairSize(size: Int) {
        prefs.edit().putInt("crosshair_size", size).apply()
    }
}
