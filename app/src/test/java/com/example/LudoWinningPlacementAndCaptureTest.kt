package com.example

import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.BoardStyle
import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.GameState
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class LudoWinningPlacementAndCaptureTest {

    @Test
    fun testFinishedSlotCoordinatesAreDistinctAndNonOverlapping() {
        PlayerColor.values().forEach { color ->
            val slots = (1..4).map { order ->
                BoardCoordinates.finishedSlot(color, order)
            }

            // Exactly 4 unique coordinates per color
            val uniqueCoords = slots.toSet()
            assertEquals("Color $color must have 4 unique finish coordinates", 4, uniqueCoords.size)

            // Verify minimum separation distance between each pair (no stacking!)
            for (i in 0 until 4) {
                for (j in i + 1 until 4) {
                    val p1 = slots[i]
                    val p2 = slots[j]
                    val dRow = p1.row - p2.row
                    val dCol = p1.col - p2.col
                    val dist = sqrt((dRow * dRow + dCol * dCol).toDouble())
                    assertTrue(
                        "Slots $i and $j for color $color must be separated by at least 0.25 grid units (was $dist)",
                        dist >= 0.25
                    )
                }
            }

            // Verify all finish coordinates are within central goal area (row and col in 6.0..8.0)
            slots.forEachIndexed { idx, coord ->
                assertTrue(
                    "Finish slot ${idx + 1} for $color row must be within 6.0..8.0 (was ${coord.row})",
                    coord.row in 6.0f..8.0f
                )
                assertTrue(
                    "Finish slot ${idx + 1} for $color col must be within 6.0..8.0 (was ${coord.col})",
                    coord.col in 6.0f..8.0f
                )
            }
        }
    }

    @Test
    fun testFinishOrderIncrementsSequentiallyForFinishedTokens() {
        val player = Player(
            id = 0,
            name = "Champion",
            color = PlayerColor.RED,
            tokens = listOf(
                Token(id = 0, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5),
                Token(id = 1, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5),
                Token(id = 2, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5),
                Token(id = 3, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5)
            )
        )

        var state = GameState(
            players = listOf(player),
            currentPlayerIndex = 0,
            diceValue = 1
        )

        // 1st token finishes
        val (state1, outcome1) = LudoGameEngine.executeMove(state, 0)
        assertTrue(outcome1 is MoveOutcome.Moved)
        val moved1 = outcome1 as MoveOutcome.Moved
        assertTrue(moved1.tokenFinished)
        assertEquals(1, moved1.finishOrder)
        assertEquals(0, moved1.finishedTokenId)
        val t0 = state1.currentPlayer.tokens.first { it.id == 0 }
        assertEquals(1, t0.finishOrder)
        assertEquals(TokenState.FINISHED, t0.state)

        // 2nd token finishes
        state = state1.copy(diceValue = 1)
        val (state2, outcome2) = LudoGameEngine.executeMove(state, 1)
        assertTrue(outcome2 is MoveOutcome.Moved)
        val moved2 = outcome2 as MoveOutcome.Moved
        assertTrue(moved2.tokenFinished)
        assertEquals(2, moved2.finishOrder)
        assertEquals(1, moved2.finishedTokenId)
        val t1 = state2.currentPlayer.tokens.first { it.id == 1 }
        assertEquals(2, t1.finishOrder)

        // 3rd token finishes
        state = state2.copy(diceValue = 1)
        val (state3, outcome3) = LudoGameEngine.executeMove(state, 2)
        assertTrue(outcome3 is MoveOutcome.Moved)
        val moved3 = outcome3 as MoveOutcome.Moved
        assertEquals(3, moved3.finishOrder)
        assertEquals(2, moved3.finishedTokenId)
        val t2 = state3.currentPlayer.tokens.first { it.id == 2 }
        assertEquals(3, t2.finishOrder)

        // 4th token finishes -> winning game!
        state = state3.copy(diceValue = 1)
        val (state4, outcome4) = LudoGameEngine.executeMove(state, 3)
        assertTrue(outcome4 is MoveOutcome.Moved)
        val moved4 = outcome4 as MoveOutcome.Moved
        assertEquals(4, moved4.finishOrder)
        assertEquals(3, moved4.finishedTokenId)
        val t3 = state4.currentPlayer.tokens.first { it.id == 3 }
        assertEquals(4, t3.finishOrder)
        assertTrue(state4.players.first().isVictorious)
    }

    @Test
    fun testCaptureOutcomeProvidesAttackerAndVictimMetadata() {
        val redToken = Token(id = 2, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 20)
        val blueToken = Token(id = 7, color = PlayerColor.BLUE, state = TokenState.TRACK, stepsTraveled = 33) // (39 + 33) % 52 = 20

        val redPlayer = Player(id = 0, name = "Red Master", color = PlayerColor.RED, tokens = listOf(redToken))
        val bluePlayer = Player(id = 1, name = "Blue Rival", color = PlayerColor.BLUE, tokens = listOf(blueToken))

        val state = GameState(
            players = listOf(redPlayer, bluePlayer),
            currentPlayerIndex = 0,
            diceValue = 4 // Red moves 20 -> 24. Let's make blue be at 24:
        )
        // Blue start index is 39. Steps to 24: (39 + steps) % 52 = 24 => steps = (24 - 39 + 52) = 37
        val blueAt24 = blueToken.copy(stepsTraveled = 37)
        val stateWithBlueAt24 = state.copy(
            players = listOf(redPlayer, bluePlayer.copy(tokens = listOf(blueAt24)))
        )

        val (nextState, outcome) = LudoGameEngine.executeMove(stateWithBlueAt24, redToken.id)
        assertTrue(outcome is MoveOutcome.Moved)
        val moved = outcome as MoveOutcome.Moved

        assertEquals(PlayerColor.BLUE, moved.capturedPlayerColor)
        assertEquals(7, moved.capturedTokenId)
        assertEquals(1, moved.capturedPlayerIndex)
        assertTrue(moved.grantedExtraTurn)

        // Blue token must be sent to Yard
        val victim = nextState.players[1].tokens.first { it.id == 7 }
        assertEquals(TokenState.YARD, victim.state)
        assertEquals(0, victim.stepsTraveled)
    }

    @Test
    fun testAllThemesHaveDistinctBoardStyles() {
        val themes = BoardThemeCatalog.allThemes
        assertEquals(6, themes.size)

        val styles = themes.map { it.boardStyle }.toSet()
        assertEquals("Each theme must map to a unique BoardStyle", 6, styles.size)
        assertTrue(styles.contains(BoardStyle.ROYAL_NAVY))
        assertTrue(styles.contains(BoardStyle.ROYAL_GOLD))
        assertTrue(styles.contains(BoardStyle.MIDNIGHT_OBSIDIAN))
        assertTrue(styles.contains(BoardStyle.CYBERPUNK_NEON))
        assertTrue(styles.contains(BoardStyle.CLASSIC_WOOD))
        assertTrue(styles.contains(BoardStyle.NEBULA_GALAXY))
    }
}
