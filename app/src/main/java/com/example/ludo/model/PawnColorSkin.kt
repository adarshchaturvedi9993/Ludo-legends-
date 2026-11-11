package com.example.ludo.model

import androidx.compose.ui.graphics.Color

/**
 * Premium Pawn Color Skin model.
 * Includes the 4 classic default unlocked colors and 6 exclusive unlockable gem/neon tones (25 coins each).
 */
data class PawnColorSkin(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val accentColor: Color,
    val darkShade: Color,
    val surfaceColor: Color,
    val priceCoins: Int = 25,
    val isDefaultUnlocked: Boolean = false,
    val description: String = ""
)

object PawnColorCatalog {
    const val PRICE_PER_SKIN = 25

    val CLASSIC_RED = PawnColorSkin(
        id = "classic_red",
        displayName = "Classic Red",
        primaryColor = Color(0xFFEF4444),
        accentColor = Color(0xFFF87171),
        darkShade = Color(0xFF991B1B),
        surfaceColor = Color(0xFF201118),
        priceCoins = 0,
        isDefaultUnlocked = true,
        description = "Standard Crimson Red"
    )

    val CLASSIC_GREEN = PawnColorSkin(
        id = "classic_green",
        displayName = "Classic Green",
        primaryColor = Color(0xFF18B875),
        accentColor = Color(0xFF34D399),
        darkShade = Color(0xFF065F46),
        surfaceColor = Color(0xFF10201A),
        priceCoins = 0,
        isDefaultUnlocked = true,
        description = "Standard Emerald Green"
    )

    val CLASSIC_YELLOW = PawnColorSkin(
        id = "classic_yellow",
        displayName = "Classic Yellow",
        primaryColor = Color(0xFFF6C928),
        accentColor = Color(0xFFFBBF24),
        darkShade = Color(0xFF92400E),
        surfaceColor = Color(0xFF231F14),
        priceCoins = 0,
        isDefaultUnlocked = true,
        description = "Standard Golden Yellow"
    )

    val CLASSIC_BLUE = PawnColorSkin(
        id = "classic_blue",
        displayName = "Classic Blue",
        primaryColor = Color(0xFF3987F5),
        accentColor = Color(0xFF60A5FA),
        darkShade = Color(0xFF1E40AF),
        surfaceColor = Color(0xFF111A2B),
        priceCoins = 0,
        isDefaultUnlocked = true,
        description = "Standard Royal Blue"
    )

    // 6 New Premium Pawn Colors (25 coins each)
    val RUBY_ROYAL = PawnColorSkin(
        id = "ruby_royal",
        displayName = "Ruby Royal",
        primaryColor = Color(0xFFE11D48),
        accentColor = Color(0xFFFB7185),
        darkShade = Color(0xFF881337),
        surfaceColor = Color(0xFF2A0E18),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Deep precious ruby with lustrous specular glint"
    )

    val NEON_CYAN = PawnColorSkin(
        id = "neon_cyan",
        displayName = "Neon Cyan",
        primaryColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF67E8F9),
        darkShade = Color(0xFF0E7490),
        surfaceColor = Color(0xFF082029),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Electric cyberpunk cyan with vibrant contrast"
    )

    val EMERALD_JADE = PawnColorSkin(
        id = "emerald_jade",
        displayName = "Emerald Jade",
        primaryColor = Color(0xFF059669),
        accentColor = Color(0xFF34D399),
        darkShade = Color(0xFF064E3B),
        surfaceColor = Color(0xFF0A241C),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Imperial jade stone with deep forest saturation"
    )

    val IMPERIAL_AMETHYST = PawnColorSkin(
        id = "imperial_amethyst",
        displayName = "Imperial Amethyst",
        primaryColor = Color(0xFF9333EA),
        accentColor = Color(0xFFC084FC),
        darkShade = Color(0xFF581C87),
        surfaceColor = Color(0xFF1E0C30),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Majestic royal purple with high-clarity violet tones"
    )

    val ROSE_QUARTZ = PawnColorSkin(
        id = "rose_quartz",
        displayName = "Rose Quartz",
        primaryColor = Color(0xFFF43F5E),
        accentColor = Color(0xFFFDA4AF),
        darkShade = Color(0xFF9F1239),
        surfaceColor = Color(0xFF2E0F1B),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Blush coral crystal with soft gold highlights"
    )

    val AMBER_SUNSET = PawnColorSkin(
        id = "amber_sunset",
        displayName = "Amber Sunset",
        primaryColor = Color(0xFFEA580C),
        accentColor = Color(0xFFFDBA74),
        darkShade = Color(0xFF7C2D12),
        surfaceColor = Color(0xFF2B1408),
        priceCoins = PRICE_PER_SKIN,
        isDefaultUnlocked = false,
        description = "Fiery molten amber with radiant sunset glow"
    )

    val allClassicColors = listOf(
        CLASSIC_RED,
        CLASSIC_GREEN,
        CLASSIC_YELLOW,
        CLASSIC_BLUE
    )

    val allPremiumColors = listOf(
        RUBY_ROYAL,
        NEON_CYAN,
        EMERALD_JADE,
        IMPERIAL_AMETHYST,
        ROSE_QUARTZ,
        AMBER_SUNSET
    )

    val allColors = allClassicColors + allPremiumColors

    fun getSkinById(id: String?): PawnColorSkin? {
        if (id == null) return null
        return allColors.find { it.id == id }
    }

    fun getSkinForPlayerColor(color: PlayerColor): PawnColorSkin {
        return when (color) {
            PlayerColor.RED -> CLASSIC_RED
            PlayerColor.GREEN -> CLASSIC_GREEN
            PlayerColor.YELLOW -> CLASSIC_YELLOW
            PlayerColor.BLUE -> CLASSIC_BLUE
        }
    }
}
