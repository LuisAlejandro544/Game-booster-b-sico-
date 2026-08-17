package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GamerColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = GamerDarkBackground,
    primaryContainer = GamerSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = GamerDarkBackground,
    secondaryContainer = GamerSurfaceElevated,
    onSecondaryContainer = NeonPurple,
    tertiary = NeonGreen,
    onTertiary = GamerDarkBackground,
    background = GamerDarkBackground,
    onBackground = TextPrimary,
    surface = GamerCardBackground,
    onSurface = TextPrimary,
    surfaceVariant = GamerSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GamerCardBorder,
    error = NeonRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = GamerDarkBackground.toArgb()
                window.navigationBarColor = GamerDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = GamerColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun GameBoosterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}
