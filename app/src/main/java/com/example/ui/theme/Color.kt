package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// LUDO LEGENDS - UNIFIED DESIGN SYSTEM COLORS
// ==========================================

// Surface Ramp (Dark Navy "Veeru" Theme)
val SurfaceBackground = Color(0xFF080F22) // Deep navy base background
val SurfaceCard = Color(0xFF101B33)       // Primary card & panel container
val SurfacePanel = Color(0xFF101B33)      // Alias for SurfaceCard
val SurfaceElevated = Color(0xFF152441)   // Elevated cards, dialogs & floating controls
val SurfaceSubtle = Color(0xFF0B1428)     // Recessed socket & sunken track tiles

// Hairline Borders & Structural Outlines
val BorderHairline = Color(0xFF293B5B)    // Default 1dp hairline border
val BorderActive = Color(0xFF3F5886)      // Highlighted/active border
val BorderGold = Color(0xFFF5C928)        // Gold rim border

// Typography Text Hierarchy
val TextPrimary = Color(0xFFF5F7FC)       // Crisp white heading and body
val TextSecondary = Color(0xFFA2AEC5)     // Soft slate-blue secondary text
val TextMuted = Color(0xFF6B7A99)         // Muted metadata & disabled text

// Royal Gold Accents
val AccentPrimary = Color(0xFFF5C928)     // Primary brand gold
val AccentPrimaryHover = Color(0xFFE5B920)
val StarGold = Color(0xFFF5C928)          // Safe stars & victory crowns
val GoldHighlight = Color(0xFFFFE078)     // Specular shimmer & bevel highlights
val GoldDeep = Color(0xFF8A6A12)          // Deep shadow tone for metallic gradients

val GoldGradientColors = listOf(
    Color(0xFFFFE078),
    Color(0xFFF5C928),
    Color(0xFFD69E08)
)

val GoldRimGradientColors = listOf(
    Color(0xFFFFE078),
    Color(0xFFF5C928),
    Color(0xFF8A6A12),
    Color(0xFFF5C928),
    Color(0xFFFFE078)
)

// Status / Game Outcomes
val StatusSuccess = Color(0xFF22C55E)
val StatusDanger = Color(0xFFEF4444)
val StatusWarning = Color(0xFFF59E0B)

// ==========================================
// TOURNAMENT PLAYER PALETTES (Neon-glow 3D)
// ==========================================

// RED (Top-Left)
val LudoRed = Color(0xFFEF4444)
val LudoRedDark = Color(0xFF991B1B)
val LudoRedHighlight = Color(0xFFF87171)
val LudoRedGlow = Color(0xFFFF6B6B)
val LudoRedHomeSurface = Color(0xFF201118) // Dark red tinted yard surface

// GREEN (Top-Right)
val LudoGreen = Color(0xFF18B875)
val LudoGreenDark = Color(0xFF065F46)
val LudoGreenHighlight = Color(0xFF34D399)
val LudoGreenGlow = Color(0xFF4ADE80)
val LudoGreenHomeSurface = Color(0xFF10201A) // Dark green tinted yard surface

// YELLOW (Bottom-Right)
val LudoYellow = Color(0xFFF6C928)
val LudoYellowDark = Color(0xFF92400E)
val LudoYellowHighlight = Color(0xFFFBBF24)
val LudoYellowGlow = Color(0xFFFDE047)
val LudoYellowHomeSurface = Color(0xFF231F14) // Dark yellow tinted yard surface

// BLUE (Bottom-Left)
val LudoBlue = Color(0xFF3987F5)
val LudoBlueDark = Color(0xFF1E40AF)
val LudoBlueHighlight = Color(0xFF60A5FA)
val LudoBlueGlow = Color(0xFF93C5FD)
val LudoBlueHomeSurface = Color(0xFF111A2B) // Dark blue tinted yard surface

// Board Geometry Surfaces
val DarkBoardBackground = Color(0xFF101B33)
val DarkGridCell = Color(0xFF152441)
val DarkAlternateCell = Color(0xFF1A2C4D)
val DarkGridBorder = Color(0xFF293B5B)

// Backward compatibility aliases
val CreamBoardSurface = DarkBoardBackground
val CreamTileSurface = DarkGridCell
val CreamTileBorder = DarkGridBorder
val LudoRedSurface = LudoRedHomeSurface
val LudoGreenSurface = LudoGreenHomeSurface
val LudoYellowSurface = LudoYellowHomeSurface
val LudoBlueSurface = LudoBlueHomeSurface
