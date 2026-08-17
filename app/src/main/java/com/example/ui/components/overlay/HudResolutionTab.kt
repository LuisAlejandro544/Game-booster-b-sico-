package com.example.ui.components.overlay

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisplayResolutionScale
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
fun HudResolutionTab(
    currentScale: DisplayResolutionScale,
    isTestingResolution: Boolean,
    testCountdownSeconds: Int,
    onScaleSelected: (DisplayResolutionScale) -> Unit,
    onStartTest: (DisplayResolutionScale) -> Unit,
    onConfirmTest: () -> Unit,
    onCancelTest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ESCALA DE RESOLUCIÓN Y DPI EN JUEGO:",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Live Testing Countdown Banner
        if (isTestingResolution) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
                    .testTag("hud_test_countdown_card"),
                colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Probando: Auto-revert en ${testCountdownSeconds}s",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onConfirmTest,
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .testTag("hud_confirm_test_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NeonGreen,
                                contentColor = GamerCardBackground
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirmar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCancelTest,
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .testTag("hud_revert_test_button"),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, NeonRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Revertir", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        DisplayResolutionScale.entries.forEach { scale ->
            val isSelected = currentScale == scale
            val scaleColor = when (scale) {
                DisplayResolutionScale.NATIVE_100 -> TextMuted
                DisplayResolutionScale.BALANCED_85 -> NeonCyan
                DisplayResolutionScale.PERFORMANCE_75 -> NeonPurple
                DisplayResolutionScale.ULTRA_SMOOTH_50 -> NeonAmber
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) scaleColor else GamerCardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onScaleSelected(scale) }
                    .testTag("hud_resolution_option_${scale.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GamerSurfaceElevated else GamerCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) scaleColor else TextMuted)
                            )
                            Column {
                                Text(
                                    text = scale.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = scale.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        color = if (isSelected) TextSecondary else TextMuted
                                    )
                                )
                            }
                        }

                        if (isSelected) {
                            Text(
                                text = "ACTIVO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = scaleColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    // 15-second test button inside scale item
                    if (scale != DisplayResolutionScale.NATIVE_100) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onStartTest(scale) },
                                modifier = Modifier
                                    .height(26.dp)
                                    .testTag("hud_test_scale_${scale.name.lowercase()}"),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.8.dp, scaleColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = scaleColor)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Probar 15s", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
