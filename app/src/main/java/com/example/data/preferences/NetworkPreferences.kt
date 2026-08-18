package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles Wi-Fi high performance and latency anti-jitter preferences.
 */
class NetworkPreferences(private val prefs: SharedPreferences) {

    fun getGameWifiHighPerf(packageName: String): Boolean {
        return prefs.getBoolean("wifi_high_perf_$packageName", true)
    }

    fun setGameWifiHighPerf(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("wifi_high_perf_$packageName", enabled).apply()
    }

    fun isWifiHighPerfActive(): Boolean {
        return prefs.getBoolean("wifi_high_perf_active", false)
    }

    fun setWifiHighPerfActive(active: Boolean) {
        prefs.edit().putBoolean("wifi_high_perf_active", active).apply()
    }

    fun getSavedWifiSuspendOpt(): Int {
        return prefs.getInt("saved_wifi_suspend_opt", -1)
    }

    fun setSavedWifiSuspendOpt(value: Int) {
        prefs.edit().putInt("saved_wifi_suspend_opt", value).apply()
    }
}
