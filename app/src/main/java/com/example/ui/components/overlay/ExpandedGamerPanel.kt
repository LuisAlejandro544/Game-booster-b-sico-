package com.example.ui.components.overlay

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceMetrics
import com.example.model.DisplayResolutionScale
import com.example.model.GraphicsDriver
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExpandedGamerPanel(
    targetGameTitle: String,
    fps: Int,
    currentDriver: GraphicsDriver,
    currentDisplayScale: DisplayResolutionScale = DisplayResolutionScale.NATIVE_100,
    isTestingResolution: Boolean = false,
    testCountdownSeconds: Int = 15,
    metrics: DeviceMetrics,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
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
    var selectedTab by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .widthIn(min = 320.dp, max = 360.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(NeonCyan, NeonPurple)),
                RoundedCornerShape(16.dp)
            )
            .testTag("in_game_hud_panel"),
        colors = CardDefaults.cardColors(
            containerColor = GamerDarkBackground.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Game Title, Draggable Area and Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                onDragStart(motionEvent.rawX, motionEvent.rawY)
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                onDragMove(motionEvent.rawX, motionEvent.rawY)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                onDragEnd()
                                true
                            }
                            else -> false
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = GamerDarkBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "HUD GAMER EN VIVO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = targetGameTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(32.dp).testTag("hud_minimize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerticalAlignBottom,
                            contentDescription = "Minimizar",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("hud_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NeonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = GamerSurfaceElevated,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                HudTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (selectedTab == index) NeonCyan else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("hud_tab_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback Message Banner
            AnimatedVisibility(
                visible = !feedbackMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                feedbackMessage?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = NeonGreen.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (HudTab.entries[selectedTab]) {
                HudTab.TELEMETRY -> HudTelemetryTab(fps = fps, metrics = metrics)
                HudTab.RESOLUTION -> HudResolutionTab(
                    currentScale = currentDisplayScale,
                    isTestingResolution = isTestingResolution,
                    testCountdownSeconds = testCountdownSeconds,
                    onScaleSelected = onScaleSelected,
                    onStartTest = onStartResolutionTest,
                    onConfirmTest = onConfirmResolutionTest,
                    onCancelTest = onCancelResolutionTest
                )
                HudTab.DRIVERS -> HudDriversTab(
                    currentDriver = currentDriver,
                    onDriverSelected = onDriverSelected
                )
                HudTab.QUICK_BOOST -> HudQuickBoostTab(
                    metrics = metrics,
                    onQuickBoost = onQuickBoost
                )
            }
        }
    }
}
