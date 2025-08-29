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
    primary = Moonveil,                    // F28D42 - Orange accent
    secondary = LilacMistDark,             // E4E0F1 - Secondary lilac
    tertiary = PassiveText,                // 7A7A81 - Tertiary gray
    background = DarkBackground,           // 2A2D3A - Dark background
    surface = DarkSurface,                 // 39393B - Dark surface
    error = ErrorDark,                     // CF6679 - Standard dark error
    onPrimary = PureWhite,                 // FFFFFF - White text on orange
    onSecondary = DeepPurple,              // 39393B - Dark text on lilac
    onTertiary = LightOnDark,              // ECEAF5 - Light text on gray
    onBackground = LightOnDark,            // ECEAF5 - Light text on dark bg
    onSurface = LightOnDark,               // ECEAF5 - Light text on dark surface
    onError = PureWhite,                   // FFFFFF - White text on error
    surfaceVariant = DeepPurple,           // 39393B - Variant surface
    onSurfaceVariant = LilacMist           // ECEAF5 - Light text on variant
)

private val LightColorScheme = lightColorScheme(
    primary = Moonveil,                    // F28D42 - Orange accent
    secondary = LilacMistDark,             // E4E0F1 - Secondary lilac
    tertiary = SoftGray,                   // B0B0B0 - Tertiary gray
    background = LilacMist,                // ECEAF5 - Light lilac background
    surface = LilacMistDark,               // E4E0F1 - Slightly darker surface
    error = ErrorLight,                    // D32F2F - Standard light error
    onPrimary = PureWhite,                 // FFFFFF - White text on orange
    onSecondary = DeepPurple,              // 39393B - Dark text on lilac
    onTertiary = DeepPurple,               // 39393B - Dark text on gray
    onBackground = DeepPurple,             // 39393B - Dark text on light bg
    onSurface = DeepPurple,                // 39393B - Dark text on surface
    onError = PureWhite,                   // FFFFFF - White text on error
    surfaceVariant = LilacMist,            // ECEAF5 - Variant surface
    onSurfaceVariant = PassiveText         // 7A7A81 - Passive text on variant
)

@Composable
fun TFCAnvilCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    
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