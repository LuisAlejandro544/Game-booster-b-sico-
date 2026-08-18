package com.example.data.preferences

import android.content.SharedPreferences

/**
 * Handles Touch Boost sensitivity settings, maximum refresh rate (Hz), and animation scales.
 */
class TouchPreferences(private val prefs: SharedPreferences) {

    fun getGameTouchBoost(packageName: String): Boolean {
        return prefs.getBoolean("touch_boost_$packageName", true)
    }

    fun setGameTouchBoost(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("touch_boost_$packageName", enabled).apply()
    }

    fun isTouchBoostActive(): Boolean {
        return prefs.getBoolean("touch_boost_active", false)
    }

    fun setTouchBoostActive(active: Boolean) {
        prefs.edit().putBoolean("touch_boost_active", active).apply()
    }

    fun getTouchPointerSpeed(): Int {
        return prefs.getInt("touch_pointer_speed", 7)
    }

    fun setTouchPointerSpeed(speed: Int) {
        prefs.edit().putInt("touch_pointer_speed", speed).apply()
    }

    fun getTouchMaxHz(): Boolean {
        return prefs.getBoolean("touch_max_hz", true)
    }

    fun setTouchMaxHz(enabled: Boolean) {
        prefs.edit().putBoolean("touch_max_hz", enabled).apply()
    }

    fun getTouchZeroAnimation(): Boolean {
        return prefs.getBoolean("touch_zero_anim", true)
    }

    fun setTouchZeroAnimation(enabled: Boolean) {
        prefs.edit().putBoolean("touch_zero_anim", enabled).apply()
    }

    fun getSavedPointerSpeed(): Int {
        return prefs.getInt("saved_pointer_speed", -1)
    }

    fun setSavedPointerSpeed(speed: Int) {
        prefs.edit().putInt("saved_pointer_speed", speed).apply()
    }

    fun getSavedMinRefreshRate(): Float {
        return prefs.getFloat("saved_min_refresh", -1f)
    }

    fun setSavedMinRefreshRate(rate: Float) {
        prefs.edit().putFloat("saved_min_refresh", rate).apply()
    }

    fun getSavedPeakRefreshRate(): Float {
        return prefs.getFloat("saved_peak_refresh", -1f)
    }

    fun setSavedPeakRefreshRate(rate: Float) {
        prefs.edit().putFloat("saved_peak_refresh", rate).apply()
    }

    fun getSavedLongPressTimeout(): Int {
        return prefs.getInt("saved_long_press_timeout", -1)
    }

    fun setSavedLongPressTimeout(timeout: Int) {
        prefs.edit().putInt("saved_long_press_timeout", timeout).apply()
    }

    fun getSavedWindowAnimScale(): Float {
        return prefs.getFloat("saved_window_anim_scale", -1f)
    }

    fun setSavedWindowAnimScale(scale: Float) {
        prefs.edit().putFloat("saved_window_anim_scale", scale).apply()
    }
}
