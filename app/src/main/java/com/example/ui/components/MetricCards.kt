package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceMetrics
import com.example.ui.theme.GamerCardBackground
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MetricCardsGrid(
    metrics: DeviceMetrics,
    onPingCardClick: () -> Unit,
    onStorageCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Real Native CPU & RAM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CPU Card (Native /proc/stat)
            val cpuColor = when {
                metrics.cpuUsagePercent > 80 -> NeonRed
                metrics.cpuUsagePercent > 50 -> NeonAmber
                else -> NeonGreen
            }

            MetricCardItem(
                title = "USO CPU REAL",
                value = "${metrics.cpuUsagePercent}%",
                subtitle = "SoC: ${"%.1f".format(metrics.cpuTempCelsius)}°C • ${metrics.cpuCores} Cores",
                icon = Icons.Default.Speed,
                accentColor = cpuColor,
                progress = metrics.cpuUsagePercent / 100f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cpu_metric_card")
            )

            // RAM Card
            MetricCardItem(
                title = "MEMORIA RAM",
                value = "${metrics.usedRamMb} MB",
                subtitle = "Libre: ${metrics.availableRamMb} MB",
                icon = Icons.Default.Memory,
                accentColor = NeonCyan,
                progress = metrics.ramUsagePercent / 100f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ram_metric_card")
            )
        }

        // Row 2: Latency Ping & Battery
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ping Latency Card
            val pingColor = Color(metrics.pingRating.colorHex)
            val infiniteTransition = rememberInfiniteTransition(label = "ping_pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "ping_alpha"
            )

            MetricCardItem(
                title = "LATENCIA PING",
                value = "${metrics.pingMs} ms",
                subtitle = metrics.pingRating.label,
                icon = Icons.Default.Wifi,
                accentColor = pingColor,
                extraIndicator = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pingColor.copy(alpha = pulseAlpha))
                    )
                },
                onClick = onPingCardClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ping_metric_card")
            )

            // Battery & Temp Card
            val tempColor = when {
                metrics.batteryTempCelsius > 42f -> NeonRed
                metrics.batteryTempCelsius > 37f -> NeonAmber
                else -> NeonGreen
            }

            MetricCardItem(
                title = "BATERÍA & TEMP",
                value = "${metrics.batteryLevel}%",
                subtitle = "${"%.1f".format(metrics.batteryTempCelsius)}°C • ${metrics.batteryStatus}",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = tempColor,
                progress = metrics.batteryLevel / 100f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("battery_metric_card")
            )
        }

        // Row 3: Storage Card
        val storageColor = when {
            metrics.storageUsagePercent > 85 -> NeonRed
            metrics.storageUsagePercent > 70 -> NeonPurple
            else -> NeonGreen
        }

        MetricCardItem(
            title = "ALMACENAMIENTO INTERNO",
            value = "${"%.1f".format(metrics.storageUsedGb)} GB usados",
            subtitle = "${metrics.storageUsagePercent}% ocupado de ${"%.0f".format(metrics.storageTotalGb)} GB (Toca para liberar caché)",
            icon = Icons.Default.SdStorage,
            accentColor = storageColor,
            progress = metrics.storageUsagePercent / 100f,
            onClick = onStorageCardClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("storage_metric_card")
        )
    }
}

@Composable
fun MetricCardItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    extraIndicator: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GamerCardBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = GamerCardBackground),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextSecondary
                    )
                }

                extraIndicator?.invoke()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = TextMuted,
                maxLines = 1
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accentColor,
                    trackColor = GamerSurfaceElevated,
                )
            }
        }
    }
}
