package com.example.util.system

import java.io.File
import kotlin.math.roundToInt

/**
 * Reads real SoC/CPU thermal readings from Linux kernel sysfs thermal zones.
 */
object ThermalTelemetryReader {

    private val THERMAL_ZONES = listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp",
        "/sys/devices/virtual/thermal/thermal_zone0/temp"
    )

    fun readSocTemperature(fallbackBatteryTemp: Float): Float {
        return try {
            for (path in THERMAL_ZONES) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val raw = file.bufferedReader().use { it.readLine() }?.trim()?.toFloatOrNull()
                    if (raw != null && raw > 0) {
                        val temp = if (raw > 1000) raw / 1000f else raw
                        if (temp in 20.0f..95.0f) {
                            return (temp * 10).roundToInt() / 10.0f
                        }
                    }
                }
            }
            fallbackBatteryTemp + 2.5f
        } catch (_: Exception) {
            fallbackBatteryTemp + 2.0f
        }
    }
}
