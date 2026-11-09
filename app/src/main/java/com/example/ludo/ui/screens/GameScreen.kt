package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.AdConstants
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ai.LudoBotAI
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GameState
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameDialog
import com.example.ludo.ui.components.LudoBoardView
import com.example.ludo.ui.components.PlayerDicePanel
import com.example.ludo.ui.components.Realistic3DDiceView
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldRimGradientColors
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Production Ludo Game Screen:
 * - 100% Direct Board Pawn Tapping (No bottom sheet popup).
 * - Movable pawns display glow ring, scale pulse, bouncing arrow, and destination ghost preview.
 * - Single pawn auto-move (400ms delay).
 * - Top header with pause, crown turn pill, sound toggle.
 * - 4 individual corner player dice panels with 60% dimming for inactive players.
 * - Turn timer with countdown ring and auto-play on timeout.
 * - Dominant bottom action bar.
 */
@Composable
fun GameScreen(
    gameState: GameState,
    soundManager: SoundManager,
    gamePreferences: GamePreferences? = null,
    onStateUpdated: (GameState) -> Unit,
    onGameFinished: (GameState) -> Unit,
    onExitToMenu: () -> Unit,
    onRestartGame: () -> Unit
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    var isSoundMuted by remember { mutableStateOf(!soundManager.isSoundEnabled) }
    var isMusicOn by remember { mutableStateOf(soundManager.isMusicEnabled) }
    val coroutineScope = rememberCoroutineScope()
    val currentPlayer = gameState.currentPlayer

    val isAutoMoveEnabled = gamePreferences?.isAutoMoveSinglePawnEnabled ?: true
    val isTurnTimerEnabled = gamePreferences?.isTurnTimerEnabled ?: true
    val turnTimerLimitSecs = gamePreferences?.turnTimerSeconds ?: 15
    val isColorBlindEnabled = gamePreferences?.isColorBlindModeEnabled ?: false

    // Player last rolled dice values
    val playerLastDiceValue = remember {
        mutableStateMapOf<Int, Int>().apply {
            gameState.players.forEach { put(it.id, 1) }
        }
    }

    LaunchedEffect(gameState.diceValue, currentPlayer.id) {
        if (gameState.diceValue in 1..6) {
            playerLastDiceValue[currentPlayer.id] = gameState.diceValue
        }
    }

    // Partition player panels: Top (Red/Green) and Bottom (Blue/Yellow)
    val (topPlayers, bottomPlayers) = remember(gameState.players) {
        when (gameState.players.size) {
            2 -> Pair(listOf(gameState.players[1]), listOf(gameState.players[0]))
            3 -> Pair(listOf(gameState.players[0], gameState.players[1]), listOf(gameState.players[2]))
            else -> Pair(
                listOf(gameState.players[0], gameState.players[1]),
                listOf(gameState.players[3], gameState.players[2])
            )
        }
    }

    // Hop animation states
    var animatingTokenId by remember { mutableStateOf<Int?>(null) }
    var animatingTokenCoord by remember { mutableStateOf<GridCoord?>(null) }
    var animatingHopElevationPx by remember { mutableFloatStateOf(0f) }
    var isHopAnimating by remember { mutableStateOf(false) }

    // Turn Timer state
    var timerRemainingSecs by remember { mutableIntStateOf(turnTimerLimitSecs) }
    val timerProgress = if (isTurnTimerEnabled && turnTimerLimitSecs > 0) {
        (timerRemainingSecs.toFloat() / turnTimerLimitSecs).coerceIn(0f, 1f)
    } else null

    // Reset turn timer whenever turn changes or state transitions
    LaunchedEffect(currentPlayer.id, gameState.isWaitingForRoll, gameState.isDiceRolling, isHopAnimating) {
        timerRemainingSecs = turnTimerLimitSecs
    }

    // Turn countdown ticker
    LaunchedEffect(isTurnTimerEnabled, gameState.isGameOver, showPauseDialog, isHopAnimating, gameState.isDiceRolling, currentPlayer.id) {
        if (isTurnTimerEnabled && !gameState.isGameOver && !showPauseDialog && !isHopAnimating && !gameState.isDiceRolling) {
            while (timerRemainingSecs > 0) {
                delay(1000)
                timerRemainingSecs -= 1
            }
            // Timer expired: auto-play fallback
            if (!isHopAnimating && !gameState.isGameOver) {
                if (gameState.isWaitingForRoll) {
                    handleRoll(
                        currentState = gameState,
                        soundManager = soundManager,
                        coroutineScope = coroutineScope,
                        onStateUpdated = onStateUpdated,
                        onRollStarted = { rolled -> playerLastDiceValue[currentPlayer.id] = rolled }
                    )
                } else if (gameState.movableTokenIds.isNotEmpty()) {
                    val fallbackId = gameState.movableTokenIds.first()
                    startHopAndMove(
                        tokenId = fallbackId,
                        currentState = gameState,
                        soundManager = soundManager,
                        coroutineScope = coroutineScope,
                        onStateUpdated = onStateUpdated,
                        setAnimatingToken = { id, coord, elev ->
                            animatingTokenId = id
                            animatingTokenCoord = coord
                            animatingHopElevationPx = elev
                        },
                        setHopAnimating = { isHopAnimating = it }
                    )
                }
            }
        }
    }

    // Centralized DIRECT BOARD TAP executor
    val onSelectPawn: (Int) -> Unit = { tokenId ->
        if (!isHopAnimating && !currentPlayer.isBot && gameState.movableTokenIds.contains(tokenId)) {
            soundManager.playPawnPick()
            startHopAndMove(
                tokenId = tokenId,
                currentState = gameState,
                soundManager = soundManager,
                coroutineScope = coroutineScope,
                onStateUpdated = onStateUpdated,
                setAnimatingToken = { id, coord, elev ->
                    animatingTokenId = id
                    animatingTokenCoord = coord
                    animatingHopElevationPx = elev
                },
                setHopAnimating = { isHopAnimating = it }
            )
        }
    }

    // Auto-advance turn if no moves available
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating) {
        if (!gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isEmpty() && !isHopAnimating) {
            delay(1100)
            val nextIndex = LudoGameEngine.getNextActivePlayerIndex(gameState.players, gameState.currentPlayerIndex)
            val nextState = gameState.copy(
                currentPlayerIndex = nextIndex,
                isWaitingForRoll = true,
                movableTokenIds = emptySet(),
                statusMessage = "${gameState.players[nextIndex].name}'s turn to roll",
                turnCount = gameState.turnCount + 1
            )
            onStateUpdated(nextState)
        }
    }

    // Auto-move single eligible pawn after 400ms (Setting: "Auto-move single pawn")
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating, currentPlayer.isBot, isAutoMoveEnabled) {
        if (isAutoMoveEnabled && !currentPlayer.isBot && !gameState.isWaitingForRoll && !gameState.isDiceRolling &&
            gameState.movableTokenIds.size == 1 && !isHopAnimating
        ) {
            val singleTokenId = gameState.movableTokenIds.first()
            delay(400)
            onSelectPawn(singleTokenId)
        }
    }

    // Coordinate automated Bot turns
    LaunchedEffect(currentPlayer.isBot, gameState.isWaitingForRoll, gameState.isDiceRolling, isHopAnimating, gameState.isGameOver) {
        if (currentPlayer.isBot && !gameState.isGameOver && !isHopAnimating) {
            if (gameState.isWaitingForRoll && !gameState.isDiceRolling) {
                delay(450)
                handleRoll(
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    onStateUpdated = onStateUpdated,
                    onRollStarted = { rolled -> playerLastDiceValue[currentPlayer.id] = rolled }
                )
            } else if (!gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isNotEmpty()) {
                delay(450)
                val chosenTokenId = LudoBotAI.chooseMove(currentPlayer, gameState, gameState.movableTokenIds)
                startHopAndMove(
                    tokenId = chosenTokenId,
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    onStateUpdated = onStateUpdated,
                    setAnimatingToken = { id, coord, elev ->
                        animatingTokenId = id
                        animatingTokenCoord = coord
                        animatingHopElevationPx = elev
                    },
                    setHopAnimating = { isHopAnimating = it }
                )
            }
        }
    }

    // Victory celebration check
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            soundManager.playVictory()
            delay(600)
            onGameFinished(gameState)
        }
    }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("game_screen")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Navigation Bar: Pause Button, Crown Turn Pill, Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundManager.playButtonTap()
                            showPauseDialog = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D1D38))
                            .border(1.5.dp, Brush.linearGradient(GoldRimGradientColors), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Turn indicator pill
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0A1428))
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TURN ${gameState.turnCount} • ${currentPlayer.name.uppercase()}",
                            style = Typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = AccentPrimary
                        )
                        if (currentPlayer.isBot) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI",
                                tint = StarGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isSoundMuted = !isSoundMuted
                            soundManager.isSoundEnabled = !isSoundMuted
                            soundManager.playButtonTap()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D1D38))
                            .border(1.5.dp, Brush.linearGradient(GoldRimGradientColors), CircleShape)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Sound",
                            tint = if (isSoundMuted) TextMuted else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2. Top Player Panels with individual dice
                PlayerDiceRow(
                    players = topPlayers,
                    currentPlayer = currentPlayer,
                    gameState = gameState,
                    playerLastDiceValue = playerLastDiceValue,
                    turnTimerProgress = timerProgress,
                    isHopAnimating = isHopAnimating,
                    isTop = true,
                    onBounce = { soundManager.playDiceRoll() },
                    onRollClick = { player ->
                        if (player.id == currentPlayer.id) {
                            handleRoll(
                                currentState = gameState,
                                soundManager = soundManager,
                                coroutineScope = coroutineScope,
                                onStateUpdated = onStateUpdated,
                                onRollStarted = { rolled -> playerLastDiceValue[player.id] = rolled }
                            )
                        }
                    }
                )

                // 3. Central Ludo Board (DIRECT BOARD TAP WITH NO POPUP)
                LudoBoardView(
                    players = gameState.players,
                    currentPlayer = currentPlayer,
                    movableTokenIds = if (isHopAnimating || currentPlayer.isBot) emptySet() else gameState.movableTokenIds,
                    diceValue = gameState.diceValue,
                    onTokenClick = onSelectPawn,
                    animatingTokenId = animatingTokenId,
                    animatingTokenCoord = animatingTokenCoord,
                    animatingHopElevationPx = animatingHopElevationPx,
                    showColorBlindSymbol = isColorBlindEnabled,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // 4. Bottom Player Panels with individual dice
                PlayerDiceRow(
                    players = bottomPlayers,
                    currentPlayer = currentPlayer,
                    gameState = gameState,
                    playerLastDiceValue = playerLastDiceValue,
                    turnTimerProgress = timerProgress,
                    isHopAnimating = isHopAnimating,
                    isTop = false,
                    onBounce = { soundManager.playDiceRoll() },
                    onRollClick = { player ->
                        if (player.id == currentPlayer.id) {
                            handleRoll(
                                currentState = gameState,
                                soundManager = soundManager,
                                coroutineScope = coroutineScope,
                                onStateUpdated = onStateUpdated,
                                onRollStarted = { rolled -> playerLastDiceValue[player.id] = rolled }
                            )
                        }
                    }
                )

                // 5. Natural Bottom Banner Ad (Seamlessly replaces redundant bottom card)
                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placementKey = AdConstants.KEY_BANNER_GAME,
                    hasCardContainer = true,
                    cornerRadius = 20.dp,
                    containerBackground = Color(0xFF080F22),
                    containerBorderColor = Color(0xFF293B5B)
                )
            }

            // Universal Pause Dialog using GameDialog
            GameDialog(
                visible = showPauseDialog,
                title = "Game Paused",
                onDismissRequest = { showPauseDialog = false },
                buttons = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GameButton(
                            text = "Resume Match",
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                            },
                            style = GameButtonStyle.PRIMARY_GOLD,
                            icon = Icons.Default.PlayArrow
                        )
                        GameButton(
                            text = "Restart Match",
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                                onRestartGame()
                            },
                            style = GameButtonStyle.SECONDARY_GLASS,
                            icon = Icons.Outlined.Refresh
                        )
                        GameButton(
                            text = "Main Menu",
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                                onExitToMenu()
                            },
                            style = GameButtonStyle.SECONDARY_GLASS,
                            icon = Icons.Outlined.Home
                        )
                    }
                }
            ) {
                Text(
                    text = "Match progress is preserved automatically.",
                    style = Typography.bodyMedium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Audio Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                            .clickable {
                                isSoundMuted = !isSoundMuted
                                soundManager.isSoundEnabled = !isSoundMuted
                                soundManager.playButtonTap()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isSoundMuted) TextMuted else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSoundMuted) "SFX Off" else "SFX On",
                                style = Typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                            .clickable {
                                isMusicOn = !isMusicOn
                                soundManager.isMusicEnabled = isMusicOn
                                soundManager.playButtonTap()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMusicOn) Icons.Outlined.MusicNote else Icons.Outlined.MusicOff,
                                contentDescription = null,
                                tint = if (isMusicOn) TextPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMusicOn) "Music On" else "Music Off",
                                style = Typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerDiceRow(
    players: List<Player>,
    currentPlayer: Player,
    gameState: GameState,
    playerLastDiceValue: Map<Int, Int>,
    turnTimerProgress: Float?,
    isHopAnimating: Boolean,
    isTop: Boolean,
    onBounce: () -> Unit,
    onRollClick: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEachIndexed { index, player ->
            val isActive = player.id == currentPlayer.id
            val isSingle = players.size == 1
            val alignDiceRight = if (isSingle) true else (index % 2 == 1)
            val canRoll = isActive && gameState.isWaitingForRoll && !gameState.isDiceRolling && !player.isBot && !isHopAnimating
            val diceVal = if (isActive && gameState.diceValue in 1..6) gameState.diceValue else (playerLastDiceValue[player.id] ?: 1)

            val turnHint = when {
                !isActive -> null
                gameState.isDiceRolling -> "ROLLING"
                canRoll -> "TAP DICE"
                player.isBot -> "AI THINKING"
                gameState.movableTokenIds.isNotEmpty() -> "TAP PAWN"
                else -> null
            }

            PlayerDicePanel(
                player = player,
                isActive = isActive,
                diceValue = diceVal,
                isRolling = isActive && gameState.isDiceRolling,
                canRoll = canRoll,
                turnStatusHint = turnHint,
                turnTimerProgress = if (isActive) turnTimerProgress else null,
                alignDiceRight = alignDiceRight,
                isTopPanel = isTop,
                onBounce = onBounce,
                onRollClick = { onRollClick(player) },
                modifier = if (isSingle) Modifier.fillMaxWidth() else Modifier.weight(1f)
            )
        }
    }
}

private fun handleRoll(
    currentState: GameState,
    soundManager: SoundManager,
    coroutineScope: CoroutineScope,
    onStateUpdated: (GameState) -> Unit,
    onRollStarted: (Int) -> Unit = {}
) {
    if (!currentState.isWaitingForRoll || currentState.isDiceRolling) return

    val rolledValue = Random.nextInt(1, 7)
    val currPlayer = currentState.currentPlayer
    val consecutiveSixes = if (rolledValue == 6) currentState.consecutiveSixes + 1 else 0

    onRollStarted(rolledValue)
    soundManager.playDiceRoll()

    val rollingState = currentState.copy(
        isDiceRolling = true,
        isWaitingForRoll = false,
        diceValue = rolledValue
    )
    onStateUpdated(rollingState)

    coroutineScope.launch {
        delay(540)

        // 3 consecutive sixes penalty
        if (consecutiveSixes >= 3) {
            val nextIndex = LudoGameEngine.getNextActivePlayerIndex(currentState.players, currentState.currentPlayerIndex)
            val penaltyState = currentState.copy(
                isDiceRolling = false,
                isWaitingForRoll = true,
                diceValue = rolledValue,
                consecutiveSixes = 0,
                currentPlayerIndex = nextIndex,
                statusMessage = "${currPlayer.name} rolled 3 sixes! Turn passed.",
                turnCount = currentState.turnCount + 1
            )
            onStateUpdated(penaltyState)
            return@launch
        }

        val movableIds = LudoGameEngine.getMovableTokenIds(currPlayer, rolledValue)
        val updatedSixes = if (rolledValue == 6) currPlayer.sixesCount + 1 else currPlayer.sixesCount
        val updatedPlayer = currPlayer.copy(sixesCount = updatedSixes)
        val allPlayers = currentState.players.toMutableList()
        val currIdx = allPlayers.indexOfFirst { it.id == currPlayer.id }
        allPlayers[currIdx] = updatedPlayer

        val status = when {
            movableIds.isEmpty() -> "${currPlayer.name} rolled $rolledValue. No valid moves."
            movableIds.size > 1 -> "Tap a glowing pawn"
            rolledValue == 6 -> "${currPlayer.name} rolled 6! Extra turn!"
            else -> "${currPlayer.name} rolled $rolledValue."
        }

        val postRollState = currentState.copy(
            players = allPlayers,
            isDiceRolling = false,
            isWaitingForRoll = false,
            diceValue = rolledValue,
            movableTokenIds = movableIds,
            consecutiveSixes = consecutiveSixes,
            statusMessage = status
        )
        onStateUpdated(postRollState)
    }
}

private fun startHopAndMove(
    tokenId: Int,
    currentState: GameState,
    soundManager: SoundManager,
    coroutineScope: CoroutineScope,
    onStateUpdated: (GameState) -> Unit,
    setAnimatingToken: (Int?, GridCoord?, Float) -> Unit,
    setHopAnimating: (Boolean) -> Unit
) {
    val currPlayer = currentState.currentPlayer
    val token = currPlayer.tokens.firstOrNull { it.id == tokenId } ?: return

    val hopPath = BoardCoordinates.computeHopPath(token, currentState.diceValue)
    if (hopPath.size <= 1) {
        val (nextState, outcome) = LudoGameEngine.executeMove(currentState, tokenId)
        when (outcome) {
            is MoveOutcome.Moved -> {
                if (outcome.capturedPlayerColor != null) soundManager.playCapture()
                else if (outcome.tokenFinished) soundManager.playTokenHome()
            }
            else -> {}
        }
        onStateUpdated(nextState)
        return
    }

    setHopAnimating(true)

    coroutineScope.launch {
        for (i in 1 until hopPath.size) {
            val startCoord = hopPath[i - 1]
            val endCoord = hopPath[i]

            val frames = 8
            val frameDelay = 18L
            for (f in 1..frames) {
                val t = f.toFloat() / frames
                val curRow = startCoord.row + (endCoord.row - startCoord.row) * t
                val curCol = startCoord.col + (endCoord.col - startCoord.col) * t
                val elevation = 22f * sin(PI.toFloat() * t)

                setAnimatingToken(tokenId, GridCoord(curRow, curCol), elevation)
                delay(frameDelay)
            }
            soundManager.playTokenStep()
        }

        setAnimatingToken(null, null, 0f)
        setHopAnimating(false)

        val (nextState, outcome) = LudoGameEngine.executeMove(currentState, tokenId)
        when (outcome) {
            is MoveOutcome.Moved -> {
                if (outcome.capturedPlayerColor != null) soundManager.playCapture()
                else if (outcome.tokenFinished) soundManager.playTokenHome()
            }
            else -> {}
        }
        onStateUpdated(nextState)
    }
}
