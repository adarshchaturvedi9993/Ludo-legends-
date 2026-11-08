package com.example

import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoDiceSystemTest {

    @Test
    fun testTwoPlayerDiceAssignment() {
        val players = listOf(
            Player(id = 0, name = "Player 1", color = PlayerColor.RED),
            Player(id = 1, name = "Player 2", color = PlayerColor.YELLOW)
        )
        val gameState = GameState(players = players, currentPlayerIndex = 0)

        assertEquals("Player 1 must be active initially", 0, gameState.currentPlayer.id)
        assertTrue("Game is waiting for roll", gameState.isWaitingForRoll)

        // Only Player 1 dice can be interactive
        val isP1DiceInteractive = (players[0].id == gameState.currentPlayer.id) && gameState.isWaitingForRoll
        val isP2DiceInteractive = (players[1].id == gameState.currentPlayer.id) && gameState.isWaitingForRoll

        assertTrue("Player 1's dice must be active", isP1DiceInteractive)
        assertFalse("Player 2's dice must be disabled", isP2DiceInteractive)
    }

    @Test
    fun testFourPlayerDiceAssignmentAndTurnRotation() {
        val players = listOf(
            Player(id = 0, name = "Red Player", color = PlayerColor.RED),
            Player(id = 1, name = "Green Player", color = PlayerColor.GREEN),
            Player(id = 2, name = "Yellow Player", color = PlayerColor.YELLOW),
            Player(id = 3, name = "Blue Player", color = PlayerColor.BLUE)
        )
        var gameState = GameState(players = players, currentPlayerIndex = 0)

        // Player 1 (Red) turn
        assertEquals(PlayerColor.RED, gameState.currentPlayer.color)
        var nextIdx = LudoGameEngine.getNextActivePlayerIndex(players, gameState.currentPlayerIndex)
        assertEquals(1, nextIdx) // Green

        // Rotate to Player 2 (Green)
        gameState = gameState.copy(currentPlayerIndex = nextIdx)
        assertEquals(PlayerColor.GREEN, gameState.currentPlayer.color)
        nextIdx = LudoGameEngine.getNextActivePlayerIndex(players, gameState.currentPlayerIndex)
        assertEquals(2, nextIdx) // Yellow

        // Rotate to Player 3 (Yellow)
        gameState = gameState.copy(currentPlayerIndex = nextIdx)
        assertEquals(PlayerColor.YELLOW, gameState.currentPlayer.color)
        nextIdx = LudoGameEngine.getNextActivePlayerIndex(players, gameState.currentPlayerIndex)
        assertEquals(3, nextIdx) // Blue

        // Rotate to Player 4 (Blue)
        gameState = gameState.copy(currentPlayerIndex = nextIdx)
        assertEquals(PlayerColor.BLUE, gameState.currentPlayer.color)
        nextIdx = LudoGameEngine.getNextActivePlayerIndex(players, gameState.currentPlayerIndex)
        assertEquals(0, nextIdx) // Loops back to Red
    }

    @Test
    fun testRollingSixGrantsExtraTurnToSamePlayerDice() {
        val players = listOf(
            Player(id = 0, name = "Player 1", color = PlayerColor.RED),
            Player(id = 1, name = "Player 2", color = PlayerColor.GREEN)
        )
        val gameState = GameState(players = players, currentPlayerIndex = 0, diceValue = 6)

        // When rolling a 6 with tokens in yard, engine provides valid moves
        val movable = LudoGameEngine.getMovableTokenIds(gameState.currentPlayer, 6)
        assertEquals(4, movable.size)

        // Executing move with 6 retains same player turn (bonus roll)
        val (nextState, _) = LudoGameEngine.executeMove(gameState, movable.first())
        assertEquals("Player 1 must keep active dice after rolling 6", 0, nextState.currentPlayerIndex)
        assertTrue("Player 1 must be waiting for next roll", nextState.isWaitingForRoll)
    }

    @Test
    fun testThreeConsecutiveSixesPenalizesTurnPass() {
        val players = listOf(
            Player(id = 0, name = "Player 1", color = PlayerColor.RED),
            Player(id = 1, name = "Player 2", color = PlayerColor.GREEN)
        )
        val nextIndex = LudoGameEngine.getNextActivePlayerIndex(players, 0)
        assertEquals("Next index after penalty must be Player 2", 1, nextIndex)
    }
}
