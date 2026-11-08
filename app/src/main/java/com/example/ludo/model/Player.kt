package com.example.ludo.model

data class Player(
    val id: Int,
    val name: String,
    val color: PlayerColor,
    val tokens: List<Token> = List(4) { idx -> Token(id = idx, color = color) },
    val rank: Int? = null,
    val sixesCount: Int = 0,
    val capturesCount: Int = 0,
    val isBot: Boolean = false,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM
) {
    val finishedTokensCount: Int get() = tokens.count { it.isFinished }
    val isVictorious: Boolean get() = finishedTokensCount == 4
    val hasTokensInYard: Boolean get() = tokens.any { it.isInYard }
    val hasTokensOnBoard: Boolean get() = tokens.any { it.isOnTrack || it.isInHomeStretch }
}
