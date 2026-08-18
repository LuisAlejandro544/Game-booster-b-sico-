package com.example.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * High-Precision Floating Gamer Crosshair (Mira Táctica).
 * Renders hardware-accelerated vector crosshairs on top of any game without input blocking.
 */
@Composable
fun CrosshairOverlayView(
    style: String = "CROSS",
    color: Color = Color(0xFF00F0FF),
    sizeDp: Int = 24,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 2.5f
            val halfW = size.width / 2f
            val halfH = size.height / 2f
            val gap = size.width * 0.18f

            when (style.uppercase()) {
                "DOT" -> {
                    drawCircle(
                        color = color,
                        radius = size.width * 0.18f,
                        center = center
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.5f),
                        radius = size.width * 0.22f,
                        center = center,
                        style = Stroke(width = 1f)
                    )
                }
                "CIRCLE_DOT" -> {
                    // Outer Ring
                    drawCircle(
                        color = color,
                        radius = halfW * 0.75f,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                    // Inner Dot
                    drawCircle(
                        color = color,
                        radius = 2.5f,
                        center = center
                    )
                }
                "DIAMOND" -> {
                    // Draw 4 lines forming a diamond
                    val top = Offset(center.x, center.y - halfH * 0.7f)
                    val right = Offset(center.x + halfW * 0.7f, center.y)
                    val bottom = Offset(center.x, center.y + halfH * 0.7f)
                    val left = Offset(center.x - halfW * 0.7f, center.y)

                    drawLine(color, top, right, strokeWidth)
                    drawLine(color, right, bottom, strokeWidth)
                    drawLine(color, bottom, left, strokeWidth)
                    drawLine(color, left, top, strokeWidth)
                    drawCircle(color, radius = 2f, center = center)
                }
                else -> { // "CROSS" default tactical
                    // Horizontal Left
                    drawLine(
                        color = color,
                        start = Offset(0f, center.y),
                        end = Offset(center.x - gap, center.y),
                        strokeWidth = strokeWidth
                    )
                    // Horizontal Right
                    drawLine(
                        color = color,
                        start = Offset(center.x + gap, center.y),
                        end = Offset(size.width, center.y),
                        strokeWidth = strokeWidth
                    )
                    // Vertical Top
                    drawLine(
                        color = color,
                        start = Offset(center.x, 0f),
                        end = Offset(center.x, center.y - gap),
                        strokeWidth = strokeWidth
                    )
                    // Vertical Bottom
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y + gap),
                        end = Offset(center.x, size.height),
                        strokeWidth = strokeWidth
                    )
                    // Center Dot
                    drawCircle(
                        color = color,
                        radius = 2f,
                        center = center
                    )
                }
            }
        }
    }
}
