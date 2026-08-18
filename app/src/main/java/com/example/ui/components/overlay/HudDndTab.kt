package com.example.ui.components.overlay

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HudDndTab(
    isDndActive: Boolean,
    blockHeadsUp: Boolean,
    allowCalls: Boolean,
    dndExceptions: Set<String>,
    onToggleDnd: (Boolean) -> Unit,
    onToggleBlockHeadsUp: (Boolean) -> Unit,
    onToggleAllowCalls: (Boolean) -> Unit,
    onToggleAppException: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main DND Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDndActive) NeonCyan.copy(alpha = 0.15f) else GamerSurfaceElevated
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDndActive) NeonCyan.copy(alpha = 0.6f) else Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDndActive) NeonCyan.copy(alpha = 0.2f) else GamerDarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (isDndActive) NeonCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Modo No Molestar (DND)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (isDndActive) NeonCyan else TextPrimary
                        )
                        Text(
                            text = if (isDndActive) "Silenciando banners e interrupciones" else "Desactivado en esta partida",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isDndActive,
                    onCheckedChange = onToggleDnd,
                    modifier = Modifier.testTag("hud_dnd_master_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerDarkBackground
                    )
                )
            }
        }

        // Sub-options: Block Heads-Up and Allow Calls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Heads-up blocker toggle
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleBlockHeadsUp(!blockHeadsUp) }
                    .testTag("hud_toggle_heads_up"),
                colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (blockHeadsUp) NeonGreen else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloquear Banners",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = if (blockHeadsUp) TextPrimary else TextMuted
                        )
                        Text(
                            text = if (blockHeadsUp) "Heads-Up OFF" else "Permitidos",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = if (blockHeadsUp) NeonGreen else TextMuted
                        )
                    }
                }
            }

            // Allow calls toggle
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleAllowCalls(!allowCalls) }
                    .testTag("hud_toggle_calls"),
                colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = if (allowCalls) NeonPurple else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permitir Llamadas",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            color = if (allowCalls) TextPrimary else TextMuted
                        )
                        Text(
                            text = if (allowCalls) "Llamadas ON" else "Silenciadas",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = if (allowCalls) NeonPurple else TextMuted
                        )
                    }
                }
            }
        }

        // Quick Exceptions Chips for popular communication apps
        Text(
            text = "Excepciones Rápidas (recibir avisos de):",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        )

        val quickApps = listOf(
            "com.whatsapp" to "WhatsApp",
            "com.discord" to "Discord",
            "org.telegram.messenger" to "Telegram",
            "com.google.android.dialer" to "Teléfono"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickApps.forEach { (pkg, label) ->
                val isWhitelisted = dndExceptions.contains(pkg)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isWhitelisted) NeonGreen.copy(alpha = 0.15f) else GamerSurfaceElevated)
                        .border(
                            1.dp,
                            if (isWhitelisted) NeonGreen.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleAppException(pkg) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isWhitelisted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = if (isWhitelisted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isWhitelisted) NeonGreen else TextSecondary
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
