package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DeviceMetrics
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver
import com.example.ui.components.overlay.ExpandedGamerPanel
import com.example.ui.components.overlay.FloatingGamerBubble
import com.example.ui.components.overlay.HudTab

// Re-export HudTab for backward compatibility
typealias HudTab = com.example.ui.components.overlay.HudTab

/**
 * Top-level entry point for the floating Game Overlay HUD (Floating bubble & Expanded panel).
 */
@Composable
fun GameOverlayHudContent(
    isExpanded: Boolean,
    currentFps: Int,
    targetGamePackage: String?,
    targetGameTitle: String,
    currentDriver: GraphicsDriver,
    currentDisplayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100,
    isTestingResolution: Boolean = false,
    testCountdownSeconds: Int = 15,
    metrics: DeviceMetrics,
    onToggleExpand: () -> Unit,
    onCloseOverlay: () -> Unit,
    onDriverSelected: (GraphicsDriver) -> Unit,
    onScaleSelected: (DisplayResolutionScale) -> Unit = {},
    onStartResolutionTest: (DisplayResolutionScale) -> Unit = {},
    onConfirmResolutionTest: () -> Unit = {},
    onCancelResolutionTest: () -> Unit = {},
    onQuickBoost: () -> Unit,
    feedbackMessage: String?,
    onDragStart: (Float, Float) -> Unit = { _, _ -> },
    onDragMove: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDrag: (Float, Float) -> Unit = { _, _ -> }
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .testTag("game_overlay_root")
    ) {
        if (!isExpanded) {
            // Minimized Floating Gamer Bubble
            FloatingGamerBubble(
                fps = currentFps,
                temp = metrics.cpuTempCelsius.toInt(),
                onBubbleClick = onToggleExpand,
                onDragStart = onDragStart,
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                onDrag = onDrag
            )
        } else {
            // Expanded In-Game Gaming Hub Panel
            ExpandedGamerPanel(
                targetGameTitle = targetGameTitle,
                fps = currentFps,
                currentDriver = currentDriver,
                currentDisplayScale = currentDisplayScale,
                isTestingResolution = isTestingResolution,
                testCountdownSeconds = testCountdownSeconds,
                metrics = metrics,
                onMinimize = onToggleExpand,
                onClose = onCloseOverlay,
                onDriverSelected = onDriverSelected,
                onScaleSelected = onScaleSelected,
                onStartResolutionTest = onStartResolutionTest,
                onConfirmResolutionTest = onConfirmResolutionTest,
                onCancelResolutionTest = onCancelResolutionTest,
                onQuickBoost = onQuickBoost,
                feedbackMessage = feedbackMessage,
                onDragStart = onDragStart,
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                onDrag = onDrag
            )
        }
    }
}
