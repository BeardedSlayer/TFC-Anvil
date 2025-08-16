package com.example.tfcanvilcalc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color.White.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = DarkTeal,
    secondary = MediumTeal,
    tertiary = LightTeal,
    background = LightBlue,
    surface = Color(0xFFEEFAFC),
    error = Color(0xFFB00020),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF001F2A),
    onSurface = Color(0xFF001F2A),
    onError = Color.White,
    surfaceVariant = Color(0xFFE1E1E1),
    onSurfaceVariant = Color.Black.copy(alpha = 0.7f)
)

@Composable
fun TFCAnvilCalcTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}