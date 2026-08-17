package com.example.ui.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.hypot

@Composable
fun FloatingGamerBubble(
    fps: Int,
    temp: Int,
    onBubbleClick: () -> Unit,
    onDrag: (Float, Float) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)),
                RoundedCornerShape(24.dp)
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var prevX = down.position.x
                    var prevY = down.position.y
                    var isDrag = false
                    var totalDrag = 0f
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val current = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!current.pressed) {
                            // Finger lifted (UP)
                            if (!isDrag) {
                                onBubbleClick()
                            }
                            break
                        }

                        val dx = current.position.x - prevX
                        val dy = current.position.y - prevY
                        totalDrag += hypot(dx, dy)

                        if (!isDrag && totalDrag > touchSlop) {
                            isDrag = true
                        }

                        if (isDrag) {
                            current.consume()
                            onDrag(dx, dy)
                        }

                        prevX = current.position.x
                        prevY = current.position.y
                    }
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
                    .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
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
        }
    }
}
