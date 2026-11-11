package com.example.ludo.model

import androidx.compose.ui.graphics.Color

data class Player(
    val id: Int,
    val name: String,
    val color: PlayerColor,
    val tokens: List<Token> = List(4) { idx -> Token(id = idx, color = color) },
    val rank: Int? = null,
    val sixesCount: Int = 0,
    val capturesCount: Int = 0,
    val isBot: Boolean = false,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    val customSkinId: String? = null
) {
    val finishedTokensCount: Int get() = tokens.count { it.isFinished }
    val isVictorious: Boolean get() = finishedTokensCount == 4
    val hasTokensInYard: Boolean get() = tokens.any { it.isInYard }
    val hasTokensOnBoard: Boolean get() = tokens.any { it.isOnTrack || it.isInHomeStretch }

    val effectiveSkin: PawnColorSkin
        get() = PawnColorCatalog.getSkinById(customSkinId) ?: PawnColorCatalog.getSkinForPlayerColor(color)

    val effectivePrimaryColor: Color get() = effectiveSkin.primaryColor
    val effectiveAccentColor: Color get() = effectiveSkin.accentColor
    val effectiveDarkShade: Color get() = effectiveSkin.darkShade
    val effectiveSurfaceColor: Color get() = effectiveSkin.surfaceColor
}
