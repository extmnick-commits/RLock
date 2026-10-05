package com.example.rlock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GlassTealColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = DarkTealBg,
    primaryContainer = GlassCardBg,
    onPrimaryContainer = TextPrimaryTeal,
    secondary = MintGaugeProgress,
    onSecondary = DarkTealBg,
    secondaryContainer = GlassCardHeaderBg,
    onSecondaryContainer = TextSecondaryTeal,
    tertiary = GoldStarColor,
    onTertiary = DarkTealBg,
    tertiaryContainer = GoldGlassBg,
    onTertiaryContainer = TextPrimaryTeal,
    background = DarkTealBg,
    onBackground = TextPrimaryTeal,
    surface = GlassCardBg,
    onSurface = TextPrimaryTeal,
    surfaceVariant = GlassCardHeaderBg,
    onSurfaceVariant = TextSecondaryTeal,
    outline = GlassCardBorder,
    outlineVariant = TextMutedTeal
)

@Composable
fun RLockTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassTealColorScheme,
        content = content
    )
}
