package com.example.util

import android.util.Log

/**
 * JNI Bridge for the C++ Native Turbo Engine (gamebooster_native).
 * Provides low-level hardware metrics and optimization buffers.
 * Includes graceful fallback if native binaries are not yet bundled.
 */
object NativeEngineBridge {
    private const val TAG = "NativeEngineBridge"
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("gamebooster_native")
            isNativeLoaded = true
            Log.i(TAG, "Native C++ Turbo Engine loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLoaded = false
            Log.w(TAG, "Native C++ library not available. Using pure Kotlin fallback: ${e.message}")
        }
    }

    fun isLoaded(): Boolean = isNativeLoaded

    fun getVersion(): String {
        return if (isNativeLoaded) {
            try {
                getNativeEngineVersion()
            } catch (e: Throwable) {
                "C++ Native (Fallback)"
            }
        } else {
            "C++ Native (Preparado para compilación NDK)"
        }
    }

    fun getFreeRamBytes(): Long {
        return if (isNativeLoaded) {
            try {
                getNativeFreeRamBytes()
            } catch (e: Throwable) {
                -1L
            }
        } else {
            -1L
        }
    }

    private var lastUser = 0L
    private var lastNice = 0L
    private var lastSys = 0L
    private var lastIdle = 0L
    private var lastIowait = 0L
    private var lastIrq = 0L
    private var lastSoftirq = 0L

    fun getCpuUsagePercent(): Int {
        if (isNativeLoaded) {
            try {
                val nativeUsage = getNativeCpuUsage()
                if (nativeUsage in 0..100) return nativeUsage
            } catch (_: Throwable) {}
        }
        return readCpuUsageFromProcStat()
    }

    private fun readCpuUsageFromProcStat(): Int {
        return try {
            val statFile = java.io.File("/proc/stat")
            if (!statFile.exists() || !statFile.canRead()) return 24

            val firstLine = statFile.bufferedReader().use { it.readLine() } ?: return 24
            val tokens = firstLine.split("\\s+".toRegex())
            if (tokens.size >= 8 && tokens[0] == "cpu") {
                val user = tokens[1].toLongOrNull() ?: 0L
                val nice = tokens[2].toLongOrNull() ?: 0L
                val sys = tokens[3].toLongOrNull() ?: 0L
                val idle = tokens[4].toLongOrNull() ?: 0L
                val iowait = tokens[5].toLongOrNull() ?: 0L
                val irq = tokens[6].toLongOrNull() ?: 0L
                val softirq = tokens[7].toLongOrNull() ?: 0L

                val totalPrev = lastUser + lastNice + lastSys + lastIdle + lastIowait + lastIrq + lastSoftirq
                val totalCurrent = user + nice + sys + idle + iowait + irq + softirq
                val idlePrev = lastIdle + lastIowait
                val idleCurrent = idle + iowait

                val totalDelta = totalCurrent - totalPrev
                val idleDelta = idleCurrent - idlePrev

                lastUser = user
                lastNice = nice
                lastSys = sys
                lastIdle = idle
                lastIowait = iowait
                lastIrq = irq
                lastSoftirq = softirq

                if (totalDelta > 0 && totalDelta >= idleDelta) {
                    val activeDelta = totalDelta - idleDelta
                    val percent = ((activeDelta * 100) / totalDelta).toInt()
                    return percent.coerceIn(1, 100)
                }
            }
            24
        } catch (_: Throwable) {
            24
        }
    }

    fun calculateOptimalBuffer(currentPing: Int, ramUsagePercent: Int): Int {
        return if (isNativeLoaded) {
            try {
                getNativeOptimalBuffer(currentPing, ramUsagePercent)
            } catch (e: Throwable) {
                fallbackBufferCalculation(currentPing, ramUsagePercent)
            }
        } else {
            fallbackBufferCalculation(currentPing, ramUsagePercent)
        }
    }

    private fun fallbackBufferCalculation(currentPing: Int, ramUsagePercent: Int): Int {
        var base = 64
        if (currentPing > 100) base = 128
        if (ramUsagePercent > 80) base *= 2
        return base
    }

    // Native C++ declarations
    private external fun getNativeEngineVersion(): String
    private external fun getNativeFreeRamBytes(): Long
    private external fun getNativeCpuUsage(): Int
    private external fun getNativeOptimalBuffer(currentPing: Int, ramUsagePercent: Int): Int
}
