package com.example.ludo.engine

import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState

sealed class MoveOutcome {
    data class Moved(
        val capturedPlayerColor: PlayerColor? = null,
        val capturedTokenId: Int? = null,
        val capturedPlayerIndex: Int? = null,
        val tokenFinished: Boolean = false,
        val finishedTokenId: Int? = null,
        val finishOrder: Int = 0,
        val grantedExtraTurn: Boolean = false,
        val extraTurnReason: String? = null
    ) : MoveOutcome()
    object CannotMove : MoveOutcome()
}

object LudoGameEngine {

    /**
     * Calculates whether a specific token can legally move with the given roll.
     */
    fun canTokenMove(token: Token, roll: Int): Boolean {
        return when (token.state) {
            TokenState.YARD -> roll == 6
            TokenState.TRACK -> {
                val newSteps = token.stepsTraveled + roll
                if (newSteps <= 50) {
                    true
                } else {
                    val homeStep = newSteps - 50
                    homeStep <= 6
                }
            }
            TokenState.HOME_STRETCH -> {
                token.homeStep + roll <= 6
            }
            TokenState.FINISHED -> false
        }
    }

    /**
     * Returns the set of token IDs for the current player that can legally move with the given roll.
     */
    fun getMovableTokenIds(player: Player, roll: Int): Set<Int> {
        return player.tokens
            .filter { canTokenMove(it, roll) }
            .map { it.id }
            .toSet()
    }

    /**
     * Calculates the new token state after moving by [roll] steps.
     */
    fun computeNewTokenState(token: Token, roll: Int): Token {
        return when (token.state) {
            TokenState.YARD -> {
                if (roll == 6) {
                    token.copy(
                        state = TokenState.TRACK,
                        stepsTraveled = 0,
                        homeStep = 0
                    )
                } else token
            }
            TokenState.TRACK -> {
                val newSteps = token.stepsTraveled + roll
                if (newSteps <= 50) {
                    token.copy(stepsTraveled = newSteps)
                } else {
                    val homeStep = newSteps - 50
                    if (homeStep == 6) {
                        token.copy(
                            state = TokenState.FINISHED,
                            stepsTraveled = 50,
                            homeStep = 6
                        )
                    } else if (homeStep <= 5) {
                        token.copy(
                            state = TokenState.HOME_STRETCH,
                            stepsTraveled = 50,
                            homeStep = homeStep
                        )
                    } else {
                        token // overshoot, invalid
                    }
                }
            }
            TokenState.HOME_STRETCH -> {
                val newHomeStep = token.homeStep + roll
                if (newHomeStep == 6) {
                    token.copy(
                        state = TokenState.FINISHED,
                        homeStep = 6
                    )
                } else if (newHomeStep <= 5) {
                    token.copy(homeStep = newHomeStep)
                } else {
                    token // overshoot
                }
            }
            TokenState.FINISHED -> token
        }
    }

    /**
     * Executes the token movement, resolves captures, rewards bonus turns, and updates player statistics.
     */
    fun executeMove(
        currentState: GameState,
        tokenId: Int
    ): Pair<GameState, MoveOutcome> {
        val currPlayer = currentState.currentPlayer
        val token = currPlayer.tokens.firstOrNull { it.id == tokenId }
            ?: return currentState to MoveOutcome.CannotMove

        if (!canTokenMove(token, currentState.diceValue)) {
            return currentState to MoveOutcome.CannotMove
        }

        val updatedToken = computeNewTokenState(token, currentState.diceValue)
        var capturedColor: PlayerColor? = null
        var capturedTokenId: Int? = null
        var capturedPlayerIndex: Int? = null
        var isCaptured = false

        // Check for capture on the track
        val allPlayers = currentState.players.toMutableList()
        if (updatedToken.state == TokenState.TRACK) {
            val landTrackIndex = updatedToken.color.stepToTrackIndex(updatedToken.stepsTraveled)
            val isSafe = BoardCoordinates.isSafeTile(landTrackIndex)

            if (!isSafe) {
                // Check other players' tokens
                for (pIdx in allPlayers.indices) {
                    if (allPlayers[pIdx].id == currPlayer.id) continue
                    val opponentTokens = allPlayers[pIdx].tokens.toMutableList()
                    var opponentChanged = false

                    for (tIdx in opponentTokens.indices) {
                        val oppToken = opponentTokens[tIdx]
                        if (oppToken.state == TokenState.TRACK &&
                            oppToken.color.stepToTrackIndex(oppToken.stepsTraveled) == landTrackIndex
                        ) {
                            // Capture this token!
                            opponentTokens[tIdx] = oppToken.copy(
                                state = TokenState.YARD,
                                stepsTraveled = 0,
                                homeStep = 0
                            )
                            capturedColor = oppToken.color
                            capturedTokenId = oppToken.id
                            capturedPlayerIndex = pIdx
                            isCaptured = true
                            opponentChanged = true
                            break // Capture one token per landing
                        }
                    }

                    if (opponentChanged) {
                        allPlayers[pIdx] = allPlayers[pIdx].copy(tokens = opponentTokens)
                    }
                    if (isCaptured) break
                }
            }
        }

        // Update current player's token and statistics
        val tokenFinished = updatedToken.isFinished && !token.isFinished
        val nextFinishOrder = if (tokenFinished) currPlayer.tokens.count { it.isFinished } + 1 else 0
        val finalToken = if (tokenFinished) updatedToken.copy(finishOrder = nextFinishOrder) else updatedToken

        val updatedTokens = currPlayer.tokens.toMutableList()
        val tokenIndex = updatedTokens.indexOfFirst { it.id == tokenId }
        updatedTokens[tokenIndex] = finalToken

        val capturesAdded = if (isCaptured) 1 else 0

        val updatedCurrentPlayer = currPlayer.copy(
            tokens = updatedTokens,
            capturesCount = currPlayer.capturesCount + capturesAdded
        )

        val currPlayerIdx = allPlayers.indexOfFirst { it.id == currPlayer.id }
        allPlayers[currPlayerIdx] = updatedCurrentPlayer

        // Check if current player just won or got ranked
        var newWinner = currentState.winner
        val updatedRankings = currentState.rankings.toMutableList()

        if (updatedCurrentPlayer.isVictorious && updatedCurrentPlayer.rank == null) {
            val nextRank = updatedRankings.size + 1
            val rankedPlayer = updatedCurrentPlayer.copy(rank = nextRank)
            allPlayers[currPlayerIdx] = rankedPlayer
            updatedRankings.add(rankedPlayer)
            if (newWinner == null) {
                newWinner = rankedPlayer
            }
        }

        // Extra turn rules:
        // 1. Rolled a 6 (and didn't exceed 3 consecutive)
        // 2. Captured an opponent token
        // 3. Token reached finish goal
        val rolledSix = currentState.diceValue == 6
        val extraTurnAllowed = rolledSix || isCaptured || tokenFinished

        val reason = when {
            tokenFinished -> "Token Finished! Bonus Turn!"
            isCaptured -> "Captured ${capturedColor?.displayName}! Bonus Turn!"
            rolledSix -> "Rolled 6! Extra Turn!"
            else -> null
        }

        // Determine next player
        val (nextPlayerIndex, passTurn) = if (extraTurnAllowed) {
            currentState.currentPlayerIndex to false
        } else {
            getNextActivePlayerIndex(allPlayers, currentState.currentPlayerIndex) to true
        }

        val nextStatus = if (!passTurn && reason != null) {
            "${allPlayers[nextPlayerIndex].name}: $reason"
        } else {
            "${allPlayers[nextPlayerIndex].name}'s turn to roll"
        }

        val updatedState = currentState.copy(
            players = allPlayers,
            currentPlayerIndex = nextPlayerIndex,
            isWaitingForRoll = true,
            movableTokenIds = emptySet(),
            canRollAgain = !passTurn,
            winner = newWinner,
            rankings = updatedRankings,
            statusMessage = nextStatus,
            turnCount = if (passTurn) currentState.turnCount + 1 else currentState.turnCount,
            lastCapturedPlayerColor = capturedColor
        )

        return updatedState to MoveOutcome.Moved(
            capturedPlayerColor = capturedColor,
            capturedTokenId = capturedTokenId,
            capturedPlayerIndex = capturedPlayerIndex,
            tokenFinished = tokenFinished,
            finishedTokenId = if (tokenFinished) tokenId else null,
            finishOrder = nextFinishOrder,
            grantedExtraTurn = !passTurn,
            extraTurnReason = reason
        )
    }

    /**
     * Advances to the next active player who hasn't already completed all 4 tokens.
     */
    fun getNextActivePlayerIndex(players: List<Player>, currentIndex: Int): Int {
        var next = (currentIndex + 1) % players.size
        var attempts = 0
        while (attempts < players.size) {
            if (!players[next].isVictorious) {
                return next
            }
            next = (next + 1) % players.size
            attempts++
        }
        return (currentIndex + 1) % players.size
    }
}
