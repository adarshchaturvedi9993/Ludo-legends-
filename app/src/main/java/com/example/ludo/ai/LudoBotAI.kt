package com.example.ludo.ai

import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import kotlin.random.Random

object LudoBotAI {

    /**
     * Chooses the best legal move for [player] from [movableTokenIds].
     * Strictly returns an ID from [movableTokenIds].
     */
    fun chooseMove(
        player: Player,
        gameState: GameState,
        movableTokenIds: Set<Int>
    ): Int {
        require(movableTokenIds.isNotEmpty()) { "movableTokenIds must not be empty" }
        if (movableTokenIds.size == 1) {
            return movableTokenIds.first()
        }

        return when (player.botDifficulty) {
            BotDifficulty.EASY -> chooseEasyMove(player, gameState, movableTokenIds)
            BotDifficulty.MEDIUM -> chooseMediumMove(player, gameState, movableTokenIds)
            BotDifficulty.HARD -> chooseHardMove(player, gameState, movableTokenIds)
        }
    }

    private fun chooseEasyMove(
        player: Player,
        gameState: GameState,
        movableTokenIds: Set<Int>
    ): Int {
        // 75% random choice, 25% greedy heuristic
        if (Random.nextFloat() < 0.75f) {
            return movableTokenIds.random()
        }
        return chooseMediumMove(player, gameState, movableTokenIds)
    }

    private fun chooseMediumMove(
        player: Player,
        gameState: GameState,
        movableTokenIds: Set<Int>
    ): Int {
        var bestScore = Double.NEGATIVE_INFINITY
        var bestTokenId = movableTokenIds.first()

        for (tokenId in movableTokenIds) {
            val token = player.tokens.first { it.id == tokenId }
            val nextToken = LudoGameEngine.computeNewTokenState(token, gameState.diceValue)
            var score = 0.0

            // 1. Can capture opponent
            if (canTokenCapture(nextToken, player, gameState)) {
                score += 100.0
            }

            // 2. Can finish token into goal
            if (nextToken.isFinished) {
                score += 80.0
            }

            // 3. Deploy from yard on roll 6
            if (token.isInYard && gameState.diceValue == 6) {
                score += 65.0
            }

            // 4. Enter safe home corridor
            if (token.isOnTrack && nextToken.isInHomeStretch) {
                score += 50.0
            }

            // 5. General progress forward
            score += nextToken.totalProgress.toDouble()

            if (score > bestScore) {
                bestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }

    private fun chooseHardMove(
        player: Player,
        gameState: GameState,
        movableTokenIds: Set<Int>
    ): Int {
        var bestScore = Double.NEGATIVE_INFINITY
        var bestTokenId = movableTokenIds.first()

        for (tokenId in movableTokenIds) {
            val token = player.tokens.first { it.id == tokenId }
            val nextToken = LudoGameEngine.computeNewTokenState(token, gameState.diceValue)
            var score = 0.0

            // 1. Immediate capture is supreme priority
            if (canTokenCapture(nextToken, player, gameState)) {
                score += 160.0
            }

            // 2. Finishing piece into goal
            if (nextToken.isFinished) {
                score += 130.0
            }

            // 3. Escaping current danger: if token is currently exposed to being captured
            if (isTokenInDanger(token, player, gameState)) {
                score += 90.0
            }

            // 4. Landing on a designated Safe Star tile
            if (nextToken.isOnTrack) {
                val nextTrackIdx = nextToken.color.stepToTrackIndex(nextToken.stepsTraveled)
                if (BoardCoordinates.isSafeTile(nextTrackIdx)) {
                    score += 75.0
                }
            }

            // 5. Entering safe home stretch
            if (token.isOnTrack && nextToken.isInHomeStretch) {
                score += 70.0
            }

            // 6. Deploying from yard on roll 6
            if (token.isInYard && gameState.diceValue == 6) {
                // Highly valuable, especially if few pieces on board
                val piecesOnBoard = player.tokens.count { it.isOnTrack || it.isInHomeStretch }
                score += if (piecesOnBoard == 0) 85.0 else 60.0
            }

            // 7. Danger penalty if landing within 1..6 steps ahead of an opponent
            if (willBeInDanger(nextToken, player, gameState)) {
                score -= 45.0
            }

            // 8. Progressive distance preference
            score += nextToken.totalProgress * 0.8

            if (score > bestScore) {
                bestScore = score
                bestTokenId = tokenId
            }
        }

        return bestTokenId
    }

    private fun canTokenCapture(
        targetToken: Token,
        player: Player,
        gameState: GameState
    ): Boolean {
        if (!targetToken.isOnTrack) return false
        val trackIdx = targetToken.color.stepToTrackIndex(targetToken.stepsTraveled)
        if (BoardCoordinates.isSafeTile(trackIdx)) return false

        for (otherPlayer in gameState.players) {
            if (otherPlayer.id == player.id) continue
            for (oppToken in otherPlayer.tokens) {
                if (oppToken.isOnTrack && oppToken.color.stepToTrackIndex(oppToken.stepsTraveled) == trackIdx) {
                    return true
                }
            }
        }
        return false
    }

    private fun isTokenInDanger(
        token: Token,
        player: Player,
        gameState: GameState
    ): Boolean {
        if (!token.isOnTrack) return false
        val tokenTrackIdx = token.color.stepToTrackIndex(token.stepsTraveled)
        if (BoardCoordinates.isSafeTile(tokenTrackIdx)) return false

        for (otherPlayer in gameState.players) {
            if (otherPlayer.id == player.id) continue
            for (oppToken in otherPlayer.tokens) {
                if (oppToken.isOnTrack) {
                    val oppTrackIdx = oppToken.color.stepToTrackIndex(oppToken.stepsTraveled)
                    val distance = (tokenTrackIdx - oppTrackIdx + 52) % 52
                    if (distance in 1..6) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun willBeInDanger(
        nextToken: Token,
        player: Player,
        gameState: GameState
    ): Boolean {
        return isTokenInDanger(nextToken, player, gameState)
    }
}
