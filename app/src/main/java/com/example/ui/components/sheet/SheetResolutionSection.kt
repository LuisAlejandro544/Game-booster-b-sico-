package com.example.ui.components.sheet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SheetResolutionSection(
    selectedScale: DisplayResolutionScale,
    isShizukuAuthorized: Boolean,
    onScaleSelected: (DisplayResolutionScale) -> Unit,
    onTestScale: (DisplayResolutionScale) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AspectRatio,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "ESCALA DE RESOLUCIÓN & DPI (FAILSAFE 5 CAPAS)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Reduce la carga de renderizado del GPU hasta un 75% manteniendo los DPI calibrados proporcionalmente para que los botones mantengan su tamaño táctil original.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DisplayResolutionScale.values().forEach { scale ->
                ResolutionScaleOptionCard(
                    scale = scale,
                    isSelected = selectedScale == scale,
                    isShizukuAuthorized = isShizukuAuthorized,
                    onSelect = { onScaleSelected(scale) },
                    onTest = { onTestScale(scale) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SafetyLayersCard()
    }
}

@Composable
private fun ResolutionScaleOptionCard(
    scale: DisplayResolutionScale,
    isSelected: Boolean,
    isShizukuAuthorized: Boolean,
    onSelect: () -> Unit,
    onTest: () -> Unit
) {
    val borderColor = if (isSelected) NeonPurple else GamerCardBorder
    val backgroundColor = if (isSelected) GamerSurfaceElevated else GamerCardBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .testTag("scale_option_${scale.id}"),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NeonPurple,
                    unselectedColor = TextMuted
                ),
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = scale.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextPrimary else TextSecondary
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) NeonPurple.copy(alpha = 0.25f) else GamerSurfaceElevated
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = scale.tag,
                            maxLines = 1,
                            softWrap = false,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonPurple else TextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = scale.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = if (isSelected) TextSecondary else TextMuted
                    )
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scale.performanceImpact,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = NeonPurple.copy(alpha = 0.85f),
                            lineHeight = 13.sp
                        )
                    )

                    if (scale != DisplayResolutionScale.NATIVE_100 && isShizukuAuthorized) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onTest,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, NeonPurple),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Probar 15 Segundos en Vivo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyLayersCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Arquitectura de Seguridad en 5 Capas",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                )
            }

            Text(
                text = "• 1. Watchdog Daemon: Proceso shell independiente con timeout de 35s.\n" +
                       "• 2. Botón de Pánico: Notificación persistente con acción directa.\n" +
                       "• 3. Boot Recovery: Restablecimiento garantizado si el teléfono se apaga.\n" +
                       "• 4. Clamping Seguro: Resolución par y DPI proporcional (sin descalibración).\n" +
                       "• 5. Test de 15 Segundos: Prueba en vivo con auto-revert si no confirmas.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            )
        }
    }
}
