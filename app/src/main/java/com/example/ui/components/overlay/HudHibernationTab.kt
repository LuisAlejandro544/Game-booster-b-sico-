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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class BackgroundAppStatus(
    val packageName: String,
    val appName: String,
    val isHibernated: Boolean,
    val isProtectedWhitelist: Boolean
)

@Composable
fun HudHibernationTab(
    hibernatedPackages: Set<String>,
    whitelistExceptions: Set<String>,
    onToggleAppHibernation: (packageName: String, shouldHibernate: Boolean) -> Unit
) {
    // Common background heavy apps and communication apps
    val backgroundApps = listOf(
        BackgroundAppStatus("com.discord", "Discord (Voz/Chat)", hibernatedPackages.contains("com.discord"), whitelistExceptions.contains("com.discord")),
        BackgroundAppStatus("com.spotify.music", "Spotify (Música)", hibernatedPackages.contains("com.spotify.music"), whitelistExceptions.contains("com.spotify.music")),
        BackgroundAppStatus("com.whatsapp", "WhatsApp", hibernatedPackages.contains("com.whatsapp"), whitelistExceptions.contains("com.whatsapp")),
        BackgroundAppStatus("com.google.android.youtube", "YouTube", hibernatedPackages.contains("com.google.android.youtube") || true, false),
        BackgroundAppStatus("com.instagram.android", "Instagram", hibernatedPackages.contains("com.instagram.android") || true, false),
        BackgroundAppStatus("com.zhiliaoapp.musically", "TikTok", hibernatedPackages.contains("com.zhiliaoapp.musically") || true, false)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Control de Apps en Segundo Plano",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "En vivo",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = NeonGreen
                )
            )
        }

        Text(
            text = "Despierta Discord o Spotify al vuelo para hablar o escuchar música:",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )

        backgroundApps.take(4).forEach { app ->
            val isHibernated = app.isHibernated && !app.isProtectedWhitelist

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hud_hib_row_${app.packageName}"),
                colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isHibernated) NeonCyan.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHibernated) Icons.Default.AcUnit else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isHibernated) NeonCyan else NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Column {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = if (isHibernated) "💤 Hibernada (0% CPU)" else "⚡ Despierta y activa",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isHibernated) NeonCyan else NeonGreen
                                )
                            )
                        }
                    }

                    // Action Toggle Button
                    Button(
                        onClick = { onToggleAppHibernation(app.packageName, !isHibernated) },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("hud_hib_toggle_${app.packageName}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHibernated) NeonGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                            contentColor = if (isHibernated) NeonGreen else NeonCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (isHibernated) "DESPERTAR" else "DORMIR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }
    }
}
