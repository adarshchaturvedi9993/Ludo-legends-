package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ====================================================
// LUDO LEGENDS - PREMIUM DESIGN SYSTEM COLOR PALETTE
// Authentic, refined, tournament-grade mobile game aesthetic
// ====================================================

// Base Dark Surfaces (Deep Slate & Midnight Navy)
val SurfaceBackground = Color(0xFF090E1A) // Deep midnight table base
val SurfaceCard = Color(0xFF111B2E)       // Primary card and panel surface
val SurfacePanel = Color(0xFF111B2E)      // Alias for SurfaceCard
val SurfaceElevated = Color(0xFF182640)   // Elevated dialogs, inputs, floating chips
val SurfaceSubtle = Color(0xFF0C1322)     // Sunken track cells & recessed sockets

// Structural Hairline Borders
val BorderHairline = Color(0xFF1E2F4D)    // Default 1dp structural border
val BorderActive = Color(0xFF385380)      // Highlighted / focused border
val BorderGold = Color(0xFFF59E0B)        // Championship gold outline

// Typography Text Hierarchy
val TextPrimary = Color(0xFFF8FAFC)       // Crisp white heading and primary text
val TextSecondary = Color(0xFF94A3B8)     // Soft slate-gray secondary text
val TextMuted = Color(0xFF64748B)         // Muted metadata, captions, disabled text

// Championship Amber-Gold Accents
val AccentPrimary = Color(0xFFF59E0B)     // Primary tournament gold
val AccentPrimaryHover = Color(0xFFD97706)
val StarGold = Color(0xFFFBBF24)          // Safe stars & victory emblems
val GoldHighlight = Color(0xFFFDE68A)     // Specular highlights
val GoldDeep = Color(0xFFB45309)          // Deep shadow gold tone

val GoldGradientColors = listOf(
    Color(0xFFFDE68A),
    Color(0xFFF59E0B),
    Color(0xFFD97706)
)

// Subtle 2-stop metallic rim highlight (used selectively on victory trophies and badges)
val GoldRimGradientColors = listOf(
    Color(0xFFFDE68A),
    Color(0xFFD97706)
)

// Status / Feedback
val StatusSuccess = Color(0xFF10B981)
val StatusDanger = Color(0xFFEF4444)
val StatusWarning = Color(0xFFF59E0B)

// ====================================================
// TOURNAMENT PLAYER TEAM PALETTES
// High-contrast, vibrant, authentic Ludo quadrants
// ====================================================

// RED (Top-Left)
val LudoRed = Color(0xFFEF4444)
val LudoRedDark = Color(0xFF991B1B)
val LudoRedHighlight = Color(0xFFF87171)
val LudoRedGlow = Color(0xFFEF4444)
val LudoRedHomeSurface = Color(0xFF1F1218) // Deep crimson tinted yard base

// GREEN (Top-Right)
val LudoGreen = Color(0xFF10B981)
val LudoGreenDark = Color(0xFF065F46)
val LudoGreenHighlight = Color(0xFF34D399)
val LudoGreenGlow = Color(0xFF10B981)
val LudoGreenHomeSurface = Color(0xFF0E1E18) // Deep emerald tinted yard base

// YELLOW (Bottom-Right)
val LudoYellow = Color(0xFFF59E0B)
val LudoYellowDark = Color(0xFF92400E)
val LudoYellowHighlight = Color(0xFFFBBF24)
val LudoYellowGlow = Color(0xFFF59E0B)
val LudoYellowHomeSurface = Color(0xFF201A10) // Deep amber tinted yard base

// BLUE (Bottom-Left)
val LudoBlue = Color(0xFF3B82F6)
val LudoBlueDark = Color(0xFF1E40AF)
val LudoBlueHighlight = Color(0xFF60A5FA)
val LudoBlueGlow = Color(0xFF3B82F6)
val LudoBlueHomeSurface = Color(0xFF0E182A) // Deep sapphire tinted yard base

// Board Geometry Surfaces
val DarkBoardBackground = Color(0xFF0E1626)
val DarkGridCell = Color(0xFF131D31)
val DarkAlternateCell = Color(0xFF17233B)
val DarkGridBorder = Color(0xFF223250)

// Backward compatibility aliases
val CreamBoardSurface = DarkBoardBackground
val CreamTileSurface = DarkGridCell
val CreamTileBorder = DarkGridBorder
val LudoRedSurface = LudoRedHomeSurface
val LudoGreenSurface = LudoGreenHomeSurface
val LudoYellowSurface = LudoYellowHomeSurface
val LudoBlueSurface = LudoBlueHomeSurface
