package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = VioletDeep,
    onPrimaryContainer = Color.White,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF041B26),
    secondaryContainer = Color(0xFF0C3D52),
    onSecondaryContainer = Color.White,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = LiveRed
)

@Composable
fun AddaBuzzTheme(
    darkTheme: Boolean = true, // Default to deep black luxury dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AddaBuzzTheme(darkTheme = true, content = content)
}
