package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Elegant Dark Color Scheme (Primary Theme Definition)
val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantPurple,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPurpleContainer,
    onPrimaryContainer = ElegantOnPurpleContainer,
    secondary = ElegantTeal,
    onSecondary = ElegantOnTeal,
    secondaryContainer = ElegantTealContainer,
    onSecondaryContainer = ElegantOnTealContainer,
    tertiary = ElegantAmber,
    onTertiary = ElegantOnAmber,
    tertiaryContainer = ElegantAmberContainer,
    onTertiaryContainer = ElegantOnAmberContainer,
    background = ElegantDarkBackground,
    onBackground = ElegantTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondary,
    surfaceTint = ElegantPurple,
    outline = ElegantDarkOutline,
    outlineVariant = ElegantDarkOutlineVariant,
    error = ElegantRed,
    onError = ElegantOnRed,
    errorContainer = ElegantRedContainer,
    onErrorContainer = ElegantOnRedContainer
)

@Composable
fun MedCareTheme(
    darkTheme: Boolean = true, // Default to Elegant Dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}
