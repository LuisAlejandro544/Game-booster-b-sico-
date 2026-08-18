package com.example.service.overlay

import android.view.Choreographer

/**
 * Tracks real-time FPS using Choreographer and delivers calculated FPS via callback.
 */
class FpsTracker(private val onFpsUpdated: (Int) -> Unit) {
    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private var isTracking = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isTracking) return

            val nowMs = frameTimeNanos / 1_000_000
            if (lastFpsTimestamp == 0L) {
                lastFpsTimestamp = nowMs
            }

            frameCount++
            val delta = nowMs - lastFpsTimestamp
            if (delta >= 1000) {
                val calculated = (frameCount * 1000L / delta).toInt()
                onFpsUpdated(calculated.coerceIn(15, 144))
                frameCount = 0
                lastFpsTimestamp = nowMs
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (!isTracking) {
            isTracking = true
            frameCount = 0
            lastFpsTimestamp = 0L
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    fun stop() {
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
