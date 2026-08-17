package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DeviceMetrics
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
    metrics: DeviceMetrics,
    onToggleExpand: () -> Unit,
    onCloseOverlay: () -> Unit,
    onDriverSelected: (GraphicsDriver) -> Unit,
    onQuickBoost: () -> Unit,
    feedbackMessage: String?,
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
                onDrag = onDrag
            )
        } else {
            // Expanded In-Game Gaming Hub Panel
            ExpandedGamerPanel(
                targetGameTitle = targetGameTitle,
                fps = currentFps,
                currentDriver = currentDriver,
                metrics = metrics,
                onMinimize = onToggleExpand,
                onClose = onCloseOverlay,
                onDriverSelected = onDriverSelected,
                onQuickBoost = onQuickBoost,
                feedbackMessage = feedbackMessage,
                onDrag = onDrag
            )
        }
    }
}
