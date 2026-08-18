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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamerCardBackground
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerDarkBackground
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
fun SheetAdvancedOptionsSection(
    deepHibernate: Boolean,
    onDeepHibernateChange: (Boolean) -> Unit,
    hibernateGoogle: Boolean,
    onHibernateGoogleChange: (Boolean) -> Unit,
    enableOverlayHud: Boolean,
    onEnableOverlayHudChange: (Boolean) -> Unit,
    enableDnd: Boolean,
    onEnableDndChange: (Boolean) -> Unit,
    dndAllowCalls: Boolean,
    onDndAllowCallsChange: (Boolean) -> Unit,
    dndBlockHeadsUp: Boolean,
    onDndBlockHeadsUpChange: (Boolean) -> Unit,
    enableTouchBoost: Boolean,
    onEnableTouchBoostChange: (Boolean) -> Unit,
    enableWifiHighPerf: Boolean,
    onEnableWifiHighPerfChange: (Boolean) -> Unit,
    enableCrosshair: Boolean,
    onEnableCrosshairChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "OPCIONES AVANZADAS DE OPTIMIZACIÓN",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 1: Deep Background Hibernation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (deepHibernate) NeonCyan.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onDeepHibernateChange(!deepHibernate) },
            colors = CardDefaults.cardColors(
                containerColor = if (deepHibernate) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = if (deepHibernate) NeonCyan else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Centinela de Hibernación en Juego",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Fuerza el reposo de redes sociales y apps pesadas mientras juegas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    )
                }
                Switch(
                    checked = deepHibernate,
                    onCheckedChange = onDeepHibernateChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 2: Suspend Google Play Services
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (hibernateGoogle) NeonPurple.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onHibernateGoogleChange(!hibernateGoogle) },
            colors = CardDefaults.cardColors(
                containerColor = if (hibernateGoogle) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = if (hibernateGoogle) NeonPurple else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Suspender Servicios Google",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+400MB RAM",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonPurple
                            )
                        }
                    }
                    Text(
                        text = "Ideal para juegos offline o con cuenta propia (Riot, Supercell, Epic, Hoyoverse). Vuelve automáticamente al salir.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    )
                }
                Switch(
                    checked = hibernateGoogle,
                    onCheckedChange = onHibernateGoogleChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonPurple,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 3: Floating Gamer HUD Overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (enableOverlayHud) NeonGreen.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onEnableOverlayHudChange(!enableOverlayHud) },
            colors = CardDefaults.cardColors(
                containerColor = if (enableOverlayHud) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = if (enableOverlayHud) NeonGreen else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Burbuja Flotante HUD",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EN VIVO",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonGreen
                            )
                        }
                    }
                    Text(
                        text = "Muestra FPS, temperatura y permite cambiar entre Vulkan, ANGLE y OpenGL directamente mientras juegas.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    )
                }
                Switch(
                    checked = enableOverlayHud,
                    onCheckedChange = onEnableOverlayHudChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 4: Gamer DND Mode
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (enableDnd) NeonAmber.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onEnableDndChange(!enableDnd) },
            colors = CardDefaults.cardColors(
                containerColor = if (enableDnd) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = if (enableDnd) NeonAmber else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Modo No Molestar (DND)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AUTO",
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = NeonAmber
                                )
                            }
                        }
                        Text(
                            text = "Silencia distracciones y bloquea banners emergentes mientras juegas. Restaura el estado al salir.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 13.sp
                            )
                        )
                    }
                    Switch(
                        checked = enableDnd,
                        onCheckedChange = onEnableDndChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GamerDarkBackground,
                            checkedTrackColor = NeonAmber,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = GamerSurfaceElevated
                        )
                    )
                }

                if (enableDnd) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDndAllowCallsChange(!dndAllowCalls) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (dndAllowCalls) NeonAmber.copy(alpha = 0.15f) else GamerCardBackground
                            ),
                            border = BorderStroke(1.dp, if (dndAllowCalls) NeonAmber.copy(alpha = 0.5f) else GamerCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (dndAllowCalls) "✓ Llamadas Permitidas" else "✕ Bloquear Llamadas",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dndAllowCalls) NeonAmber else TextMuted
                                    )
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDndBlockHeadsUpChange(!dndBlockHeadsUp) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (dndBlockHeadsUp) NeonCyan.copy(alpha = 0.15f) else GamerCardBackground
                            ),
                            border = BorderStroke(1.dp, if (dndBlockHeadsUp) NeonCyan.copy(alpha = 0.5f) else GamerCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (dndBlockHeadsUp) "✓ Ocultar Banners" else "✕ Mostrar Banners",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dndBlockHeadsUp) NeonCyan else TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 5: Touch Boost
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (enableTouchBoost) NeonCyan.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onEnableTouchBoostChange(!enableTouchBoost) },
            colors = CardDefaults.cardColors(
                containerColor = if (enableTouchBoost) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (enableTouchBoost) NeonCyan else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Touch Boost & Respuesta",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "120Hz/MAX",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonCyan
                            )
                        }
                    }
                    Text(
                        text = "Aumenta la sensibilidad del puntero a nivel 7, fuerza la tasa máxima de refresco y reduce latencia táctil.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    )
                }
                Switch(
                    checked = enableTouchBoost,
                    onCheckedChange = onEnableTouchBoostChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 6: Wi-Fi Anti-Jitter Optimizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (enableWifiHighPerf) NeonGreen.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onEnableWifiHighPerfChange(!enableWifiHighPerf) },
            colors = CardDefaults.cardColors(
                containerColor = if (enableWifiHighPerf) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (enableWifiHighPerf) NeonGreen else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Wi-Fi Ultra-Baja Latencia",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PING PLANO",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonGreen
                            )
                        }
                    }
                    Text(
                        text = "Desactiva la suspensión de energía del chip Wi-Fi para evitar caídas de paquetes y picos de lag en shooters.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    )
                }
                Switch(
                    checked = enableWifiHighPerf,
                    onCheckedChange = onEnableWifiHighPerfChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 7: Tactical Crosshair Overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(
                    1.dp,
                    if (enableCrosshair) NeonRed.copy(alpha = 0.6f) else GamerCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onEnableCrosshairChange(!enableCrosshair) },
            colors = CardDefaults.cardColors(
                containerColor = if (enableCrosshair) GamerSurfaceElevated else GamerCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = if (enableCrosshair) NeonRed else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Mira Gamer Táctica (HUD)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FPS/AIM",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = NeonRed
                            )
                        }
                    }
                    Text(
                        text = "Superpone una retícula táctica fija personalizable en el centro para juegos Battle Royale y FPS.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    )
                }
                Switch(
                    checked = enableCrosshair,
                    onCheckedChange = onEnableCrosshairChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GamerDarkBackground,
                        checkedTrackColor = NeonRed,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GamerSurfaceElevated
                    )
                )
            }
        }
    }
}
