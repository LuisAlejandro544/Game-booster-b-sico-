package com.example.util.shizuku

import android.content.Context
import android.util.Log
import com.example.data.BoosterPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controller for Real-Time Wi-Fi & Network Latency Stabilization (Anti-Jitter).
 *
 * Prevents Wi-Fi chip power-saving sleep cycles during online gaming sessions to minimize
 * packet drops and ping spikes (`settings put global wifi_suspend_optimizations_enabled 0`).
 *
 * Guarantees automated restoration upon game exit.
 */
object NetworkOptimizerController {
    private const val TAG = "NetworkOptimizerController"

    suspend fun applyWifiHighPerf(
        context: Context,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        if (!isAuthorized) {
            logs.add("ℹ️ Requiere autorización Shizuku para optimización profunda de latencia Wi-Fi")
            return@withContext logs
        }

        try {
            if (prefs.getSavedWifiSuspendOpt() == -1) {
                val currVal = AdbShellExecutor.executeCommand("settings get global wifi_suspend_optimizations_enabled", true)
                val optVal = currVal.output.trim().toIntOrNull() ?: 1
                prefs.setSavedWifiSuspendOpt(optVal)
            }

            // Disable Wi-Fi suspend power-saving optimizations while gaming
            val res = AdbShellExecutor.executeCommand("settings put global wifi_suspend_optimizations_enabled 0", true)
            if (res.isSuccess) {
                logs.add("📶 Modo Wi-Fi Ultra-Baja Latencia activado (Suspensión de chip Wi-Fi desactivada)")
            }

            prefs.setWifiHighPerfActive(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying Wi-Fi high performance mode", e)
        }

        logs
    }

    suspend fun restoreWifiSettings(
        context: Context,
        isAuthorized: Boolean
    ): List<String> = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val prefs = BoosterPreferences(context)

        if (!isAuthorized) return@withContext logs

        try {
            val savedVal = prefs.getSavedWifiSuspendOpt()
            val targetVal = if (savedVal != -1) savedVal else 1
            AdbShellExecutor.executeCommand("settings put global wifi_suspend_optimizations_enabled $targetVal", true)

            prefs.setWifiHighPerfActive(false)
            prefs.setSavedWifiSuspendOpt(-1)
            logs.add("✓ Modo de ahorro de energía Wi-Fi restaurado a la normalidad")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring Wi-Fi settings", e)
        }

        logs
    }
}
