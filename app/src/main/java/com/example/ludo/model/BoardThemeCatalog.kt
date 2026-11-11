package com.example.ludo.model

import androidx.compose.ui.graphics.Color

/**
 * Exclusive Ludo Board Theme model.
 * Includes the default Royal Navy theme and 5 exclusive themes (150 coins each).
 */
data class BoardTheme(
    val id: String,
    val displayName: String,
    val priceCoins: Int = 150,
    val isDefaultUnlocked: Boolean = false,
    val description: String,
    val boardBackground: Color,
    val gridBorder: Color,
    val gridCell: Color,
    val alternateCell: Color,
    val starColor: Color,
    val yardSurfaceRed: Color = Color(0xFF261217),
    val yardSurfaceGreen: Color = Color(0xFF10241A),
    val yardSurfaceYellow: Color = Color(0xFF242012),
    val yardSurfaceBlue: Color = Color(0xFF121C2B),
    val centerStarGradient: List<Color> = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF78350F))
)

object BoardThemeCatalog {
    const val PRICE_PER_THEME = 150

    val THEME_ROYAL_NAVY = BoardTheme(
        id = "theme_royal_navy",
        displayName = "Royal Navy",
        priceCoins = 0,
        isDefaultUnlocked = true,
        description = "Standard midnight sapphire board with brass gold filigree",
        boardBackground = Color(0xFF090E1A),
        gridBorder = Color(0xFF24334D),
        gridCell = Color(0xFF0D1829),
        alternateCell = Color(0xFF112038),
        starColor = Color(0xFFF6C928),
        centerStarGradient = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF78350F))
    )

    // 5 Exclusive Themes (150 coins each)
    val THEME_ROYAL_GOLD = BoardTheme(
        id = "theme_royal_gold",
        displayName = "Royal Gold",
        priceCoins = PRICE_PER_THEME,
        isDefaultUnlocked = false,
        description = "Luxury burnished gold foil with opulent ivory-trimmed cells",
        boardBackground = Color(0xFF18150D),
        gridBorder = Color(0xFF5E491E),
        gridCell = Color(0xFF241F14),
        alternateCell = Color(0xFF2E2719),
        starColor = Color(0xFFFFDF7A),
        yardSurfaceRed = Color(0xFF33161C),
        yardSurfaceGreen = Color(0xFF1A2B1E),
        yardSurfaceYellow = Color(0xFF362E17),
        yardSurfaceBlue = Color(0xFF172233),
        centerStarGradient = listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B), Color(0xFFB45309))
    )

    val THEME_MIDNIGHT_OBSIDIAN = BoardTheme(
        id = "theme_midnight_obsidian",
        displayName = "Midnight Obsidian",
        priceCoins = PRICE_PER_THEME,
        isDefaultUnlocked = false,
        description = "Stealth volcanic glass slate with razor-sharp gunmetal borders",
        boardBackground = Color(0xFF070709),
        gridBorder = Color(0xFF27272A),
        gridCell = Color(0xFF121215),
        alternateCell = Color(0xFF18181C),
        starColor = Color(0xFFE4E4E7),
        yardSurfaceRed = Color(0xFF251114),
        yardSurfaceGreen = Color(0xFF112015),
        yardSurfaceYellow = Color(0xFF221F10),
        yardSurfaceBlue = Color(0xFF111824),
        centerStarGradient = listOf(Color(0xFFFAFAFA), Color(0xFFA1A1AA), Color(0xFF3F3F46))
    )

    val THEME_CYBERPUNK_NEON = BoardTheme(
        id = "theme_cyberpunk_neon",
        displayName = "Cyberpunk Neon",
        priceCoins = PRICE_PER_THEME,
        isDefaultUnlocked = false,
        description = "Futuristic synthwave grid with electric magenta and cyan lines",
        boardBackground = Color(0xFF090614),
        gridBorder = Color(0xFF4C1D95),
        gridCell = Color(0xFF140D2B),
        alternateCell = Color(0xFF1C133C),
        starColor = Color(0xFF06B6D4),
        yardSurfaceRed = Color(0xFF300D22),
        yardSurfaceGreen = Color(0xFF0A2B20),
        yardSurfaceYellow = Color(0xFF30260D),
        yardSurfaceBlue = Color(0xFF0E1A38),
        centerStarGradient = listOf(Color(0xFF67E8F9), Color(0xFFC084FC), Color(0xFFEC4899))
    )

    val THEME_CLASSIC_WOOD = BoardTheme(
        id = "theme_classic_wood",
        displayName = "Classic Wood",
        priceCoins = PRICE_PER_THEME,
        isDefaultUnlocked = false,
        description = "Aged mahogany grain with antique brass studs and warm parchment",
        boardBackground = Color(0xFF1C110A),
        gridBorder = Color(0xFF4A301D),
        gridCell = Color(0xFF2B1C12),
        alternateCell = Color(0xFF352317),
        starColor = Color(0xFFF59E0B),
        yardSurfaceRed = Color(0xFF381518),
        yardSurfaceGreen = Color(0xFF172D1E),
        yardSurfaceYellow = Color(0xFF382F16),
        yardSurfaceBlue = Color(0xFF182436),
        centerStarGradient = listOf(Color(0xFFFEF3C7), Color(0xFFD97706), Color(0xFF78350F))
    )

    val THEME_NEBULA_GALAXY = BoardTheme(
        id = "theme_nebula_galaxy",
        displayName = "Nebula Galaxy",
        priceCoins = PRICE_PER_THEME,
        isDefaultUnlocked = false,
        description = "Deep celestial cosmos bathed in starlight violet and astral dust",
        boardBackground = Color(0xFF0B061A),
        gridBorder = Color(0xFF3B246B),
        gridCell = Color(0xFF160E30),
        alternateCell = Color(0xFF1F1442),
        starColor = Color(0xFFA855F7),
        yardSurfaceRed = Color(0xFF2C0F22),
        yardSurfaceGreen = Color(0xFF0F2625),
        yardSurfaceYellow = Color(0xFF2C2412),
        yardSurfaceBlue = Color(0xFF101938),
        centerStarGradient = listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF4C1D95))
    )

    val allThemes = listOf(
        THEME_ROYAL_NAVY,
        THEME_ROYAL_GOLD,
        THEME_MIDNIGHT_OBSIDIAN,
        THEME_CYBERPUNK_NEON,
        THEME_CLASSIC_WOOD,
        THEME_NEBULA_GALAXY
    )

    fun getThemeById(id: String?): BoardTheme {
        return allThemes.find { it.id == id } ?: THEME_ROYAL_NAVY
    }
}
