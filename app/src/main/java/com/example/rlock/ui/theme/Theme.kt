package com.example.rlock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonAccent,
    onPrimary = CrimsonBackground,
    primaryContainer = GlassCrimson,
    onPrimaryContainer = CrimsonTextPrimary,
    secondary = CrimsonGaze,
    onSecondary = CrimsonBackground,
    secondaryContainer = GlassCrimsonHighlight,
    onSecondaryContainer = CrimsonTextSecondary,
    tertiary = GoldStarColor,
    onTertiary = CrimsonBackground,
    tertiaryContainer = GoldGlassBg,
    onTertiaryContainer = CrimsonTextPrimary,
    background = CrimsonBackground,
    onBackground = CrimsonTextPrimary,
    surface = CrimsonSurface,
    onSurface = CrimsonTextPrimary,
    surfaceVariant = GlassCrimsonHighlight,
    onSurfaceVariant = CrimsonTextSecondary,
    outline = CrimsonBorder,
    outlineVariant = CrimsonTextSecondary
)

@Composable
fun RLockTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
