package com.example.ludo.model

import androidx.compose.ui.graphics.Color

/**
 * Authentic Ludo player colors in standard clockwise order:
 * RED (Top-Left) -> GREEN (Top-Right) -> YELLOW (Bottom-Right) -> BLUE (Bottom-Left)
 */
enum class PlayerColor(
    val displayName: String,
    val primaryColor: Color,
    val surfaceColor: Color,
    val accentColor: Color,
    val darkShade: Color,
    val startIndex: Int,      // Perimeter index (0..51) where token enters board
    val homeEntryIndex: Int,  // Perimeter index where token turns into home stretch
    val yardRow: Float,       // Center row of yard in 15x15 grid
    val yardCol: Float        // Center col of yard in 15x15 grid
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFEF4444),
        surfaceColor = Color(0xFF201118),
        accentColor = Color(0xFFF87171),
        darkShade = Color(0xFF991B1B),
        startIndex = 0,
        homeEntryIndex = 50,
        yardRow = 2.5f,
        yardCol = 2.5f
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF18B875),
        surfaceColor = Color(0xFF10201A),
        accentColor = Color(0xFF34D399),
        darkShade = Color(0xFF065F46),
        startIndex = 13,
        homeEntryIndex = 11,
        yardRow = 2.5f,
        yardCol = 11.5f
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFF6C928),
        surfaceColor = Color(0xFF231F14),
        accentColor = Color(0xFFFBBF24),
        darkShade = Color(0xFF92400E),
        startIndex = 26,
        homeEntryIndex = 24,
        yardRow = 11.5f,
        yardCol = 11.5f
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF3987F5),
        surfaceColor = Color(0xFF111A2B),
        accentColor = Color(0xFF60A5FA),
        darkShade = Color(0xFF1E40AF),
        startIndex = 39,
        homeEntryIndex = 37,
        yardRow = 11.5f,
        yardCol = 2.5f
    );

    fun stepToTrackIndex(stepsTraveled: Int): Int {
        return (startIndex + stepsTraveled) % 52
    }
}
