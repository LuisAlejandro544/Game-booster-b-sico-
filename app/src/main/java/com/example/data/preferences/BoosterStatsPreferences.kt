package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles booster run statistics (boost count and total memory freed).
 */
class BoosterStatsPreferences(private val prefs: SharedPreferences) {

    fun getBoostCount(): Int {
        return prefs.getInt("total_boosts", 0)
    }

    fun incrementBoostCount() {
        val count = getBoostCount() + 1
        prefs.edit().putInt("total_boosts", count).apply()
    }

    fun getTotalMemoryFreedMb(): Long {
        return prefs.getLong("total_freed_mb", 0L)
    }

    fun addMemoryFreedMb(mb: Long) {
        val total = getTotalMemoryFreedMb() + mb
        prefs.edit().putLong("total_freed_mb", total).apply()
    }
}
