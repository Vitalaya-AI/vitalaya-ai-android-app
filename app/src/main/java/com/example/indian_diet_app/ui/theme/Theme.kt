package com.example.indian_diet_app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color as ComposeColor

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = BackgroundDark,
    primaryContainer = ComposeColor(0xFF1B5E20),
    onPrimaryContainer = Green80,
    secondary = Orange80,
    onSecondary = BackgroundDark,
    secondaryContainer = ComposeColor(0xFFBF360C),
    onSecondaryContainer = Orange80,
    tertiary = Blue80,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = ComposeColor(0xFFFFFFFF),
    onSurface = ComposeColor(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = ComposeColor(0xFFFFFFFF),
    primaryContainer = ComposeColor(0xFFC8E6C9),
    onPrimaryContainer = ComposeColor(0xFF1B5E20),
    secondary = Orange40,
    onSecondary = ComposeColor(0xFFFFFFFF),
    secondaryContainer = ComposeColor(0xFFFFE0B2),
    onSecondaryContainer = ComposeColor(0xFFBF360C),
    tertiary = Blue40,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = ComposeColor(0xFF1C2B1E),
    onSurface = ComposeColor(0xFF1C2B1E),
    surfaceVariant = ComposeColor(0xFFE8F5E9),
    onSurfaceVariant = ComposeColor(0xFF4A5568),
)

@Composable
fun Indian_diet_appTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}