package com.example.util

import android.util.Log
import kotlin.math.abs

/**
 * FFI Bridge for the Rust Native Core (gamebooster_rust_core).
 * Provides zero-cost memory pressure calculation and jitter analysis.
 * Includes graceful Kotlin fallbacks if the native .so is not present.
 */
object RustCoreBridge {
    private const val TAG = "RustCoreBridge"
    private var isRustLoaded = false

    init {
        try {
            System.loadLibrary("gamebooster_rust_core")
            isRustLoaded = true
            Log.i(TAG, "Rust Core library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            isRustLoaded = false
            Log.w(TAG, "Rust Core library not available. Using pure Kotlin fallback: ${e.message}")
        }
    }

    fun isLoaded(): Boolean = isRustLoaded

    fun getVersion(): String {
        return if (isRustLoaded) {
            try {
                getRustCoreVersion()
            } catch (e: Throwable) {
                "Rust Core (Fallback)"
            }
        } else {
            "Rust Core (Estructura Cargo FFI Lista)"
        }
    }

    fun calculatePressure(usedMb: Long, totalMb: Long): Int {
        return if (isRustLoaded) {
            try {
                calculateMemoryPressure(usedMb, totalMb)
            } catch (e: Throwable) {
                fallbackMemoryPressure(usedMb, totalMb)
            }
        } else {
            fallbackMemoryPressure(usedMb, totalMb)
        }
    }

    fun calculateJitterScore(pingA: Int, pingB: Int): Int {
        return if (isRustLoaded) {
            try {
                calculateJitter(pingA, pingB)
            } catch (e: Throwable) {
                abs(pingA - pingB)
            }
        } else {
            abs(pingA - pingB)
        }
    }

    private fun fallbackMemoryPressure(usedMb: Long, totalMb: Long): Int {
        if (totalMb <= 0) return 0
        val ratio = (usedMb.toDouble() / totalMb.toDouble()) * 100
        return ratio.toInt().coerceIn(0, 100)
    }

    // Rust Native declarations
    private external fun getRustCoreVersion(): String
    private external fun calculateMemoryPressure(usedMb: Long, totalMb: Long): Int
    private external fun calculateJitter(pingA: Int, pingB: Int): Int
}
