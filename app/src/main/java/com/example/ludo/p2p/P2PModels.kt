package com.example.ludo.p2p

import com.example.ludo.model.GameState
import com.example.ludo.model.PlayerColor

enum class P2PTransportType(val displayName: String, val description: String) {
    WIFI_DIRECT_LAN(
        displayName = "Wi-Fi / Hotspot",
        description = "High speed, instant auto-discovery on local Wi-Fi or Personal Hotspot (2-4 devices)"
    ),
    BLUETOOTH(
        displayName = "Bluetooth P2P",
        description = "Direct peer-to-peer RFCOMM connection without needing any Wi-Fi router (2-4 devices)"
    )
}

enum class P2PRole {
    HOST,
    CLIENT
}

enum class P2PConnectionState {
    DISCONNECTED,
    ADVERTISING,
    DISCOVERING,
    CONNECTING,
    CONNECTED_LOBBY,
    IN_GAME,
    ERROR
}

data class DiscoveredRoom(
    val id: String,
    val name: String,
    val hostAddress: String,
    val port: Int,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val transport: P2PTransportType,
    val bluetoothDeviceAddress: String? = null
)

data class P2PPlayerSlot(
    val playerIndex: Int,
    val name: String,
    val color: PlayerColor,
    val isHost: Boolean = false,
    val isConnected: Boolean = true,
    val isReady: Boolean = false,
    val isBot: Boolean = false,
    val peerId: String? = null,
    val pingMs: Long = 0L
)

data class P2PLobbyState(
    val roomName: String,
    val roomCode: String,
    val hostName: String,
    val maxPlayers: Int,
    val slots: List<P2PPlayerSlot>,
    val isGameStarted: Boolean = false,
    val transport: P2PTransportType = P2PTransportType.WIFI_DIRECT_LAN
)

data class P2PEmoteMessage(
    val senderName: String,
    val senderColor: PlayerColor,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class P2PPacket {
    data class Hello(
        val peerId: String,
        val playerName: String,
        val preferredColor: PlayerColor
    ) : P2PPacket()

    data class LobbyUpdate(
        val lobby: P2PLobbyState
    ) : P2PPacket()

    data class SlotUpdate(
        val playerIndex: Int,
        val isReady: Boolean,
        val isBot: Boolean,
        val name: String? = null
    ) : P2PPacket()

    data class StartMatch(
        val initialState: GameState,
        val yourPlayerIndex: Int
    ) : P2PPacket()

    data class RollDiceRequest(
        val playerIndex: Int
    ) : P2PPacket()

    data class SelectPawnRequest(
        val playerIndex: Int,
        val tokenId: Int
    ) : P2PPacket()

    data class SyncGameState(
        val gameState: GameState
    ) : P2PPacket()

    data class QuickEmote(
        val senderName: String,
        val senderColor: PlayerColor,
        val message: String
    ) : P2PPacket()

    data class Ping(
        val timestamp: Long
    ) : P2PPacket()

    data class Pong(
        val originalTimestamp: Long
    ) : P2PPacket()

    data class LeaveGame(
        val reason: String
    ) : P2PPacket()
}
