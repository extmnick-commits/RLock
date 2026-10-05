package com.example.rlock.ui.theme

import androidx.compose.ui.graphics.Color

// RLock Glassmorphism Dark Crimson Theme Palette

// Background & Surfaces
val CrimsonBackground = Color(0xFF0F0A0B)       // Ultra-dark tinted obsidian
val CrimsonSurface = Color(0xFF1A0E10)          // Slightly lighter card base
val GlassCrimson = Color(0x333D1418)            // Translucent glass fill (~20% opacity)
val GlassCrimsonHighlight = Color(0x555C1A21)   // Active/Displaced translucent fill

// Accents & Borders
val CrimsonAccent = Color(0xFFE53935)           // Vibrant primary crimson
val CrimsonGaze = Color(0xFFFF5252)             // High-contrast gauge fill
val CrimsonBorder = Color(0x44FF5252)           // Sleek 1dp translucent glass stroke
val CrimsonTextPrimary = Color(0xFFFCE8E9)      // Soft white with faint warm tint
val CrimsonTextSecondary = Color(0xFFB39295)    // Muted tinted gray

// Additional Glass & UI colors
val GlassNavBarBg = Color(0xCC140B0D)            // Semi-transparent dark crimson blur
val GlassDialogBg = Color(0xF51A0E10)            // Elevated dialog fill
val CrimsonGaugeTrack = Color(0x22FF5252)        // Gauge track background

// Gold Appointment Glass (Preserved for priority appointment styling)
val GoldGlassBg = Color(0x3B332B14)
val GoldGlassBorder = Color(0xFFD4AF37)
val GoldStarColor = Color(0xFFFFD54F)

// Compatibility aliases
val DarkTealBg = CrimsonBackground
val DarkTealGradientEnd = Color(0xFF0A0607)
val GlassCardBg = GlassCrimson
val GlassCardBorder = CrimsonBorder
val GlassCardHeaderBg = GlassCrimsonHighlight
val CyanAccent = CrimsonAccent
val CyanGlow = Color(0x80E53935)
val MintGaugeProgress = CrimsonGaze
val MintGaugeTrack = CrimsonGaugeTrack
val TextPrimaryTeal = CrimsonTextPrimary
val TextSecondaryTeal = CrimsonTextSecondary
val TextMutedTeal = CrimsonTextSecondary
val AmberNoticeBg = GoldGlassBg
val AmberNoticeBorder = GoldGlassBorder
val AmberNoticeText = GoldStarColor
val AppointmentBorderColor = GoldGlassBorder
val AppointmentBgColor = GoldGlassBg
