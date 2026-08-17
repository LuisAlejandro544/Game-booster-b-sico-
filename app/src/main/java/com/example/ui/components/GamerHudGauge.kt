package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceMetrics
import com.example.ui.theme.GamerCardBackground
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GamerHudGauge(
    metrics: DeviceMetrics,
    isBoosting: Boolean,
    onBoostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRamPercent by animateFloatAsState(
        targetValue = metrics.ramUsagePercent.toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "ram_percent"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_rotate")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val gaugeColor = when {
        metrics.ramUsagePercent > 80 -> NeonRed
        metrics.ramUsagePercent > 60 -> NeonPurple
        else -> NeonCyan
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glowing Canvas & Gauge Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track Arc (260 degrees)
            drawArc(
                color = Color(0xFF1E2337),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Neon Active Gauge Arc
            val sweep = (animatedRamPercent / 100f) * 260f
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to NeonCyan,
                    0.5f to NeonPurple,
                    1.0f to gaugeColor
                ),
                startAngle = 140f,
                sweepAngle = sweep.coerceIn(5f, 260f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Gamer tick marks along inner ring
            val innerRadius = diameter / 2 - 18.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            for (i in 0..12) {
                val angleRad = Math.toRadians((140.0 + (i * (260.0 / 12.0))))
                val startX = center.x + (innerRadius * Math.cos(angleRad)).toFloat()
                val startY = center.y + (innerRadius * Math.sin(angleRad)).toFloat()
                val endX = center.x + ((innerRadius - 6.dp.toPx()) * Math.cos(angleRad)).toFloat()
                val endY = center.y + ((innerRadius - 6.dp.toPx()) * Math.sin(angleRad)).toFloat()
                drawLine(
                    color = if (i <= (metrics.ramUsagePercent * 12 / 100)) gaugeColor.copy(alpha = 0.8f) else Color(0xFF2A314D),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Central Interactive Turbo Boost Button
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(if (isBoosting) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E243D),
                            GamerCardBackground,
                            GamerDarkBackground
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(NeonCyan, NeonPurple, Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clickable(enabled = !isBoosting) { onBoostClick() }
                .testTag("boost_hud_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Optimizar",
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${metrics.ramUsagePercent}%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = if (isBoosting) "BOOSTING..." else "OPTIMIZAR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = if (metrics.isOptimized) NeonGreen else NeonCyan
                )

                Text(
                    text = "RAM ${metrics.usedRamMb}MB",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = TextMuted
                )
            }
        }
    }
}
