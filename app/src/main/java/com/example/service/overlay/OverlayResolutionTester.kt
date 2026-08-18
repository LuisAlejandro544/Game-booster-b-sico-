package com.example.service.overlay

import android.content.Context
import com.example.data.BoosterPreferences
import com.example.model.DisplayResolutionScale
import com.example.util.ShizukuManager
import com.example.util.shizuku.DisplayScaleController
import com.example.util.shizuku.ResolutionCountdownNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Handles 15-second live resolution and DPI testing with auto-revert failsafe.
 */
class OverlayResolutionTester(
    private val context: Context,
    private val scope: CoroutineScope,
    private val prefs: BoosterPreferences,
    private val onStateChanged: (isTesting: Boolean, countdownSeconds: Int, currentScale: DisplayResolutionScale, feedback: String?) -> Unit
) {
    private var isTesting = false
    private var countdown = 15
    private var testJob: Job? = null
    private var scaleBeforeTest: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
    var activeScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100
        private set

    fun setInitialScale(scale: DisplayResolutionScale) {
        activeScale = scale
    }

    fun startTest(testScale: DisplayResolutionScale) {
        if (!ShizukuManager.isAuthorized) {
            onStateChanged(isTesting, countdown, activeScale, "⚠️ Requiere Shizuku autorizado")
            return
        }

        testJob?.cancel()
        scaleBeforeTest = activeScale
        activeScale = testScale
        isTesting = true
        countdown = 15
        onStateChanged(isTesting, countdown, activeScale, null)

        testJob = scope.launch {
            DisplayScaleController.applyDisplayScale(
                context = context,
                scale = testScale,
                isAuthorized = ShizukuManager.isAuthorized
            )

            for (sec in 15 downTo 1) {
                countdown = sec
                onStateChanged(isTesting, countdown, activeScale, null)
                ResolutionCountdownNotifier.showCountdownTick(context, sec, testScale)
                delay(1000L)
            }

            if (isTesting) {
                cancelTest()
                ResolutionCountdownNotifier.showReverted(context)
                onStateChanged(false, 0, activeScale, "⏱️ Test finalizado: Escala revertida")
            }
        }
    }

    fun confirmTest(targetPackage: String?) {
        testJob?.cancel()
        isTesting = false
        ResolutionCountdownNotifier.showConfirmed(context, activeScale)
        if (targetPackage != null) {
            prefs.setGameDisplayScale(targetPackage, activeScale)
        }
        onStateChanged(false, 0, activeScale, "✓ Escala ${activeScale.title} confirmada")
    }

    fun cancelTest() {
        testJob?.cancel()
        isTesting = false
        ResolutionCountdownNotifier.cancel()
        val prevScale = scaleBeforeTest
        activeScale = prevScale
        onStateChanged(false, 0, activeScale, null)
        scope.launch(Dispatchers.IO) {
            DisplayScaleController.applyDisplayScale(
                context = context,
                scale = prevScale,
                isAuthorized = ShizukuManager.isAuthorized
            )
        }
    }

    fun cancel() {
        testJob?.cancel()
        isTesting = false
    }
}
