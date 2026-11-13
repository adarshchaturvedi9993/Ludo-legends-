package com.example.ludo.p2p

import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles encoding and decoding of P2P network packets and full GameState synchronization.
 */
object P2PProtocol {

    fun encodePacket(packet: P2PPacket): String {
        val json = JSONObject()
        when (packet) {
            is P2PPacket.Hello -> {
                json.put("type", "HELLO")
                json.put("peerId", packet.peerId)
                json.put("playerName", packet.playerName)
                json.put("preferredColor", packet.preferredColor.name)
            }
            is P2PPacket.LobbyUpdate -> {
                json.put("type", "LOBBY_UPDATE")
                json.put("lobby", encodeLobby(packet.lobby))
            }
            is P2PPacket.SlotUpdate -> {
                json.put("type", "SLOT_UPDATE")
                json.put("playerIndex", packet.playerIndex)
                json.put("isReady", packet.isReady)
                json.put("isBot", packet.isBot)
                packet.name?.let { json.put("name", it) }
            }
            is P2PPacket.StartMatch -> {
                json.put("type", "START_MATCH")
                json.put("initialState", encodeGameState(packet.initialState))
                json.put("yourPlayerIndex", packet.yourPlayerIndex)
            }
            is P2PPacket.RollDiceRequest -> {
                json.put("type", "ROLL_DICE_REQ")
                json.put("playerIndex", packet.playerIndex)
            }
            is P2PPacket.SelectPawnRequest -> {
                json.put("type", "SELECT_PAWN_REQ")
                json.put("playerIndex", packet.playerIndex)
                json.put("tokenId", packet.tokenId)
            }
            is P2PPacket.SyncGameState -> {
                json.put("type", "SYNC_GAME_STATE")
                json.put("gameState", encodeGameState(packet.gameState))
            }
            is P2PPacket.QuickEmote -> {
                json.put("type", "QUICK_EMOTE")
                json.put("senderName", packet.senderName)
                json.put("senderColor", packet.senderColor.name)
                json.put("message", packet.message)
            }
            is P2PPacket.Ping -> {
                json.put("type", "PING")
                json.put("timestamp", packet.timestamp)
            }
            is P2PPacket.Pong -> {
                json.put("type", "PONG")
                json.put("originalTimestamp", packet.originalTimestamp)
            }
            is P2PPacket.LeaveGame -> {
                json.put("type", "LEAVE_GAME")
                json.put("reason", packet.reason)
            }
        }
        return json.toString()
    }

    fun decodePacket(raw: String): P2PPacket? {
        return try {
            val json = JSONObject(raw)
            when (json.optString("type")) {
                "HELLO" -> P2PPacket.Hello(
                    peerId = json.getString("peerId"),
                    playerName = json.getString("playerName"),
                    preferredColor = PlayerColor.valueOf(json.optString("preferredColor", "RED"))
                )
                "LOBBY_UPDATE" -> P2PPacket.LobbyUpdate(
                    lobby = decodeLobby(json.getJSONObject("lobby"))
                )
                "SLOT_UPDATE" -> P2PPacket.SlotUpdate(
                    playerIndex = json.getInt("playerIndex"),
                    isReady = json.getBoolean("isReady"),
                    isBot = json.getBoolean("isBot"),
                    name = if (json.has("name")) json.getString("name") else null
                )
                "START_MATCH" -> P2PPacket.StartMatch(
                    initialState = decodeGameState(json.getJSONObject("initialState")),
                    yourPlayerIndex = json.getInt("yourPlayerIndex")
                )
                "ROLL_DICE_REQ" -> P2PPacket.RollDiceRequest(
                    playerIndex = json.getInt("playerIndex")
                )
                "SELECT_PAWN_REQ" -> P2PPacket.SelectPawnRequest(
                    playerIndex = json.getInt("playerIndex"),
                    tokenId = json.getInt("tokenId")
                )
                "SYNC_GAME_STATE" -> P2PPacket.SyncGameState(
                    gameState = decodeGameState(json.getJSONObject("gameState"))
                )
                "QUICK_EMOTE" -> P2PPacket.QuickEmote(
                    senderName = json.getString("senderName"),
                    senderColor = PlayerColor.valueOf(json.optString("senderColor", "RED")),
                    message = json.getString("message")
                )
                "PING" -> P2PPacket.Ping(
                    timestamp = json.getLong("timestamp")
                )
                "PONG" -> P2PPacket.Pong(
                    originalTimestamp = json.getLong("originalTimestamp")
                )
                "LEAVE_GAME" -> P2PPacket.LeaveGame(
                    reason = json.optString("reason", "Left")
                )
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodeLobby(lobby: P2PLobbyState): JSONObject {
        val json = JSONObject()
        json.put("roomName", lobby.roomName)
        json.put("roomCode", lobby.roomCode)
        json.put("hostName", lobby.hostName)
        json.put("maxPlayers", lobby.maxPlayers)
        json.put("isGameStarted", lobby.isGameStarted)
        json.put("transport", lobby.transport.name)

        val slotsArray = JSONArray()
        for (slot in lobby.slots) {
            val sObj = JSONObject()
            sObj.put("playerIndex", slot.playerIndex)
            sObj.put("name", slot.name)
            sObj.put("color", slot.color.name)
            sObj.put("isHost", slot.isHost)
            sObj.put("isConnected", slot.isConnected)
            sObj.put("isReady", slot.isReady)
            sObj.put("isBot", slot.isBot)
            sObj.put("pingMs", slot.pingMs)
            slot.peerId?.let { sObj.put("peerId", it) }
            slotsArray.put(sObj)
        }
        json.put("slots", slotsArray)
        return json
    }

    fun decodeLobby(json: JSONObject): P2PLobbyState {
        val roomName = json.getString("roomName")
        val roomCode = json.optString("roomCode", "1234")
        val hostName = json.getString("hostName")
        val maxPlayers = json.getInt("maxPlayers")
        val isGameStarted = json.optBoolean("isGameStarted", false)
        val transport = try {
            P2PTransportType.valueOf(json.optString("transport", "WIFI_DIRECT_LAN"))
        } catch (_: Exception) {
            P2PTransportType.WIFI_DIRECT_LAN
        }

        val slotsArray = json.getJSONArray("slots")
        val slots = mutableListOf<P2PPlayerSlot>()
        for (i in 0 until slotsArray.length()) {
            val sObj = slotsArray.getJSONObject(i)
            slots.add(
                P2PPlayerSlot(
                    playerIndex = sObj.getInt("playerIndex"),
                    name = sObj.getString("name"),
                    color = PlayerColor.valueOf(sObj.getString("color")),
                    isHost = sObj.optBoolean("isHost", false),
                    isConnected = sObj.optBoolean("isConnected", true),
                    isReady = sObj.optBoolean("isReady", false),
                    isBot = sObj.optBoolean("isBot", false),
                    peerId = if (sObj.has("peerId")) sObj.getString("peerId") else null,
                    pingMs = sObj.optLong("pingMs", 0L)
                )
            )
        }
        return P2PLobbyState(
            roomName = roomName,
            roomCode = roomCode,
            hostName = hostName,
            maxPlayers = maxPlayers,
            slots = slots,
            isGameStarted = isGameStarted,
            transport = transport
        )
    }

    fun encodeGameState(state: GameState): JSONObject {
        val json = JSONObject()
        json.put("currentPlayerIndex", state.currentPlayerIndex)
        json.put("diceValue", state.diceValue)
        json.put("isDiceRolling", state.isDiceRolling)
        json.put("isWaitingForRoll", state.isWaitingForRoll)
        json.put("consecutiveSixes", state.consecutiveSixes)
        json.put("canRollAgain", state.canRollAgain)
        json.put("statusMessage", state.statusMessage)
        json.put("turnCount", state.turnCount)
        json.put("isPaused", state.isPaused)
        state.lastCapturedPlayerColor?.let { json.put("lastCapturedPlayerColor", it.name) }

        val movableTokensArr = JSONArray()
        state.movableTokenIds.forEach { movableTokensArr.put(it) }
        json.put("movableTokenIds", movableTokensArr)

        val playersArr = JSONArray()
        for (p in state.players) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("color", p.color.name)
            pObj.put("sixesCount", p.sixesCount)
            pObj.put("capturesCount", p.capturesCount)
            pObj.put("isBot", p.isBot)
            pObj.put("botDifficulty", p.botDifficulty.name)
            p.customSkinId?.let { pObj.put("customSkinId", it) }
            p.rank?.let { pObj.put("rank", it) }

            val tokensArr = JSONArray()
            for (t in p.tokens) {
                val tObj = JSONObject()
                tObj.put("id", t.id)
                tObj.put("state", t.state.name)
                tObj.put("stepsTraveled", t.stepsTraveled)
                tObj.put("homeStep", t.homeStep)
                tObj.put("finishOrder", t.finishOrder)
                tokensArr.put(tObj)
            }
            pObj.put("tokens", tokensArr)
            playersArr.put(pObj)
        }
        json.put("players", playersArr)
        return json
    }

    fun decodeGameState(json: JSONObject): GameState {
        val currentPlayerIndex = json.getInt("currentPlayerIndex")
        val diceValue = json.optInt("diceValue", 1)
        val isDiceRolling = json.optBoolean("isDiceRolling", false)
        val isWaitingForRoll = json.optBoolean("isWaitingForRoll", true)
        val consecutiveSixes = json.optInt("consecutiveSixes", 0)
        val canRollAgain = json.optBoolean("canRollAgain", false)
        val statusMessage = json.optString("statusMessage", "")
        val turnCount = json.optInt("turnCount", 1)
        val isPaused = json.optBoolean("isPaused", false)
        val lastCapturedPlayerColor = if (json.has("lastCapturedPlayerColor")) {
            try {
                PlayerColor.valueOf(json.getString("lastCapturedPlayerColor"))
            } catch (_: Exception) {
                null
            }
        } else null

        val movableTokenIds = mutableSetOf<Int>()
        if (json.has("movableTokenIds")) {
            val arr = json.getJSONArray("movableTokenIds")
            for (i in 0 until arr.length()) {
                movableTokenIds.add(arr.getInt(i))
            }
        }

        val playersArr = json.getJSONArray("players")
        val players = mutableListOf<Player>()
        for (i in 0 until playersArr.length()) {
            val pObj = playersArr.getJSONObject(i)
            val id = pObj.getInt("id")
            val name = pObj.getString("name")
            val color = PlayerColor.valueOf(pObj.getString("color"))
            val sixesCount = pObj.optInt("sixesCount", 0)
            val capturesCount = pObj.optInt("capturesCount", 0)
            val isBot = pObj.optBoolean("isBot", false)
            val customSkinId = if (pObj.has("customSkinId")) pObj.getString("customSkinId") else null
            val rank = if (pObj.has("rank")) pObj.getInt("rank") else null
            val botDifficulty = try {
                BotDifficulty.valueOf(pObj.optString("botDifficulty", "MEDIUM"))
            } catch (_: Exception) {
                BotDifficulty.MEDIUM
            }

            val tokensArr = pObj.getJSONArray("tokens")
            val tokens = mutableListOf<Token>()
            for (j in 0 until tokensArr.length()) {
                val tObj = tokensArr.getJSONObject(j)
                tokens.add(
                    Token(
                        id = tObj.getInt("id"),
                        color = color,
                        state = TokenState.valueOf(tObj.getString("state")),
                        stepsTraveled = tObj.getInt("stepsTraveled"),
                        homeStep = tObj.getInt("homeStep"),
                        finishOrder = tObj.optInt("finishOrder", 0)
                    )
                )
            }

            players.add(
                Player(
                    id = id,
                    name = name,
                    color = color,
                    tokens = tokens,
                    rank = rank,
                    sixesCount = sixesCount,
                    capturesCount = capturesCount,
                    isBot = isBot,
                    botDifficulty = botDifficulty,
                    customSkinId = customSkinId
                )
            )
        }

        val winner = players.firstOrNull { it.rank == 1 }
        val rankings = players.filter { it.rank != null }.sortedBy { it.rank ?: 99 }

        return GameState(
            players = players,
            currentPlayerIndex = currentPlayerIndex,
            diceValue = diceValue,
            isDiceRolling = isDiceRolling,
            isWaitingForRoll = isWaitingForRoll,
            movableTokenIds = movableTokenIds,
            consecutiveSixes = consecutiveSixes,
            canRollAgain = canRollAgain,
            winner = winner,
            rankings = rankings,
            statusMessage = statusMessage,
            turnCount = turnCount,
            isPaused = isPaused,
            lastCapturedPlayerColor = lastCapturedPlayerColor
        )
    }
}
