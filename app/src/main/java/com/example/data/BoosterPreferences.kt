package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.preferences.BoosterStatsPreferences
import com.example.data.preferences.CrosshairPreferences
import com.example.data.preferences.GameConfigPreferences
import com.example.data.preferences.GamerDndPreferences
import com.example.data.preferences.HibernationPreferences
import com.example.data.preferences.NetworkPreferences
import com.example.data.preferences.TouchPreferences
import com.example.model.BoostProfile
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver

/**
 * Unified preferences facade delegating to specialized domain preference managers.
 */
class BoosterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_booster_prefs", Context.MODE_PRIVATE)

    val gameConfig = GameConfigPreferences(prefs)
    val gamerDnd = GamerDndPreferences(prefs)
    val hibernation = HibernationPreferences(prefs)
    val touch = TouchPreferences(prefs)
    val network = NetworkPreferences(prefs)
    val crosshair = CrosshairPreferences(prefs)
    val stats = BoosterStatsPreferences(prefs)

    // ==========================================
    // GAME CONFIGURATION & RESOLUTION
    // ==========================================
    fun getSavedProfile(): BoostProfile = gameConfig.getSavedProfile()
    fun saveProfile(profile: BoostProfile) = gameConfig.saveProfile(profile)

    fun getAddedGamePackages(): Set<String> = gameConfig.getAddedGamePackages()
    fun addGamePackage(packageName: String) = gameConfig.addGamePackage(packageName)
    fun removeGamePackage(packageName: String) = gameConfig.removeGamePackage(packageName)

    fun getGameDriver(packageName: String): GraphicsDriver = gameConfig.getGameDriver(packageName)
    fun setGameDriver(packageName: String, driver: GraphicsDriver) = gameConfig.setGameDriver(packageName, driver)

    fun getGameHibernateGoogle(packageName: String): Boolean = gameConfig.getGameHibernateGoogle(packageName)
    fun setGameHibernateGoogle(packageName: String, enabled: Boolean) = gameConfig.setGameHibernateGoogle(packageName, enabled)

    fun getGameDeepHibernate(packageName: String): Boolean = gameConfig.getGameDeepHibernate(packageName)
    fun setGameDeepHibernate(packageName: String, enabled: Boolean) = gameConfig.setGameDeepHibernate(packageName, enabled)

    fun isGoogleServicesSuspended(): Boolean = gameConfig.isGoogleServicesSuspended()
    fun setGoogleServicesSuspended(suspended: Boolean) = gameConfig.setGoogleServicesSuspended(suspended)

    fun getActiveBoostedPackage(): String? = gameConfig.getActiveBoostedPackage()
    fun setActiveBoostedPackage(packageName: String?) = gameConfig.setActiveBoostedPackage(packageName)

    fun isOverlayHudEnabled(): Boolean = gameConfig.isOverlayHudEnabled()
    fun setOverlayHudEnabled(enabled: Boolean) = gameConfig.setOverlayHudEnabled(enabled)

    fun getGameOverlayHud(packageName: String): Boolean = gameConfig.getGameOverlayHud(packageName)
    fun setGameOverlayHud(packageName: String, enabled: Boolean) = gameConfig.setGameOverlayHud(packageName, enabled)

    fun getGameDisplayScale(packageName: String): DisplayResolutionScale = gameConfig.getGameDisplayScale(packageName)
    fun setGameDisplayScale(packageName: String, scale: DisplayResolutionScale) = gameConfig.setGameDisplayScale(packageName, scale)

    fun isCustomDisplayScaleActive(): Boolean = gameConfig.isCustomDisplayScaleActive()
    fun setCustomDisplayScaleActive(active: Boolean) = gameConfig.setCustomDisplayScaleActive(active)

    fun getPhysicalDisplayWidth(): Int = gameConfig.getPhysicalDisplayWidth()
    fun setPhysicalDisplayWidth(w: Int) = gameConfig.setPhysicalDisplayWidth(w)

    fun getPhysicalDisplayHeight(): Int = gameConfig.getPhysicalDisplayHeight()
    fun setPhysicalDisplayHeight(h: Int) = gameConfig.setPhysicalDisplayHeight(h)

    fun getPhysicalDisplayDensity(): Int = gameConfig.getPhysicalDisplayDensity()
    fun setPhysicalDisplayDensity(d: Int) = gameConfig.setPhysicalDisplayDensity(d)

    // ==========================================
    // STATS
    // ==========================================
    fun getBoostCount(): Int = stats.getBoostCount()
    fun incrementBoostCount() = stats.incrementBoostCount()
    fun getTotalMemoryFreedMb(): Long = stats.getTotalMemoryFreedMb()
    fun addMemoryFreedMb(mb: Long) = stats.addMemoryFreedMb(mb)

    // ==========================================
    // GAMER DND
    // ==========================================
    fun getGameDndEnabled(packageName: String): Boolean = gamerDnd.getGameDndEnabled(packageName)
    fun setGameDndEnabled(packageName: String, enabled: Boolean) = gamerDnd.setGameDndEnabled(packageName, enabled)

    fun isDndActive(): Boolean = gamerDnd.isDndActive()
    fun setDndActive(active: Boolean) = gamerDnd.setDndActive(active)

    fun getDndAllowCalls(): Boolean = gamerDnd.getDndAllowCalls()
    fun setDndAllowCalls(allow: Boolean) = gamerDnd.setDndAllowCalls(allow)

    fun getDndBlockHeadsUp(): Boolean = gamerDnd.getDndBlockHeadsUp()
    fun setDndBlockHeadsUp(block: Boolean) = gamerDnd.setDndBlockHeadsUp(block)

    fun getDndExceptions(): Set<String> = gamerDnd.getDndExceptions()
    fun setDndExceptions(pkgs: Set<String>) = gamerDnd.setDndExceptions(pkgs)
    fun addDndException(pkg: String) = gamerDnd.addDndException(pkg)
    fun removeDndException(pkg: String) = gamerDnd.removeDndException(pkg)

    fun getSavedZenMode(): Int = gamerDnd.getSavedZenMode()
    fun setSavedZenMode(mode: Int) = gamerDnd.setSavedZenMode(mode)

    fun getSavedHeadsUp(): Int = gamerDnd.getSavedHeadsUp()
    fun setSavedHeadsUp(state: Int) = gamerDnd.setSavedHeadsUp(state)

    // ==========================================
    // HIBERNATION
    // ==========================================
    fun getHibernationExceptions(): Set<String> = hibernation.getHibernationExceptions()
    fun setHibernationExceptions(pkgs: Set<String>) = hibernation.setHibernationExceptions(pkgs)
    fun addHibernationException(pkg: String) = hibernation.addHibernationException(pkg)
    fun removeHibernationException(pkg: String) = hibernation.removeHibernationException(pkg)

    fun getHibernationCustomTargets(): Set<String> = hibernation.getHibernationCustomTargets()
    fun setHibernationCustomTargets(pkgs: Set<String>) = hibernation.setHibernationCustomTargets(pkgs)
    fun addHibernationTarget(pkg: String) = hibernation.addHibernationTarget(pkg)
    fun addHibernationCustomTarget(pkg: String) = hibernation.addHibernationTarget(pkg)
    fun removeHibernationTarget(pkg: String) = hibernation.removeHibernationTarget(pkg)
    fun removeHibernationCustomTarget(pkg: String) = hibernation.removeHibernationTarget(pkg)

    fun getCurrentlyHibernatedPackages(): Set<String> = hibernation.getCurrentlyHibernatedPackages()
    fun setCurrentlyHibernatedPackages(pkgs: Set<String>) = hibernation.setCurrentlyHibernatedPackages(pkgs)

    // ==========================================
    // TOUCH BOOST
    // ==========================================
    fun getGameTouchBoost(packageName: String): Boolean = touch.getGameTouchBoost(packageName)
    fun setGameTouchBoost(packageName: String, enabled: Boolean) = touch.setGameTouchBoost(packageName, enabled)

    fun isTouchBoostActive(): Boolean = touch.isTouchBoostActive()
    fun setTouchBoostActive(active: Boolean) = touch.setTouchBoostActive(active)

    fun getTouchPointerSpeed(): Int = touch.getTouchPointerSpeed()
    fun setTouchPointerSpeed(speed: Int) = touch.setTouchPointerSpeed(speed)

    fun getTouchMaxHz(): Boolean = touch.getTouchMaxHz()
    fun setTouchMaxHz(enabled: Boolean) = touch.setTouchMaxHz(enabled)

    fun getTouchZeroAnimation(): Boolean = touch.getTouchZeroAnimation()
    fun setTouchZeroAnimation(enabled: Boolean) = touch.setTouchZeroAnimation(enabled)

    fun getSavedPointerSpeed(): Int = touch.getSavedPointerSpeed()
    fun setSavedPointerSpeed(speed: Int) = touch.setSavedPointerSpeed(speed)

    fun getSavedMinRefreshRate(): Float = touch.getSavedMinRefreshRate()
    fun setSavedMinRefreshRate(rate: Float) = touch.setSavedMinRefreshRate(rate)

    fun getSavedPeakRefreshRate(): Float = touch.getSavedPeakRefreshRate()
    fun setSavedPeakRefreshRate(rate: Float) = touch.setSavedPeakRefreshRate(rate)

    fun getSavedLongPressTimeout(): Int = touch.getSavedLongPressTimeout()
    fun setSavedLongPressTimeout(timeout: Int) = touch.setSavedLongPressTimeout(timeout)

    fun getSavedWindowAnimScale(): Float = touch.getSavedWindowAnimScale()
    fun setSavedWindowAnimScale(scale: Float) = touch.setSavedWindowAnimScale(scale)

    // ==========================================
    // WI-FI OPTIMIZATION
    // ==========================================
    fun getGameWifiHighPerf(packageName: String): Boolean = network.getGameWifiHighPerf(packageName)
    fun setGameWifiHighPerf(packageName: String, enabled: Boolean) = network.setGameWifiHighPerf(packageName, enabled)

    fun isWifiHighPerfActive(): Boolean = network.isWifiHighPerfActive()
    fun setWifiHighPerfActive(active: Boolean) = network.setWifiHighPerfActive(active)

    fun getSavedWifiSuspendOpt(): Int = network.getSavedWifiSuspendOpt()
    fun setSavedWifiSuspendOpt(value: Int) = network.setSavedWifiSuspendOpt(value)

    // ==========================================
    // CROSSHAIR
    // ==========================================
    fun getGameCrosshairEnabled(packageName: String): Boolean = crosshair.getGameCrosshairEnabled(packageName)
    fun setGameCrosshairEnabled(packageName: String, enabled: Boolean) = crosshair.setGameCrosshairEnabled(packageName, enabled)

    fun isGlobalCrosshairActive(): Boolean = crosshair.isGlobalCrosshairActive()
    fun setGlobalCrosshairActive(active: Boolean) = crosshair.setGlobalCrosshairActive(active)

    fun getCrosshairStyle(): String = crosshair.getCrosshairStyle()
    fun setCrosshairStyle(style: String) = crosshair.setCrosshairStyle(style)

    fun getCrosshairColor(): Long = crosshair.getCrosshairColor()
    fun setCrosshairColor(color: Long) = crosshair.setCrosshairColor(color)

    fun getCrosshairSize(): Int = crosshair.getCrosshairSize()
    fun setCrosshairSize(size: Int) = crosshair.setCrosshairSize(size)
}
