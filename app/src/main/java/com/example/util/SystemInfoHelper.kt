package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.WindowManager
import com.example.model.DeviceMetrics
import com.example.model.GameItem
import com.example.model.PingRating
import com.example.util.system.InstalledAppScanner
import com.example.util.system.MemoryCacheCleaner
import com.example.util.system.NetworkPingTester
import com.example.util.system.ThermalTelemetryReader
import kotlin.math.roundToInt

/**
 * Unified facade for reading hardware telemetry, RAM, storage, battery, display hz and app lists.
 */
object SystemInfoHelper {

    fun getDeviceMetrics(context: Context, measuredPing: Int = 28): DeviceMetrics {
        // Memory Info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availableRamMb = memInfo.availMem / (1024 * 1024)
        val usedRamMb = (totalRamMb - availableRamMb).coerceAtLeast(0)
        val ramUsagePercent = if (totalRamMb > 0) {
            ((usedRamMb.toDouble() / totalRamMb.toDouble()) * 100).roundToInt()
        } else 0

        // Storage Info
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalStorageBytes = totalBlocks * blockSize
        val freeStorageBytes = availableBlocks * blockSize
        val usedStorageBytes = totalStorageBytes - freeStorageBytes

        val storageTotalGb = (totalStorageBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
        val storageFreeGb = (freeStorageBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
        val storageUsedGb = (usedStorageBytes.toDouble() / (1024 * 1024 * 1024)).toFloat()
        val storageUsagePercent = if (storageTotalGb > 0) {
            ((storageUsedGb / storageTotalGb) * 100).roundToInt()
        } else 0

        // Battery Info
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).roundToInt() else 100

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val batteryTemp = tempTenths / 10.0f

        val statusInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL
        val batteryStatus = if (isCharging) "Cargando" else "En Batería"

        // Ping Rating
        val pingRating = when {
            measuredPing <= 40 -> PingRating.EXCELLENT
            measuredPing <= 85 -> PingRating.GOOD
            measuredPing <= 140 -> PingRating.MODERATE
            else -> PingRating.POOR
        }

        // Hardware details
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val cpuUsagePercent = NativeEngineBridge.getCpuUsagePercent()
        val cpuTempCelsius = readSocTemperature(batteryTemp)

        // Display refresh rate
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display?.refreshRate?.roundToInt() ?: 60
            } catch (_: Exception) {
                60
            }
        } else {
            @Suppress("DEPRECATION")
            wm?.defaultDisplay?.refreshRate?.roundToInt() ?: 60
        }

        return DeviceMetrics(
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            usedRamMb = usedRamMb,
            ramUsagePercent = ramUsagePercent,
            cpuUsagePercent = cpuUsagePercent,
            cpuTempCelsius = cpuTempCelsius,
            batteryLevel = batteryPct,
            batteryTempCelsius = batteryTemp,
            batteryStatus = batteryStatus,
            pingMs = measuredPing,
            pingRating = pingRating,
            storageTotalGb = storageTotalGb,
            storageFreeGb = storageFreeGb,
            storageUsedGb = storageUsedGb,
            storageUsagePercent = storageUsagePercent,
            cpuCores = cpuCores,
            deviceModel = deviceModel,
            refreshRateHz = if (refreshRate in 30..240) refreshRate else 60
        )
    }

    fun readSocTemperature(fallbackBatteryTemp: Float): Float {
        return ThermalTelemetryReader.readSocTemperature(fallbackBatteryTemp)
    }

    suspend fun measureRealPing(): Int {
        return NetworkPingTester.measureRealPing()
    }

    suspend fun cleanMemoryAndCache(context: Context): Long {
        return MemoryCacheCleaner.cleanMemoryAndCache(context)
    }

    fun getInstalledAppsAndGames(context: Context): List<GameItem> {
        return InstalledAppScanner.getInstalledAppsAndGames(context)
    }
}
