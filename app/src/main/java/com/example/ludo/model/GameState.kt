package com.example.ludo.model

data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isDiceRolling: Boolean = false,
    val isWaitingForRoll: Boolean = true,
    val movableTokenIds: Set<Int> = emptySet(),
    val consecutiveSixes: Int = 0,
    val canRollAgain: Boolean = false,
    val winner: Player? = null,
    val rankings: List<Player> = emptyList(),
    val statusMessage: String = "",
    val turnCount: Int = 1,
    val isPaused: Boolean = false,
    val lastCapturedPlayerColor: PlayerColor? = null
) {
    val currentPlayer: Player
        get() = players[currentPlayerIndex.coerceIn(0, players.size - 1)]

    val isGameOver: Boolean
        get() = winner != null || (rankings.size >= players.size - 1 && players.size > 1)
}
