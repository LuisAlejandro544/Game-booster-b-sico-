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

    // ==========================================
    // GAMER DND (NO MOLESTAR AUTOMATIZADO)
    // ==========================================

    fun getGameDndEnabled(packageName: String): Boolean {
        return prefs.getBoolean("dnd_enabled_$packageName", true)
    }

    fun setGameDndEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("dnd_enabled_$packageName", enabled).apply()
    }

    fun isDndActive(): Boolean {
        return prefs.getBoolean("dnd_is_active", false)
    }

    fun setDndActive(active: Boolean) {
        prefs.edit().putBoolean("dnd_is_active", active).apply()
    }

    fun getDndAllowCalls(): Boolean {
        return prefs.getBoolean("dnd_allow_calls", true)
    }

    fun setDndAllowCalls(allow: Boolean) {
        prefs.edit().putBoolean("dnd_allow_calls", allow).apply()
    }

    fun getDndBlockHeadsUp(): Boolean {
        return prefs.getBoolean("dnd_block_heads_up", true)
    }

    fun setDndBlockHeadsUp(block: Boolean) {
        prefs.edit().putBoolean("dnd_block_heads_up", block).apply()
    }

    fun getDndExceptions(): Set<String> {
        // Default exceptions: Telecom/Phone dialer, Discord, WhatsApp if user needs them
        val defaultSet = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.discord",
            "com.whatsapp"
        )
        return prefs.getStringSet("dnd_exceptions", defaultSet) ?: defaultSet
    }

    fun setDndExceptions(pkgs: Set<String>) {
        prefs.edit().putStringSet("dnd_exceptions", pkgs).apply()
    }

    fun addDndException(pkg: String) {
        val set = getDndExceptions().toMutableSet()
        set.add(pkg)
        setDndExceptions(set)
    }

    fun removeDndException(pkg: String) {
        val set = getDndExceptions().toMutableSet()
        set.remove(pkg)
        setDndExceptions(set)
    }

    fun getSavedZenMode(): Int {
        return prefs.getInt("saved_zen_mode", -1)
    }

    fun setSavedZenMode(mode: Int) {
        prefs.edit().putInt("saved_zen_mode", mode).apply()
    }

    fun getSavedHeadsUp(): Int {
        return prefs.getInt("saved_heads_up", -1)
    }

    fun setSavedHeadsUp(state: Int) {
        prefs.edit().putInt("saved_heads_up", state).apply()
    }

    // ==========================================
    // LISTA PERSONALIZADA DE HIBERNACIÓN
    // ==========================================

    /**
     * Excepciones de hibernación: Apps que NUNCA deben hibernarse (Whitelist), como Discord, Spotify, etc.
     */
    fun getHibernationExceptions(): Set<String> {
        val defaultExceptions = setOf(
            "com.discord",
            "com.spotify.music",
            "com.whatsapp",
            "org.telegram.messenger"
        )
        return prefs.getStringSet("hibernation_exceptions", defaultExceptions) ?: defaultExceptions
    }

    fun setHibernationExceptions(pkgs: Set<String>) {
        prefs.edit().putStringSet("hibernation_exceptions", pkgs).apply()
    }

    fun addHibernationException(pkg: String) {
        val set = getHibernationExceptions().toMutableSet()
        set.add(pkg)
        setHibernationExceptions(set)
    }

    fun removeHibernationException(pkg: String) {
        val set = getHibernationExceptions().toMutableSet()
        set.remove(pkg)
        setHibernationExceptions(set)
    }

    /**
     * Lista negra explícita de apps que el usuario desea hibernar específicamente al jugar.
     */
    fun getHibernationCustomTargets(): Set<String> {
        return prefs.getStringSet("hibernation_custom_targets", emptySet()) ?: emptySet()
    }

    fun setHibernationCustomTargets(pkgs: Set<String>) {
        prefs.edit().putStringSet("hibernation_custom_targets", pkgs).apply()
    }

    fun addHibernationTarget(pkg: String) {
        val set = getHibernationCustomTargets().toMutableSet()
        set.add(pkg)
        setHibernationCustomTargets(set)
    }

    fun addHibernationCustomTarget(pkg: String) {
        addHibernationTarget(pkg)
    }

    fun removeHibernationTarget(pkg: String) {
        val set = getHibernationCustomTargets().toMutableSet()
        set.remove(pkg)
        setHibernationCustomTargets(set)
    }

    fun removeHibernationCustomTarget(pkg: String) {
        removeHibernationTarget(pkg)
    }

    /**
     * Registro de paquetes actualmente hibernados para deshibernación selectiva y en tiempo real.
     */
    fun getCurrentlyHibernatedPackages(): Set<String> {
        return prefs.getStringSet("currently_hibernated_pkgs", emptySet()) ?: emptySet()
    }

    fun setCurrentlyHibernatedPackages(pkgs: Set<String>) {
        prefs.edit().putStringSet("currently_hibernated_pkgs", pkgs).apply()
    }

    // ==========================================
    // RESPUESTA TÁCTIL Y SENSIBILIDAD (TOUCH BOOST)
    // ==========================================

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

    // ==========================================
    // OPTIMIZADOR WI-FI Y PING (ANTI-JITTER)
    // ==========================================

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

    // ==========================================
    // MIRA GAMER FLOTANTE (CROSSHAIR HUD)
    // ==========================================

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
