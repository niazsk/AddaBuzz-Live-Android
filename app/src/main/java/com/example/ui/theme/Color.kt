package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Backgrounds
val BackgroundDark = Color(0xFF070510)
val BackgroundDeep = Color(0xFF0D0A1C)
val SurfaceDark = Color(0xFF15102A)
val SurfaceCard = Color(0xFF1E173E)
val SurfaceCardBorder = Color(0x408B5CF6)
val SurfaceGlass = Color(0x402A2058)
val SurfaceGlassHigh = Color(0x663B2B7A)

// Brand Accents
val PurplePrimary = Color(0xFF9333EA)
val PurpleAccent = Color(0xFF7928CA)
val VioletBright = Color(0xFFA855F7)
val VioletDeep = Color(0xFF581C87)
val ElectricCyan = Color(0xFF00F0FF)
val SkyBlue = Color(0xFF38BDF8)
val NeonPink = Color(0xFFFF2E93)
val LiveRed = Color(0xFFFF1744)
val GoldCoin = Color(0xFFFFD700)
val EmeraldGreen = Color(0xFF10B981)

// Texts
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Useful Brushes
val BrandGradient = Brush.horizontalGradient(
    colors = listOf(PurpleAccent, PurplePrimary, ElectricCyan)
)

val LivePulseGradient = Brush.horizontalGradient(
    colors = listOf(LiveRed, NeonPink)
)

val HostCrownGradient = Brush.linearGradient(
    colors = listOf(GoldCoin, Color(0xFFFFA500), NeonPink)
)

val GlassCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0x354338CA), Color(0x1A1E173E))
)
