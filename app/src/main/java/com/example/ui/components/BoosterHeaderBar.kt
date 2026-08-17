package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamerDarkBackground
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ShizukuState

@Composable
fun BoosterHeaderBar(
    deviceModel: String,
    refreshRate: Int,
    shizukuState: ShizukuState,
    onOpenSpeedTest: () -> Unit,
    onRefreshShizuku: () -> Unit,
    onToggleOverlayHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = GamerDarkBackground,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GAME BOOSTER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )

                    val badgeColor = when (shizukuState) {
                        ShizukuState.AUTHORIZED -> NeonGreen
                        ShizukuState.PERMISSION_NEEDED -> Color(0xFFF59E0B)
                        ShizukuState.NOT_RUNNING -> NeonCyan
                    }

                    val badgeText = when (shizukuState) {
                        ShizukuState.AUTHORIZED -> "SHIZUKU"
                        ShizukuState.PERMISSION_NEEDED -> "PENDIENTE"
                        ShizukuState.NOT_RUNNING -> "TURBO"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            ),
                            color = badgeColor
                        )
                    }
                }

                Text(
                    text = "$deviceModel • ${refreshRate}Hz",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }

        // Quick Tools Actions
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onToggleOverlayHud,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GamerSurfaceElevated)
                    .testTag("top_overlay_hud_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Burbuja HUD Gamer",
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRefreshShizuku,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GamerSurfaceElevated)
                    .testTag("top_refresh_shizuku_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar Shizuku",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenSpeedTest,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GamerSurfaceElevated)
                    .testTag("top_ping_tool_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Test Ping",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
