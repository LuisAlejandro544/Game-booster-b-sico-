package com.example.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Manages the WindowManager lifecycle, layout params, and touch dragging for floating game overlays.
 */
class DraggableOverlayWindowManager(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayComposeView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    // Absolute Screen-Space Dragging State (Zero-Jitter / Zero-Wobble)
    private var dragStartRawX: Float = 0f
    private var dragStartRawY: Float = 0f
    private var dragStartWindowX: Int = 0
    private var dragStartWindowY: Int = 0

    val isAttached: Boolean
        get() = overlayComposeView != null

    fun attachOverlay(
        initialX: Int = 30,
        initialY: Int = 180,
        content: @Composable () -> Unit
    ) {
        if (overlayComposeView != null) return

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        dragStartWindowX = initialX
        dragStartWindowY = initialY

        val owner = OverlayLifecycleOwner().apply {
            performRestore(null)
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        lifecycleOwner = owner

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setContent(content)
        }

        try {
            windowManager.addView(composeView, layoutParams)
            overlayComposeView = composeView
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay view to WindowManager", e)
        }
    }

    /**
     * Initializes a drag session using absolute screen coordinates (rawX, rawY).
     */
    fun onDragStart(rawX: Float, rawY: Float) {
        dragStartRawX = rawX
        dragStartRawY = rawY
        dragStartWindowX = layoutParams.x
        dragStartWindowY = layoutParams.y
    }

    /**
     * Smoothly updates window position using absolute screen-space deltas,
     * preventing any feedback jitter or wobble caused by relative window translation.
     */
    fun onDragMove(rawX: Float, rawY: Float) {
        try {
            val deltaX = rawX - dragStartRawX
            val deltaY = rawY - dragStartRawY

            val displayMetrics = context.resources.displayMetrics
            val maxW = displayMetrics.widthPixels
            val maxH = displayMetrics.heightPixels

            layoutParams.x = (dragStartWindowX + deltaX.toInt()).coerceIn(-20, maxW - 40)
            layoutParams.y = (dragStartWindowY + deltaY.toInt()).coerceIn(0, maxH - 80)

            overlayComposeView?.let { view ->
                windowManager.updateViewLayout(view, layoutParams)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating drag move", e)
        }
    }

    /**
     * Finalizes the current drag session.
     */
    fun onDragEnd() {
        dragStartWindowX = layoutParams.x
        dragStartWindowY = layoutParams.y
    }

    /**
     * Fallback relative repositioning.
     */
    fun moveBy(deltaX: Float, deltaY: Float) {
        try {
            val displayMetrics = context.resources.displayMetrics
            val maxW = displayMetrics.widthPixels
            val maxH = displayMetrics.heightPixels

            layoutParams.x = (layoutParams.x + deltaX.toInt()).coerceIn(-20, maxW - 40)
            layoutParams.y = (layoutParams.y + deltaY.toInt()).coerceIn(0, maxH - 80)

            overlayComposeView?.let { view ->
                windowManager.updateViewLayout(view, layoutParams)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error moving overlay view", e)
        }
    }

    fun requestLayoutUpdate() {
        try {
            overlayComposeView?.let { view ->
                windowManager.updateViewLayout(view, layoutParams)
            }
        } catch (_: Exception) {}
    }

    fun detachOverlay() {
        lifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleOwner?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        lifecycleOwner = null

        overlayComposeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
        }
        overlayComposeView = null
    }

    companion object {
        private const val TAG = "DraggableOverlay"
    }
}
