package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BoostProfile

class BoosterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_booster_prefs", Context.MODE_PRIVATE)

    fun getSavedProfile(): BoostProfile {
        val name = prefs.getString("active_profile", BoostProfile.ULTRA_TURBO.name)
        return try {
            BoostProfile.valueOf(name ?: BoostProfile.ULTRA_TURBO.name)
        } catch (_: Exception) {
            BoostProfile.ULTRA_TURBO
        }
    }

    fun saveProfile(profile: BoostProfile) {
        prefs.edit().putString("active_profile", profile.name).apply()
    }

    fun getAddedGamePackages(): Set<String> {
        return prefs.getStringSet("added_games", emptySet()) ?: emptySet()
    }

    fun addGamePackage(packageName: String) {
        val set = getAddedGamePackages().toMutableSet()
        set.add(packageName)
        prefs.edit().putStringSet("added_games", set).apply()
    }

    fun removeGamePackage(packageName: String) {
        val set = getAddedGamePackages().toMutableSet()
        set.remove(packageName)
        prefs.edit().putStringSet("added_games", set).apply()
    }

    fun getGameDriver(packageName: String): com.example.model.GraphicsDriver {
        val name = prefs.getString("driver_$packageName", com.example.model.GraphicsDriver.SYSTEM_DEFAULT.name)
        return try {
            com.example.model.GraphicsDriver.valueOf(name ?: com.example.model.GraphicsDriver.SYSTEM_DEFAULT.name)
        } catch (_: Exception) {
            com.example.model.GraphicsDriver.SYSTEM_DEFAULT
        }
    }

    fun setGameDriver(packageName: String, driver: com.example.model.GraphicsDriver) {
        prefs.edit().putString("driver_$packageName", driver.name).apply()
    }

    fun getGameHibernateGoogle(packageName: String): Boolean {
        return prefs.getBoolean("hib_google_$packageName", false)
    }

    fun setGameHibernateGoogle(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("hib_google_$packageName", enabled).apply()
    }

    fun getGameDeepHibernate(packageName: String): Boolean {
        return prefs.getBoolean("deep_hib_$packageName", true)
    }

    fun setGameDeepHibernate(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("deep_hib_$packageName", enabled).apply()
    }

    fun isGoogleServicesSuspended(): Boolean {
        return prefs.getBoolean("gms_suspended_state", false)
    }

    fun setGoogleServicesSuspended(suspended: Boolean) {
        prefs.edit().putBoolean("gms_suspended_state", suspended).apply()
    }

    fun getActiveBoostedPackage(): String? {
        return prefs.getString("active_boosted_package", null)
    }

    fun setActiveBoostedPackage(packageName: String?) {
        prefs.edit().putString("active_boosted_package", packageName).apply()
    }

    fun isOverlayHudEnabled(): Boolean {
        return prefs.getBoolean("global_overlay_hud", true)
    }

    fun setOverlayHudEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("global_overlay_hud", enabled).apply()
    }

    fun getGameOverlayHud(packageName: String): Boolean {
        return prefs.getBoolean("overlay_hud_$packageName", true)
    }

    fun setGameOverlayHud(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("overlay_hud_$packageName", enabled).apply()
    }

    fun getGameDisplayScale(packageName: String): com.example.model.DisplayResolutionScale {
        val name = prefs.getString("scale_$packageName", com.example.model.DisplayResolutionScale.NATIVE_100.name)
        return try {
            com.example.model.DisplayResolutionScale.valueOf(name ?: com.example.model.DisplayResolutionScale.NATIVE_100.name)
        } catch (_: Exception) {
            com.example.model.DisplayResolutionScale.NATIVE_100
        }
    }

    fun setGameDisplayScale(packageName: String, scale: com.example.model.DisplayResolutionScale) {
        prefs.edit().putString("scale_$packageName", scale.name).apply()
    }

    fun isCustomDisplayScaleActive(): Boolean {
        return prefs.getBoolean("custom_display_scale_active", false)
    }

    fun setCustomDisplayScaleActive(active: Boolean) {
        prefs.edit().putBoolean("custom_display_scale_active", active).apply()
    }

    fun getPhysicalDisplayWidth(): Int {
        return prefs.getInt("physical_display_w", 0)
    }

    fun setPhysicalDisplayWidth(w: Int) {
        prefs.edit().putInt("physical_display_w", w).apply()
    }

    fun getPhysicalDisplayHeight(): Int {
        return prefs.getInt("physical_display_h", 0)
    }

    fun setPhysicalDisplayHeight(h: Int) {
        prefs.edit().putInt("physical_display_h", h).apply()
    }

    fun getPhysicalDisplayDensity(): Int {
        return prefs.getInt("physical_display_density", 0)
    }

    fun setPhysicalDisplayDensity(d: Int) {
        prefs.edit().putInt("physical_display_density", d).apply()
    }

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
