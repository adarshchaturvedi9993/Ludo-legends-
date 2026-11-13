package com.example

import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import com.example.ludo.p2p.P2PLobbyState
import com.example.ludo.p2p.P2PPacket
import com.example.ludo.p2p.P2PPlayerSlot
import com.example.ludo.p2p.P2PProtocol
import com.example.ludo.p2p.P2PTransportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LudoP2PMultiplayerTest {

    @Test
    fun testHelloPacketRoundtrip() {
        val original = P2PPacket.Hello(
            peerId = "peer_test_123",
            playerName = "Alex",
            preferredColor = PlayerColor.GREEN
        )
        val encoded = P2PProtocol.encodePacket(original)
        val decoded = P2PProtocol.decodePacket(encoded)

        assertTrue(decoded is P2PPacket.Hello)
        val hello = decoded as P2PPacket.Hello
        assertEquals("peer_test_123", hello.peerId)
        assertEquals("Alex", hello.playerName)
        assertEquals(PlayerColor.GREEN, hello.preferredColor)
    }

    @Test
    fun testLobbyUpdatePacketRoundtrip() {
        val slots = listOf(
            P2PPlayerSlot(0, "HostPlayer", PlayerColor.RED, isHost = true, isConnected = true, isReady = true),
            P2PPlayerSlot(1, "GuestPlayer", PlayerColor.GREEN, isHost = false, isConnected = true, isReady = true),
            P2PPlayerSlot(2, "Bot Yellow", PlayerColor.YELLOW, isHost = false, isConnected = true, isReady = true, isBot = true),
            P2PPlayerSlot(3, "Waiting...", PlayerColor.BLUE, isHost = false, isConnected = false, isReady = false)
        )
        val lobby = P2PLobbyState(
            roomName = "Alex's Arena",
            roomCode = "8492",
            hostName = "HostPlayer",
            maxPlayers = 4,
            slots = slots,
            isGameStarted = false,
            transport = P2PTransportType.WIFI_DIRECT_LAN
        )

        val packet = P2PPacket.LobbyUpdate(lobby)
        val encoded = P2PProtocol.encodePacket(packet)
        val decoded = P2PProtocol.decodePacket(encoded) as? P2PPacket.LobbyUpdate

        assertNotNull(decoded)
        assertEquals("Alex's Arena", decoded!!.lobby.roomName)
        assertEquals("8492", decoded.lobby.roomCode)
        assertEquals(4, decoded.lobby.slots.size)
        assertEquals(true, decoded.lobby.slots[0].isHost)
        assertEquals(true, decoded.lobby.slots[2].isBot)
        assertEquals(false, decoded.lobby.slots[3].isConnected)
    }

    @Test
    fun testGameStatePacketRoundtrip() {
        val playerRed = Player(
            id = 0,
            name = "Red Player",
            color = PlayerColor.RED,
            tokens = listOf(
                Token(0, PlayerColor.RED, TokenState.TRACK, stepsTraveled = 14),
                Token(1, PlayerColor.RED, TokenState.YARD),
                Token(2, PlayerColor.RED, TokenState.HOME_STRETCH, homeStep = 3),
                Token(3, PlayerColor.RED, TokenState.FINISHED, finishOrder = 1)
            ),
            sixesCount = 4,
            capturesCount = 2
        )
        val playerGreen = Player(
            id = 1,
            name = "Green Player",
            color = PlayerColor.GREEN,
            isBot = true,
            botDifficulty = BotDifficulty.HARD
        )

        val state = GameState(
            players = listOf(playerRed, playerGreen),
            currentPlayerIndex = 0,
            diceValue = 6,
            isWaitingForRoll = false,
            movableTokenIds = setOf(0, 1),
            consecutiveSixes = 1,
            canRollAgain = true,
            statusMessage = "Red rolled a 6!",
            turnCount = 12
        )

        val packet = P2PPacket.SyncGameState(state)
        val encoded = P2PProtocol.encodePacket(packet)
        val decoded = P2PProtocol.decodePacket(encoded) as? P2PPacket.SyncGameState

        assertNotNull(decoded)
        val restored = decoded!!.gameState
        assertEquals(2, restored.players.size)
        assertEquals(0, restored.currentPlayerIndex)
        assertEquals(6, restored.diceValue)
        assertEquals(false, restored.isWaitingForRoll)
        assertEquals(setOf(0, 1), restored.movableTokenIds)
        assertEquals(1, restored.consecutiveSixes)
        assertEquals(true, restored.canRollAgain)
        assertEquals(TokenState.TRACK, restored.players[0].tokens[0].state)
        assertEquals(14, restored.players[0].tokens[0].stepsTraveled)
        assertEquals(TokenState.FINISHED, restored.players[0].tokens[3].state)
        assertEquals(true, restored.players[1].isBot)
    }

    @Test
    fun testQuickEmotePacketRoundtrip() {
        val emote = P2PPacket.QuickEmote(
            senderName = "Player 1",
            senderColor = PlayerColor.BLUE,
            message = "🔥 Great roll!"
        )
        val encoded = P2PProtocol.encodePacket(emote)
        val decoded = P2PProtocol.decodePacket(encoded) as? P2PPacket.QuickEmote

        assertNotNull(decoded)
        assertEquals("Player 1", decoded!!.senderName)
        assertEquals(PlayerColor.BLUE, decoded.senderColor)
        assertEquals("🔥 Great roll!", decoded.message)
    }

    @Test
    fun testActionRequestsRoundtrip() {
        val rollReq = P2PPacket.RollDiceRequest(playerIndex = 2)
        val encodedRoll = P2PProtocol.encodePacket(rollReq)
        val decodedRoll = P2PProtocol.decodePacket(encodedRoll) as? P2PPacket.RollDiceRequest
        assertNotNull(decodedRoll)
        assertEquals(2, decodedRoll!!.playerIndex)

        val selectReq = P2PPacket.SelectPawnRequest(playerIndex = 1, tokenId = 3)
        val encodedSelect = P2PProtocol.encodePacket(selectReq)
        val decodedSelect = P2PProtocol.decodePacket(encodedSelect) as? P2PPacket.SelectPawnRequest
        assertNotNull(decodedSelect)
        assertEquals(1, decodedSelect!!.playerIndex)
        assertEquals(3, decodedSelect.tokenId)
    }
}
