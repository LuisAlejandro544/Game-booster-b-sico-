package com.example.ui.components.overlay

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.hypot

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FloatingGamerBubble(
    fps: Int,
    temp: Int,
    isTestingResolution: Boolean = false,
    testCountdownSeconds: Int = 15,
    onBubbleClick: () -> Unit,
    onDragStart: (Float, Float) -> Unit = { _, _ -> },
    onDragMove: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onDrag: (Float, Float) -> Unit = { _, _ -> }
) {
    var touchDownRawX by remember { mutableFloatStateOf(0f) }
    var touchDownRawY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val borderGradient = if (isTestingResolution) {
        Brush.horizontalGradient(listOf(NeonAmber, NeonOrange))
    } else {
        Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
    }

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                borderGradient,
                RoundedCornerShape(24.dp)
            )
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        touchDownRawX = motionEvent.rawX
                        touchDownRawY = motionEvent.rawY
                        isDragging = false
                        onDragStart(motionEvent.rawX, motionEvent.rawY)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dist = hypot(motionEvent.rawX - touchDownRawX, motionEvent.rawY - touchDownRawY)
                        if (!isDragging && dist > 16f) {
                            isDragging = true
                        }
                        if (isDragging) {
                            onDragMove(motionEvent.rawX, motionEvent.rawY)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            onDragEnd()
                        } else {
                            onBubbleClick()
                        }
                        isDragging = false
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (isDragging) {
                            onDragEnd()
                        }
                        isDragging = false
                        true
                    }
                    else -> false
                }
            }
            .testTag("floating_hud_bubble"),
        colors = CardDefaults.cardColors(
            containerColor = GamerDarkBackground.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rocket Icon badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTestingResolution) {
                            Brush.linearGradient(listOf(NeonAmber, NeonOrange))
                        } else {
                            Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Gamer HUD",
                    tint = GamerDarkBackground,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Real-time FPS Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$fps",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (fps >= 55) NeonGreen else if (fps >= 30) NeonOrange else NeonRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "FPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Divider dot
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(GamerCardBorder)
            )

            // SoC Temperature
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = if (temp > 45) NeonRed else NeonOrange,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${temp}°C",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            // Active Resolution Test Timer Badge (visible when testing and minimized)
            if (isTestingResolution) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(GamerCardBorder)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonAmber.copy(alpha = 0.2f))
                        .border(1.dp, NeonAmber, RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${testCountdownSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
