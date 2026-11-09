package com.example

import com.example.ludo.ai.LudoBotAI
import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoBotAITest {

    @Test
    fun testSingleEligibleMoveReturnedImmediately() {
        val player = Player(
            id = 1,
            name = "Bot 1",
            color = PlayerColor.GREEN,
            isBot = true,
            botDifficulty = BotDifficulty.HARD
        )
        val state = GameState(players = listOf(player), diceValue = 6)
        val selected = LudoBotAI.chooseMove(player, state, setOf(2))
        assertEquals(2, selected)
    }

    @Test
    fun testHardBotPrioritizesCapturingOpponent() {
        // Green token 0 at step 10 -> track index (13 + 10) % 52 = 23
        // Green token 1 at step 4 -> track index (13 + 4) % 52 = 17.
        // Dice roll is 4.
        // Token 0 would move to step 14 -> track index (13 + 14) % 52 = 27.
        // Token 1 would move to step 8 -> track index (13 + 8) % 52 = 21.
        // Opponent (Red) token is at track index 21.
        // Moving token 1 captures Red! Hard bot should select token 1.
        val greenToken0 = Token(id = 0, color = PlayerColor.GREEN, state = TokenState.TRACK, stepsTraveled = 10)
        val greenToken1 = Token(id = 1, color = PlayerColor.GREEN, state = TokenState.TRACK, stepsTraveled = 4)

        val redToken = Token(id = 10, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 21) // Red track index 21

        val greenBot = Player(
            id = 0,
            name = "Green Bot",
            color = PlayerColor.GREEN,
            isBot = true,
            botDifficulty = BotDifficulty.HARD,
            tokens = listOf(greenToken0, greenToken1)
        )
        val redPlayer = Player(
            id = 1,
            name = "Human Red",
            color = PlayerColor.RED,
            tokens = listOf(redToken)
        )

        val state = GameState(
            players = listOf(greenBot, redPlayer),
            currentPlayerIndex = 0,
            diceValue = 4
        )

        val chosenId = LudoBotAI.chooseMove(greenBot, state, setOf(0, 1))
        assertEquals("Hard bot must choose token 1 to capture opponent", 1, chosenId)
    }

    @Test
    fun testHardBotPrioritizesFinishingPiece() {
        // Token 0 can reach finish (homeStep 5 + roll 1 = 6 FINISHED)
        // Token 1 is in yard (roll 1 cannot move)
        // Token 2 is on track step 15
        val finishingToken = Token(id = 0, color = PlayerColor.RED, state = TokenState.HOME_STRETCH, homeStep = 5)
        val trackToken = Token(id = 1, color = PlayerColor.RED, state = TokenState.TRACK, stepsTraveled = 15)

        val botPlayer = Player(
            id = 0,
            name = "Red Bot",
            color = PlayerColor.RED,
            isBot = true,
            botDifficulty = BotDifficulty.HARD,
            tokens = listOf(finishingToken, trackToken)
        )

        val state = GameState(
            players = listOf(botPlayer),
            diceValue = 1
        )

        val chosenId = LudoBotAI.chooseMove(botPlayer, state, setOf(0, 1))
        assertEquals("Hard bot must choose finishing token", 0, chosenId)
    }

    @Test
    fun testEasyBotAlwaysReturnsValidTokenId() {
        val tokens = listOf(
            Token(id = 0, color = PlayerColor.BLUE, state = TokenState.TRACK, stepsTraveled = 5),
            Token(id = 1, color = PlayerColor.BLUE, state = TokenState.TRACK, stepsTraveled = 15)
        )
        val easyBot = Player(
            id = 0,
            name = "Easy Bot",
            color = PlayerColor.BLUE,
            isBot = true,
            botDifficulty = BotDifficulty.EASY,
            tokens = tokens
        )
        val state = GameState(players = listOf(easyBot), diceValue = 3)

        for (i in 0..10) {
            val chosen = LudoBotAI.chooseMove(easyBot, state, setOf(0, 1))
            assertTrue(chosen == 0 || chosen == 1)
        }
    }
}
