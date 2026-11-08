package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LudoDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = SurfaceBackground,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfacePanel,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderHairline,
    outlineVariant = BorderActive
)

@Composable
fun LudoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LudoDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LudoTheme(content = content)
}
