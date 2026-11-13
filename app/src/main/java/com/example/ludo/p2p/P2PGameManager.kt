package com.example.ludo.p2p

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.example.ludo.ai.LudoBotAI
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Singleton coordinator for local peer-to-peer Wi-Fi and Bluetooth multiplayer.
 * Manages hosting, room discovery, lobby negotiation, synchronized turn engine, and quick chat.
 */
class P2PGameManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "P2PGameManager"

        @Volatile
        private var instance: P2PGameManager? = null

        fun getInstance(context: Context): P2PGameManager {
            return instance ?: synchronized(this) {
                instance ?: P2PGameManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _connectionState = MutableStateFlow(P2PConnectionState.DISCONNECTED)
    val connectionState: StateFlow<P2PConnectionState> = _connectionState.asStateFlow()

    private val _transportType = MutableStateFlow(P2PTransportType.WIFI_DIRECT_LAN)
    val transportType: StateFlow<P2PTransportType> = _transportType.asStateFlow()

    private val _role = MutableStateFlow(P2PRole.HOST)
    val role: StateFlow<P2PRole> = _role.asStateFlow()

    private val _lobbyState = MutableStateFlow<P2PLobbyState?>(null)
    val lobbyState: StateFlow<P2PLobbyState?> = _lobbyState.asStateFlow()

    private val _discoveredRooms = MutableStateFlow<List<DiscoveredRoom>>(emptyList())
    val discoveredRooms: StateFlow<List<DiscoveredRoom>> = _discoveredRooms.asStateFlow()

    private val _discoveredBtDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredBtDevices: StateFlow<List<BluetoothDevice>> = _discoveredBtDevices.asStateFlow()

    private val _localPlayerIndex = MutableStateFlow(0)
    val localPlayerIndex: StateFlow<Int> = _localPlayerIndex.asStateFlow()

    private val _syncedGameState = MutableStateFlow<GameState?>(null)
    val syncedGameState: StateFlow<GameState?> = _syncedGameState.asStateFlow()

    private val _emotesFlow = MutableSharedFlow<P2PEmoteMessage>(extraBufferCapacity = 16)
    val emotesFlow: SharedFlow<P2PEmoteMessage> = _emotesFlow.asSharedFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var myPlayerName: String = "Player"

    // Peer ID to slot index mapping on Host
    private val peerToSlotMap = mutableMapOf<String, Int>()
    private var botTurnJob: Job? = null

    // Transports
    val wifiTransport: WifiP2PTransport by lazy {
        WifiP2PTransport(
            context = context,
            coroutineScope = scope,
            onPacketReceived = { packet, peerId -> handlePacket(packet, peerId) },
            onPeerConnected = { peerId -> handlePeerConnected(peerId) },
            onPeerDisconnected = { peerId -> handlePeerDisconnected(peerId) },
            onRoomDiscovered = { room -> handleRoomDiscovered(room) },
            onRoomLost = { roomId -> handleRoomLost(roomId) },
            onError = { msg -> _errorMessage.value = msg }
        )
    }

    val bluetoothTransport: BluetoothP2PTransport by lazy {
        BluetoothP2PTransport(
            context = context,
            coroutineScope = scope,
            onPacketReceived = { packet, peerId -> handlePacket(packet, peerId) },
            onPeerConnected = { peerId -> handlePeerConnected(peerId) },
            onPeerDisconnected = { peerId -> handlePeerDisconnected(peerId) },
            onDeviceDiscovered = { dev -> handleBtDeviceDiscovered(dev) },
            onError = { msg -> _errorMessage.value = msg }
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ----------------------------------------------------
    // HOSTING
    // ----------------------------------------------------

    fun hostRoom(
        transport: P2PTransportType,
        roomName: String,
        maxPlayers: Int,
        hostName: String
    ): Boolean {
        leaveGame()
        _transportType.value = transport
        _role.value = P2PRole.HOST
        _localPlayerIndex.value = 0
        myPlayerName = hostName
        peerToSlotMap.clear()

        val initialColors = when (maxPlayers) {
            2 -> listOf(PlayerColor.RED, PlayerColor.YELLOW)
            3 -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW)
            else -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
        }

        val initialSlots = initialColors.mapIndexed { idx, color ->
            P2PPlayerSlot(
                playerIndex = idx,
                name = if (idx == 0) hostName else "Waiting for Player ${idx + 1}...",
                color = color,
                isHost = idx == 0,
                isConnected = idx == 0,
                isReady = idx == 0,
                isBot = false,
                peerId = if (idx == 0) "local_host" else null
            )
        }

        val roomCode = (1000..9999).random().toString()
        val lobby = P2PLobbyState(
            roomName = roomName,
            roomCode = roomCode,
            hostName = hostName,
            maxPlayers = maxPlayers,
            slots = initialSlots,
            isGameStarted = false,
            transport = transport
        )
        _lobbyState.value = lobby

        val success = if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
            wifiTransport.startHosting(roomName, maxPlayers, hostName)
        } else {
            bluetoothTransport.startHosting(roomName)
        }

        if (success) {
            _connectionState.value = P2PConnectionState.CONNECTED_LOBBY
            // Update room code from wifi transport if available
            if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
                _lobbyState.value = _lobbyState.value?.copy(roomCode = wifiTransport.currentRoomCode)
            }
        } else {
            _connectionState.value = P2PConnectionState.ERROR
        }
        return success
    }

    // Toggle bot in an unfilled slot on Host
    fun toggleSlotBot(slotIndex: Int) {
        if (_role.value != P2PRole.HOST) return
        val current = _lobbyState.value ?: return
        if (slotIndex <= 0 || slotIndex >= current.slots.size) return

        val slot = current.slots[slotIndex]
        if (slot.isConnected && !slot.isBot) return // human player already connected

        val updatedSlots = current.slots.toMutableList()
        val newIsBot = !slot.isBot
        updatedSlots[slotIndex] = slot.copy(
            isBot = newIsBot,
            isConnected = newIsBot,
            isReady = newIsBot,
            name = if (newIsBot) "Bot ${slot.color.displayName}" else "Waiting for Player ${slotIndex + 1}...",
            peerId = if (newIsBot) "bot_$slotIndex" else null
        )

        val updatedLobby = current.copy(slots = updatedSlots)
        _lobbyState.value = updatedLobby
        broadcastPacket(P2PPacket.LobbyUpdate(updatedLobby))
    }

    // ----------------------------------------------------
    // CLIENT DISCOVERY & JOINING
    // ----------------------------------------------------

    fun startDiscovery(transport: P2PTransportType) {
        _transportType.value = transport
        _discoveredRooms.value = emptyList()
        _discoveredBtDevices.value = emptyList()
        _connectionState.value = P2PConnectionState.DISCOVERING

        if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
            wifiTransport.startDiscovery()
        } else {
            val paired = bluetoothTransport.getPairedDevices()
            _discoveredBtDevices.value = paired
            bluetoothTransport.startScanning()
        }
    }

    fun stopDiscovery() {
        if (_transportType.value == P2PTransportType.WIFI_DIRECT_LAN) {
            wifiTransport.stopDiscovery()
        } else {
            bluetoothTransport.stopScanning()
        }
        if (_connectionState.value == P2PConnectionState.DISCOVERING) {
            _connectionState.value = P2PConnectionState.DISCONNECTED
        }
    }

    fun joinRoom(room: DiscoveredRoom, playerName: String) {
        stopDiscovery()
        _role.value = P2PRole.CLIENT
        _transportType.value = room.transport
        myPlayerName = playerName
        _connectionState.value = P2PConnectionState.CONNECTING

        if (room.transport == P2PTransportType.WIFI_DIRECT_LAN) {
            wifiTransport.connectToHost(room.hostAddress, room.port)
        } else {
            val dev = _discoveredBtDevices.value.firstOrNull { it.address == room.bluetoothDeviceAddress }
            if (dev != null) {
                bluetoothTransport.connectToDevice(dev)
            } else {
                _errorMessage.value = "Bluetooth device not found"
                _connectionState.value = P2PConnectionState.ERROR
            }
        }
    }

    fun joinByIp(hostAddress: String, port: Int = WifiP2PTransport.DEFAULT_PORT, playerName: String) {
        stopDiscovery()
        _role.value = P2PRole.CLIENT
        _transportType.value = P2PTransportType.WIFI_DIRECT_LAN
        myPlayerName = playerName
        _connectionState.value = P2PConnectionState.CONNECTING
        wifiTransport.connectToHost(hostAddress, port)
    }

    fun joinBluetoothDevice(device: BluetoothDevice, playerName: String) {
        stopDiscovery()
        _role.value = P2PRole.CLIENT
        _transportType.value = P2PTransportType.BLUETOOTH
        myPlayerName = playerName
        _connectionState.value = P2PConnectionState.CONNECTING
        bluetoothTransport.connectToDevice(device)
    }

    fun toggleClientReady() {
        val lobby = _lobbyState.value ?: return
        val mySlotIdx = _localPlayerIndex.value
        val mySlot = lobby.slots.getOrNull(mySlotIdx) ?: return
        val newReady = !mySlot.isReady

        sendPacket(P2PPacket.SlotUpdate(
            playerIndex = mySlotIdx,
            isReady = newReady,
            isBot = false,
            name = myPlayerName
        ))
    }

    // ----------------------------------------------------
    // STARTING THE MATCH
    // ----------------------------------------------------

    fun canStartMatch(): Boolean {
        if (_role.value != P2PRole.HOST) return false
        val lobby = _lobbyState.value ?: return false
        val filledSlots = lobby.slots.filter { it.isConnected }
        // Must have at least 2 connected players/bots, and all must be ready
        return filledSlots.size >= 2 && filledSlots.all { it.isReady }
    }

    fun startMatch() {
        if (!canStartMatch()) return
        val lobby = _lobbyState.value ?: return

        // Build Players for GameState ordered clockwise
        val players = lobby.slots.filter { it.isConnected }.mapIndexed { idx, slot ->
            Player(
                id = idx,
                name = slot.name,
                color = slot.color,
                isBot = slot.isBot,
                botDifficulty = BotDifficulty.MEDIUM
            )
        }.sortedBy { it.color.startIndex }

        val initialState = GameState(
            players = players,
            currentPlayerIndex = 0,
            diceValue = 1,
            isWaitingForRoll = true,
            statusMessage = "${players[0].name}'s turn to roll"
        )

        _syncedGameState.value = initialState
        _connectionState.value = P2PConnectionState.IN_GAME

        val updatedLobby = lobby.copy(isGameStarted = true)
        _lobbyState.value = updatedLobby

        // Send StartMatch packet to each peer with their assigned player index in ordered players list
        if (_transportType.value == P2PTransportType.WIFI_DIRECT_LAN) {
            for ((peerId, slotIdx) in peerToSlotMap) {
                val assignedColor = lobby.slots.getOrNull(slotIdx)?.color
                val gamePlayerIdx = players.indexOfFirst { it.color == assignedColor }.coerceAtLeast(0)
                wifiTransport.sendPacketToPeer(peerId, P2PPacket.StartMatch(initialState, gamePlayerIdx))
            }
        } else {
            for ((peerId, slotIdx) in peerToSlotMap) {
                val assignedColor = lobby.slots.getOrNull(slotIdx)?.color
                val gamePlayerIdx = players.indexOfFirst { it.color == assignedColor }.coerceAtLeast(0)
                bluetoothTransport.sendPacketToPeer(peerId, P2PPacket.StartMatch(initialState, gamePlayerIdx))
            }
        }

        // Check if first player is bot
        checkAndTriggerBotTurn(initialState)
    }

    // ----------------------------------------------------
    // GAMEPLAY DISPATCH (TURNS, MOVES, ROLLS)
    // ----------------------------------------------------

    fun rollDice() {
        val state = _syncedGameState.value ?: return
        if (!state.isWaitingForRoll) return
        val myIdx = _localPlayerIndex.value
        if (state.currentPlayerIndex != myIdx) return // Not your turn

        if (_role.value == P2PRole.HOST) {
            executeHostDiceRoll(myIdx)
        } else {
            sendPacket(P2PPacket.RollDiceRequest(myIdx))
        }
    }

    fun selectPawn(tokenId: Int) {
        val state = _syncedGameState.value ?: return
        val myIdx = _localPlayerIndex.value
        if (state.currentPlayerIndex != myIdx) return
        if (state.isWaitingForRoll) return
        if (!state.movableTokenIds.contains(tokenId)) return

        if (_role.value == P2PRole.HOST) {
            executeHostPawnMove(myIdx, tokenId)
        } else {
            sendPacket(P2PPacket.SelectPawnRequest(myIdx, tokenId))
        }
    }

    private fun executeHostDiceRoll(playerIndex: Int) {
        val state = _syncedGameState.value ?: return
        if (state.currentPlayerIndex != playerIndex) return

        val roll = Random.nextInt(1, 7)
        val currentPlayer = state.currentPlayer
        val movableIds = LudoGameEngine.getMovableTokenIds(currentPlayer, roll)
        val newConsecutiveSixes = if (roll == 6) state.consecutiveSixes + 1 else 0

        val (canMove, statusMsg) = when {
            newConsecutiveSixes >= 3 -> {
                false to "${currentPlayer.name} rolled three 6s in a row! Turn forfeited."
            }
            movableIds.isEmpty() -> {
                false to "${currentPlayer.name} rolled a $roll. No legal moves."
            }
            movableIds.size == 1 -> {
                true to "${currentPlayer.name} rolled a $roll! Auto-moving pawn..."
            }
            else -> {
                true to "${currentPlayer.name} rolled a $roll! Select a pawn to move."
            }
        }

        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == playerIndex && roll == 6) p.copy(sixesCount = p.sixesCount + 1) else p
        }

        if (!canMove) {
            // Advance turn immediately
            val nextIdx = (state.currentPlayerIndex + 1) % state.players.size
            val nextPlayer = updatedPlayers[nextIdx]
            val nextState = state.copy(
                players = updatedPlayers,
                currentPlayerIndex = nextIdx,
                diceValue = roll,
                isWaitingForRoll = true,
                movableTokenIds = emptySet(),
                consecutiveSixes = 0,
                canRollAgain = false,
                statusMessage = "$statusMsg Now ${nextPlayer.name}'s turn.",
                turnCount = state.turnCount + 1
            )
            updateAndBroadcastGameState(nextState)
            checkAndTriggerBotTurn(nextState)
        } else {
            val intermediateState = state.copy(
                players = updatedPlayers,
                diceValue = roll,
                isWaitingForRoll = false,
                movableTokenIds = movableIds,
                consecutiveSixes = newConsecutiveSixes,
                statusMessage = statusMsg
            )

            // Auto-move single pawn if only one option exists
            if (movableIds.size == 1) {
                updateAndBroadcastGameState(intermediateState)
                scope.launch {
                    delay(400)
                    executeHostPawnMove(playerIndex, movableIds.first())
                }
            } else {
                updateAndBroadcastGameState(intermediateState)
            }
        }
    }

    private fun executeHostPawnMove(playerIndex: Int, tokenId: Int) {
        val state = _syncedGameState.value ?: return
        if (state.currentPlayerIndex != playerIndex) return

        val (newState, _) = LudoGameEngine.executeMove(state, tokenId)
        updateAndBroadcastGameState(newState)

        if (newState.winner == null) {
            checkAndTriggerBotTurn(newState)
        }
    }

    private fun checkAndTriggerBotTurn(state: GameState) {
        botTurnJob?.cancel()
        val currentPlayer = state.currentPlayer
        if (!currentPlayer.isBot || state.isGameOver) return

        botTurnJob = scope.launch {
            delay(1000)
            if (_syncedGameState.value?.currentPlayerIndex == currentPlayer.id && _syncedGameState.value?.isWaitingForRoll == true) {
                executeHostDiceRoll(currentPlayer.id)
                delay(600)
                val curr = _syncedGameState.value ?: return@launch
                if (!curr.isWaitingForRoll && curr.movableTokenIds.isNotEmpty()) {
                    val chosenTokenId = LudoBotAI.chooseMove(
                        player = curr.currentPlayer,
                        gameState = curr,
                        movableTokenIds = curr.movableTokenIds
                    )
                    executeHostPawnMove(currentPlayer.id, chosenTokenId)
                }
            }
        }
    }

    private fun updateAndBroadcastGameState(newState: GameState) {
        _syncedGameState.value = newState
        broadcastPacket(P2PPacket.SyncGameState(newState))
    }

    // ----------------------------------------------------
    // QUICK CHAT EMOTES
    // ----------------------------------------------------

    fun sendQuickEmote(message: String) {
        val lobby = _lobbyState.value
        val mySlot = lobby?.slots?.getOrNull(_localPlayerIndex.value)
        val color = mySlot?.color ?: PlayerColor.RED
        val packet = P2PPacket.QuickEmote(
            senderName = myPlayerName,
            senderColor = color,
            message = message
        )
        broadcastPacket(packet)
        scope.launch {
            _emotesFlow.emit(P2PEmoteMessage(myPlayerName, color, message))
        }
    }

    // ----------------------------------------------------
    // PACKET & CONNECTION ROUTING
    // ----------------------------------------------------

    private fun sendPacket(packet: P2PPacket) {
        if (_transportType.value == P2PTransportType.WIFI_DIRECT_LAN) {
            wifiTransport.sendPacketToAll(packet)
        } else {
            bluetoothTransport.sendPacketToAll(packet)
        }
    }

    private fun broadcastPacket(packet: P2PPacket) {
        sendPacket(packet)
    }

    private fun handlePacket(packet: P2PPacket, senderPeerId: String) {
        when (packet) {
            is P2PPacket.Hello -> {
                if (_role.value == P2PRole.HOST) {
                    val lobby = _lobbyState.value ?: return
                    // Find first available slot (excluding host slot 0 and connected humans)
                    val availableSlot = lobby.slots.firstOrNull { it.playerIndex > 0 && (!it.isConnected || it.isBot) }
                    if (availableSlot != null) {
                        peerToSlotMap[senderPeerId] = availableSlot.playerIndex
                        val updatedSlots = lobby.slots.toMutableList()
                        updatedSlots[availableSlot.playerIndex] = availableSlot.copy(
                            name = packet.playerName,
                            isConnected = true,
                            isReady = false,
                            isBot = false,
                            peerId = senderPeerId
                        )
                        val updatedLobby = lobby.copy(slots = updatedSlots)
                        _lobbyState.value = updatedLobby
                        broadcastPacket(P2PPacket.LobbyUpdate(updatedLobby))
                    }
                }
            }
            is P2PPacket.LobbyUpdate -> {
                if (_role.value == P2PRole.CLIENT) {
                    _lobbyState.value = packet.lobby
                    if (_connectionState.value == P2PConnectionState.CONNECTING) {
                        _connectionState.value = P2PConnectionState.CONNECTED_LOBBY
                        // Identify my player index from the slots
                        val mySlot = packet.lobby.slots.firstOrNull { it.name == myPlayerName }
                        if (mySlot != null) {
                            _localPlayerIndex.value = mySlot.playerIndex
                        }
                    }
                }
            }
            is P2PPacket.SlotUpdate -> {
                if (_role.value == P2PRole.HOST) {
                    val lobby = _lobbyState.value ?: return
                    if (packet.playerIndex in lobby.slots.indices) {
                        val updatedSlots = lobby.slots.toMutableList()
                        val current = updatedSlots[packet.playerIndex]
                        updatedSlots[packet.playerIndex] = current.copy(
                            isReady = packet.isReady,
                            name = packet.name ?: current.name
                        )
                        val updatedLobby = lobby.copy(slots = updatedSlots)
                        _lobbyState.value = updatedLobby
                        broadcastPacket(P2PPacket.LobbyUpdate(updatedLobby))
                    }
                }
            }
            is P2PPacket.StartMatch -> {
                if (_role.value == P2PRole.CLIENT) {
                    _localPlayerIndex.value = packet.yourPlayerIndex
                    _syncedGameState.value = packet.initialState
                    _connectionState.value = P2PConnectionState.IN_GAME
                }
            }
            is P2PPacket.RollDiceRequest -> {
                if (_role.value == P2PRole.HOST) {
                    executeHostDiceRoll(packet.playerIndex)
                }
            }
            is P2PPacket.SelectPawnRequest -> {
                if (_role.value == P2PRole.HOST) {
                    executeHostPawnMove(packet.playerIndex, packet.tokenId)
                }
            }
            is P2PPacket.SyncGameState -> {
                _syncedGameState.value = packet.gameState
            }
            is P2PPacket.QuickEmote -> {
                scope.launch {
                    _emotesFlow.emit(P2PEmoteMessage(packet.senderName, packet.senderColor, packet.message))
                }
            }
            is P2PPacket.LeaveGame -> {
                handlePeerDisconnected(senderPeerId)
            }
            else -> {}
        }
    }

    private fun handlePeerConnected(peerId: String) {
        if (_role.value == P2PRole.CLIENT) {
            // Send Hello packet to host
            val myPreferredColor = PlayerColor.GREEN
            sendPacket(P2PPacket.Hello(
                peerId = "client_${System.currentTimeMillis()}",
                playerName = myPlayerName,
                preferredColor = myPreferredColor
            ))
        }
    }

    private fun handlePeerDisconnected(peerId: String) {
        if (_role.value == P2PRole.HOST) {
            val slotIdx = peerToSlotMap.remove(peerId) ?: return
            val lobby = _lobbyState.value ?: return
            if (slotIdx in lobby.slots.indices) {
                val updatedSlots = lobby.slots.toMutableList()
                val current = updatedSlots[slotIdx]
                updatedSlots[slotIdx] = current.copy(
                    isConnected = false,
                    isReady = false,
                    name = "Waiting for Player ${slotIdx + 1}...",
                    peerId = null
                )
                val updatedLobby = lobby.copy(slots = updatedSlots)
                _lobbyState.value = updatedLobby
                broadcastPacket(P2PPacket.LobbyUpdate(updatedLobby))
            }
        } else if (peerId == "host") {
            _errorMessage.value = "Host disconnected from game"
            leaveGame()
        }
    }

    private fun handleRoomDiscovered(room: DiscoveredRoom) {
        val current = _discoveredRooms.value.toMutableList()
        current.removeAll { it.id == room.id }
        current.add(room)
        _discoveredRooms.value = current
    }

    private fun handleRoomLost(roomId: String) {
        _discoveredRooms.value = _discoveredRooms.value.filterNot { it.id == roomId }
    }

    private fun handleBtDeviceDiscovered(device: BluetoothDevice) {
        val current = _discoveredBtDevices.value.toMutableList()
        if (current.none { it.address == device.address }) {
            current.add(device)
            _discoveredBtDevices.value = current
        }
    }

    fun leaveGame() {
        botTurnJob?.cancel()
        wifiTransport.stopAll()
        bluetoothTransport.stopAll()
        _lobbyState.value = null
        _syncedGameState.value = null
        _connectionState.value = P2PConnectionState.DISCONNECTED
        peerToSlotMap.clear()
    }
}
