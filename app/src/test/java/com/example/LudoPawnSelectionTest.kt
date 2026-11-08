package com.example

import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoPawnSelectionTest {

    @Test
    fun testRollingSixIdentifiesYardAndTrackPawns() {
        // Player has 3 tokens in yard and 1 token on track
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 1, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 2, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 3, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 10)
        )
        val player = Player(id = 0, name = "Red", color = PlayerColor.RED, tokens = tokens)

        val movableOnSix = LudoGameEngine.getMovableTokenIds(player, 6)
        // All 4 tokens are eligible: 3 can spawn from yard, 1 can advance on track
        assertEquals(4, movableOnSix.size)
        assertTrue(movableOnSix.contains(0))
        assertTrue(movableOnSix.contains(1))
        assertTrue(movableOnSix.contains(2))
        assertTrue(movableOnSix.contains(3))
    }

    @Test
    fun testRollingOneToFiveWithOnlyYardTokensYieldsNoMoves() {
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 1, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 2, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 3, color = PlayerColor.RED, state = TokenState.YARD)
        )
        val player = Player(id = 0, name = "Red", color = PlayerColor.RED, tokens = tokens)

        for (roll in 1..5) {
            val movable = LudoGameEngine.getMovableTokenIds(player, roll)
            assertTrue("No pawns should be eligible on roll $roll with all in yard", movable.isEmpty())
        }
    }

    @Test
    fun testSingleEligiblePawnIdentification() {
        // 1 token on track, 3 in yard
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 15),
            Token(id = 1, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 2, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 3, color = PlayerColor.RED, state = TokenState.YARD)
        )
        val player = Player(id = 0, name = "Red", color = PlayerColor.RED, tokens = tokens)

        val movable = LudoGameEngine.getMovableTokenIds(player, 3)
        // Exactly one pawn is eligible -> auto-move CUJ
        assertEquals(1, movable.size)
        assertEquals(0, movable.first())
    }

    @Test
    fun testMultipleEligiblePawnsIdentification() {
        // 2 tokens on track, 2 in yard
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 8),
            Token(id = 1, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 22),
            Token(id = 2, color = PlayerColor.RED, state = TokenState.YARD),
            Token(id = 3, color = PlayerColor.RED, state = TokenState.YARD)
        )
        val player = Player(id = 0, name = "Red", color = PlayerColor.RED, tokens = tokens)

        val movable = LudoGameEngine.getMovableTokenIds(player, 4)
        // Exactly two pawns are eligible -> bottom-sheet selection CUJ
        assertEquals(2, movable.size)
        assertTrue(movable.contains(0))
        assertTrue(movable.contains(1))
        assertFalse(movable.contains(2))
        assertFalse(movable.contains(3))
    }

    @Test
    fun testHomeStretchExactRollValidation() {
        // Token is at homeStep 4 (needs exact 2 to finish at 6)
        val token = Token(id = 0, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 4)

        // 1 is legal (advances to 5)
        assertTrue(LudoGameEngine.canTokenMove(token, 1))
        // 2 is legal (finishes at 6)
        assertTrue(LudoGameEngine.canTokenMove(token, 2))
        // 3, 4, 5, 6 are illegal (overshoot)
        assertFalse(LudoGameEngine.canTokenMove(token, 3))
        assertFalse(LudoGameEngine.canTokenMove(token, 4))
        assertFalse(LudoGameEngine.canTokenMove(token, 5))
        assertFalse(LudoGameEngine.canTokenMove(token, 6))
    }

    @Test
    fun testCaptureOpponentPawnAndGrantExtraTurn() {
        // Red start index is 0. Token travels 14 steps -> track index = (0 + 14) % 52 = 14 (not safe).
        // Yellow token is also at track index 14.
        val redToken = Token(id = 0, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 10)
        val yellowToken = Token(id = 10, color = PlayerColor.YELLOW, state = TokenState.TRACK, stepsTraveled = 40) // (26 + 40) % 52 = 14

        val redPlayer = Player(id = 0, name = "Red", color = PlayerColor.RED, tokens = listOf(redToken))
        val yellowPlayer = Player(id = 1, name = "Yellow", color = PlayerColor.YELLOW, tokens = listOf(yellowToken))

        val state = GameState(
            players = listOf(redPlayer, yellowPlayer),
            currentPlayerIndex = 0,
            diceValue = 4 // 10 + 4 = 14
        )

        val (nextState, outcome) = LudoGameEngine.executeMove(state, redToken.id)

        assertTrue("Outcome must be Moved", outcome is MoveOutcome.Moved)
        val moved = outcome as MoveOutcome.Moved
        assertEquals("Must capture yellow player", PlayerColor.YELLOW, moved.capturedPlayerColor)
        assertTrue("Must grant extra turn on capture", moved.grantedExtraTurn)

        // Yellow token must be sent back to Yard
        val updatedYellow = nextState.players.first { it.color == PlayerColor.YELLOW }
        val capturedPawn = updatedYellow.tokens.first { it.id == yellowToken.id }
        assertEquals(TokenState.YARD, capturedPawn.state)
        assertEquals(0, capturedPawn.stepsTraveled)
    }

    @Test
    fun testEnteringHomeAndWinningGame() {
        // Red has 3 finished tokens, and 1 token at homeStep 5 (needs 1 to finish)
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.RED, state = TokenState.FINISHED, homeStep = 6),
            Token(id = 1, color = PlayerColor.RED, state = TokenState.FINISHED, homeStep = 6),
            Token(id = 2, color = PlayerColor.RED, state = TokenState.FINISHED, homeStep = 6),
            Token(id = 3, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5)
        )
        val redPlayer = Player(id = 0, name = "Red Player", color = PlayerColor.RED, tokens = tokens)
        val greenPlayer = Player(id = 1, name = "Green Player", color = PlayerColor.GREEN)

        val state = GameState(
            players = listOf(redPlayer, greenPlayer),
            currentPlayerIndex = 0,
            diceValue = 1
        )

        val (nextState, outcome) = LudoGameEngine.executeMove(state, 3)

        assertTrue(outcome is MoveOutcome.Moved)
        val moved = outcome as MoveOutcome.Moved
        assertTrue("Token must finish", moved.tokenFinished)

        val updatedRed = nextState.players.first { it.id == 0 }
        assertTrue("All 4 tokens must be finished", updatedRed.isVictorious)
        assertNotNull("Winner must be recorded", nextState.winner)
        assertEquals("Red Player", nextState.winner?.name)
    }
}
