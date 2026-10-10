package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CodeLensColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F385C),
    onPrimaryContainer = Color(0xFFCCE4FF),
    secondary = AccentGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1C3D22),
    onSecondaryContainer = Color(0xFFB4F0B8),
    tertiary = AccentPurple,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark theme for developer aesthetic
    dynamicColor: Boolean = false, // Keep developer GitHub dark palette consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CodeLensColorScheme,
        typography = Typography,
        content = content
    )
}
