package com.example.ludo.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.AccentPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import com.example.ludo.p2p.P2PEmoteMessage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.AdConstants
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ai.LudoBotAI
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.AnimationIntensity
import com.example.ludo.data.GamePreferences
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GameState
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.ActiveCaptureAnimation
import com.example.ludo.ui.components.ActiveFinishAnimation
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameDialog
import com.example.ludo.ui.components.GameToastBanner
import com.example.ludo.ui.components.GameToastNotification
import com.example.ludo.ui.components.LudoBoardView
import com.example.ludo.ui.components.PlayerDicePanel
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * World-Class Responsive Ludo Game Screen:
 * - Predictive BackHandler with confirm leave match dialog.
 * - Adaptive layout: side-by-side on tablet/landscape, vertical stacked on phone.
 * - Score strip (pawns home e.g. "2/4 Home", captures count).
 * - Contextual hint line ("Tap dice", "Tap a glowing pawn", "AI thinking...").
 * - High-impact ~1.2s capture animation with white hit flash, double shockwave, sparks,
 *   pawn squash & spin flight, board shake, and "CAPTURED!" toast.
 * - Winning pawn 3D spin into dedicated 2x2 finish quadrant with golden rays and "HOME SAFE!" toast.
 * - Low-power / animation intensity aware (Full, Reduced, Off).
 */
@Composable
fun GameScreen(
    gameState: GameState,
    soundManager: SoundManager,
    gamePreferences: GamePreferences? = null,
    onStateUpdated: (GameState) -> Unit,
    onGameFinished: (GameState) -> Unit,
    onExitToMenu: () -> Unit,
    onRestartGame: () -> Unit,
    onWatchRewardAd: (() -> Unit)? = null,
    isP2PMultiplayer: Boolean = false,
    p2pLocalPlayerIndex: Int = 0,
    p2pTransportName: String = "Wi-Fi Direct",
    onP2PRollDice: (() -> Unit)? = null,
    onP2PSelectPawn: ((Int) -> Unit)? = null,
    onP2PSendEmote: ((String) -> Unit)? = null,
    p2pLatestEmote: P2PEmoteMessage? = null
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    var isSoundMuted by remember { mutableStateOf(!soundManager.isSoundEnabled) }
    var isMusicOn by remember { mutableStateOf(soundManager.isMusicEnabled) }
    val coroutineScope = rememberCoroutineScope()
    val currentPlayer = gameState.currentPlayer

    // In-game predictive back interceptor
    BackHandler {
        showPauseDialog = true
    }

    // Never play background music during active game match
    DisposableEffect(Unit) {
        soundManager.pauseMusic()
        onDispose {
            if (gamePreferences?.isMusicEnabled == true) {
                soundManager.resumeMusic()
            }
        }
    }

    val isAutoMoveEnabled = gamePreferences?.isAutoMoveSinglePawnEnabled ?: true
    val isTurnTimerEnabled = gamePreferences?.isTurnTimerEnabled ?: true
    val turnTimerLimitSecs = gamePreferences?.turnTimerSeconds ?: 15
    val isColorBlindEnabled = gamePreferences?.isColorBlindModeEnabled ?: false
    val animationIntensity = gamePreferences?.animationIntensity ?: AnimationIntensity.FULL
    val isAnimationsEnabled = animationIntensity != AnimationIntensity.OFF

    // Floating toast state
    var activeToast by remember { mutableStateOf<GameToastNotification?>(null) }

    // Screen shake offsets
    var shakeOffsetX by remember { mutableFloatStateOf(0f) }
    var shakeOffsetY by remember { mutableFloatStateOf(0f) }

    // Capture & Finish animations
    var activeCaptureAnim by remember { mutableStateOf<ActiveCaptureAnimation?>(null) }
    var activeFinishAnim by remember { mutableStateOf<ActiveFinishAnimation?>(null) }

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
    LaunchedEffect(currentPlayer.id, gameState.isWaitingForRoll, gameState.isDiceRolling, isHopAnimating, activeCaptureAnim, activeFinishAnim) {
        timerRemainingSecs = turnTimerLimitSecs
    }

    // Turn countdown ticker
    LaunchedEffect(
        isTurnTimerEnabled, gameState.isGameOver, showPauseDialog, isHopAnimating,
        gameState.isDiceRolling, currentPlayer.id, activeCaptureAnim, activeFinishAnim
    ) {
        if (isTurnTimerEnabled && !gameState.isGameOver && !showPauseDialog && !isHopAnimating &&
            !gameState.isDiceRolling && activeCaptureAnim == null && activeFinishAnim == null
        ) {
            while (timerRemainingSecs > 0) {
                delay(1000)
                timerRemainingSecs -= 1
            }
            // Timer expired: auto-play fallback
            if (!isHopAnimating && !gameState.isGameOver && activeCaptureAnim == null && activeFinishAnim == null) {
                if (gameState.isWaitingForRoll) {
                    handleRoll(
                        currentState = gameState,
                        soundManager = soundManager,
                        coroutineScope = coroutineScope,
                        onStateUpdated = onStateUpdated,
                        onRollStarted = { rolled -> playerLastDiceValue[currentPlayer.id] = rolled },
                        onShowToast = { t -> activeToast = t }
                    )
                } else if (gameState.movableTokenIds.isNotEmpty()) {
                    val fallbackId = gameState.movableTokenIds.first()
                    startHopAndMove(
                        tokenId = fallbackId,
                        currentState = gameState,
                        soundManager = soundManager,
                        coroutineScope = coroutineScope,
                        animationIntensity = animationIntensity,
                        onStateUpdated = onStateUpdated,
                        onGameFinished = onGameFinished,
                        setAnimatingToken = { id, coord, elev ->
                            animatingTokenId = id
                            animatingTokenCoord = coord
                            animatingHopElevationPx = elev
                        },
                        setHopAnimating = { isHopAnimating = it },
                        setCaptureAnimation = { activeCaptureAnim = it },
                        setFinishAnimation = { activeFinishAnim = it },
                        setShakeOffset = { x, y ->
                            shakeOffsetX = x
                            shakeOffsetY = y
                        },
                        onShowToast = { t -> activeToast = t }
                    )
                }
            }
        }
    }

    // Centralized DIRECT BOARD TAP executor
    val onSelectPawn: (Int) -> Unit = { tokenId ->
        if (!isHopAnimating && activeCaptureAnim == null && activeFinishAnim == null &&
            !currentPlayer.isBot && gameState.movableTokenIds.contains(tokenId)
        ) {
            soundManager.playPawnPick()
            startHopAndMove(
                tokenId = tokenId,
                currentState = gameState,
                soundManager = soundManager,
                coroutineScope = coroutineScope,
                animationIntensity = animationIntensity,
                onStateUpdated = onStateUpdated,
                onGameFinished = onGameFinished,
                setAnimatingToken = { id, coord, elev ->
                    animatingTokenId = id
                    animatingTokenCoord = coord
                    animatingHopElevationPx = elev
                },
                setHopAnimating = { isHopAnimating = it },
                setCaptureAnimation = { activeCaptureAnim = it },
                setFinishAnimation = { activeFinishAnim = it },
                setShakeOffset = { x, y ->
                    shakeOffsetX = x
                    shakeOffsetY = y
                },
                onShowToast = { t -> activeToast = t }
            )
        }
    }

    var showEmotePicker by remember { mutableStateOf(false) }

    // Emote incoming listener
    LaunchedEffect(p2pLatestEmote) {
        p2pLatestEmote?.let { emote ->
            activeToast = GameToastNotification(
                title = emote.senderName,
                subtitle = emote.message,
                accentColor = emote.senderColor.accentColor
            )
        }
    }

    val handleDiceRollClick: (Player) -> Unit = { player ->
        if (player.id == currentPlayer.id) {
            if (isP2PMultiplayer) {
                if (player.id == p2pLocalPlayerIndex) {
                    onP2PRollDice?.invoke()
                } else {
                    activeToast = GameToastNotification(
                        title = "Opponent's Turn",
                        subtitle = "Waiting for ${currentPlayer.name} to roll...",
                        accentColor = currentPlayer.color.accentColor
                    )
                }
            } else {
                handleRoll(
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    onStateUpdated = onStateUpdated,
                    onRollStarted = { rolled -> playerLastDiceValue[player.id] = rolled },
                    onShowToast = { t -> activeToast = t }
                )
            }
        }
    }

    val handleTokenClick: (Int) -> Unit = { tokenId ->
        if (isP2PMultiplayer) {
            if (currentPlayer.id == p2pLocalPlayerIndex && gameState.movableTokenIds.contains(tokenId)) {
                onP2PSelectPawn?.invoke(tokenId)
            }
        } else {
            onSelectPawn(tokenId)
        }
    }

    // Auto-advance turn if no moves available (local pass & play mode only)
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating, activeCaptureAnim, activeFinishAnim, isP2PMultiplayer) {
        if (!isP2PMultiplayer && !gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isEmpty() &&
            !isHopAnimating && activeCaptureAnim == null && activeFinishAnim == null
        ) {
            delay(1000)
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

    // Auto-move single eligible pawn after 400ms (local mode only)
    LaunchedEffect(
        gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling,
        isHopAnimating, currentPlayer.isBot, isAutoMoveEnabled, activeCaptureAnim, activeFinishAnim, isP2PMultiplayer
    ) {
        if (!isP2PMultiplayer && isAutoMoveEnabled && !currentPlayer.isBot && !gameState.isWaitingForRoll && !gameState.isDiceRolling &&
            gameState.movableTokenIds.size == 1 && !isHopAnimating && activeCaptureAnim == null && activeFinishAnim == null
        ) {
            val singleTokenId = gameState.movableTokenIds.first()
            delay(400)
            onSelectPawn(singleTokenId)
        }
    }

    // Coordinate automated Bot turns (local mode only; in P2P Host coordinates bots)
    LaunchedEffect(
        currentPlayer.isBot, gameState.isWaitingForRoll, gameState.isDiceRolling,
        isHopAnimating, gameState.isGameOver, activeCaptureAnim, activeFinishAnim, isP2PMultiplayer
    ) {
        if (!isP2PMultiplayer && currentPlayer.isBot && !gameState.isGameOver && !isHopAnimating &&
            activeCaptureAnim == null && activeFinishAnim == null
        ) {
            if (gameState.isWaitingForRoll && !gameState.isDiceRolling) {
                delay(400)
                handleRoll(
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    onStateUpdated = onStateUpdated,
                    onRollStarted = { rolled -> playerLastDiceValue[currentPlayer.id] = rolled },
                    onShowToast = { t -> activeToast = t }
                )
            } else if (!gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isNotEmpty()) {
                delay(400)
                val chosenTokenId = LudoBotAI.chooseMove(currentPlayer, gameState, gameState.movableTokenIds)
                startHopAndMove(
                    tokenId = chosenTokenId,
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    animationIntensity = animationIntensity,
                    onStateUpdated = onStateUpdated,
                    onGameFinished = onGameFinished,
                    setAnimatingToken = { id, coord, elev ->
                        animatingTokenId = id
                        animatingTokenCoord = coord
                        animatingHopElevationPx = elev
                    },
                    setHopAnimating = { isHopAnimating = it },
                    setCaptureAnimation = { activeCaptureAnim = it },
                    setFinishAnimation = { activeFinishAnim = it },
                    setShakeOffset = { x, y ->
                        shakeOffsetX = x
                        shakeOffsetY = y
                    },
                    onShowToast = { t -> activeToast = t }
                )
            }
        }
    }

    // Auto dismiss toast after 2.2s
    LaunchedEffect(activeToast) {
        if (activeToast != null) {
            delay(2200)
            activeToast = null
        }
    }

    val activeTheme = remember(gamePreferences?.selectedBoardThemeId) {
        com.example.ludo.model.BoardThemeCatalog.getThemeById(gamePreferences?.selectedBoardThemeId)
    }

    GameBackground(enableTwinkle = animationIntensity == AnimationIntensity.FULL) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("game_screen")
        ) {
            val isLandscapeOrTablet = maxWidth > maxHeight || maxWidth >= 600.dp

            if (isLandscapeOrTablet) {
                // Wide / Tablet / Landscape Layout: Board in center, panels on left and right
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Top Bar, Left Players, Score Strip
                    Column(
                        modifier = Modifier
                            .widthIn(min = 180.dp, max = 240.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameTopHeader(
                            gameState = gameState,
                            currentPlayer = currentPlayer,
                            isSoundMuted = isSoundMuted,
                            onPauseClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = true
                            },
                            onSoundToggleClick = {
                                isSoundMuted = !isSoundMuted
                                soundManager.isSoundEnabled = !isSoundMuted
                                soundManager.playButtonTap()
                            },
                            isP2PMultiplayer = isP2PMultiplayer,
                            p2pTransportName = p2pTransportName,
                            p2pMyColor = gameState.players.getOrNull(p2pLocalPlayerIndex)?.color
                        )

                        ScoreStrip(players = gameState.players, currentPlayer = currentPlayer)

                        PlayerDiceRow(
                            players = topPlayers,
                            currentPlayer = currentPlayer,
                            gameState = gameState,
                            playerLastDiceValue = playerLastDiceValue,
                            turnTimerProgress = timerProgress,
                            isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null,
                            isTop = true,
                            onBounce = {},
                            onRollClick = handleDiceRollClick
                        )
                    }

                    // Center: Large Square Centered Board
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoBoardView(
                            players = gameState.players,
                            currentPlayer = currentPlayer,
                            movableTokenIds = if (isHopAnimating || currentPlayer.isBot || activeCaptureAnim != null || activeFinishAnim != null) emptySet() else gameState.movableTokenIds,
                            diceValue = gameState.diceValue,
                            onTokenClick = handleTokenClick,
                            animatingTokenId = animatingTokenId,
                            animatingTokenCoord = animatingTokenCoord,
                            animatingHopElevationPx = animatingHopElevationPx,
                            showColorBlindSymbol = isColorBlindEnabled,
                            boardTheme = activeTheme,
                            activeCaptureAnim = activeCaptureAnim,
                            activeFinishAnim = activeFinishAnim,
                            shakeOffsetX = shakeOffsetX,
                            shakeOffsetY = shakeOffsetY,
                            enableAmbientAnimation = animationIntensity == AnimationIntensity.FULL,
                            modifier = Modifier.aspectRatio(1f)
                        )

                        // Floating Toast Overlay
                        GameToastBanner(
                            toast = activeToast,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                        )

                        // P2P Quick Emote Bar in Board Corner
                        if (isP2PMultiplayer) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    AnimatedVisibility(visible = showEmotePicker) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(SurfaceCard)
                                                .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("🎲", "🔥", "😂", "👏", "👑", "GG!", "Your turn!").forEach { emote ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(SurfaceElevated)
                                                        .clickable {
                                                            onP2PSendEmote?.invoke(emote)
                                                            showEmotePicker = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Text(text = emote, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceCard)
                                            .border(1.2.dp, AccentPrimary, RoundedCornerShape(20.dp))
                                            .clickable { showEmotePicker = !showEmotePicker }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Emotes",
                                                tint = AccentPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Emotes",
                                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Right Players, Hint Line, Banner Ad
                    Column(
                        modifier = Modifier
                            .widthIn(min = 180.dp, max = 240.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HintLine(
                            gameState = gameState,
                            currentPlayer = currentPlayer,
                            isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null
                        )

                        PlayerDiceRow(
                            players = bottomPlayers,
                            currentPlayer = currentPlayer,
                            gameState = gameState,
                            playerLastDiceValue = playerLastDiceValue,
                            turnTimerProgress = timerProgress,
                            isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null,
                            isTop = false,
                            onBounce = {},
                            onRollClick = handleDiceRollClick
                        )

                        BannerAdView(
                            modifier = Modifier.fillMaxWidth(),
                            placementKey = AdConstants.KEY_BANNER_GAME,
                            hasCardContainer = true,
                            cornerRadius = 12.dp,
                            containerBackground = SurfaceCard,
                            containerBorderColor = BorderHairline
                        )
                    }
                }
            } else {
                // Portrait Phone Layout: Top HUD, Score strip, Top panels, Board, Bottom panels, Banner Ad
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Top HUD
                        GameTopHeader(
                            gameState = gameState,
                            currentPlayer = currentPlayer,
                            isSoundMuted = isSoundMuted,
                            onPauseClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = true
                            },
                            onSoundToggleClick = {
                                isSoundMuted = !isSoundMuted
                                soundManager.isSoundEnabled = !isSoundMuted
                                soundManager.playButtonTap()
                            },
                            isP2PMultiplayer = isP2PMultiplayer,
                            p2pTransportName = p2pTransportName,
                            p2pMyColor = gameState.players.getOrNull(p2pLocalPlayerIndex)?.color
                        )

                        // 2. Score Strip & Hint Line
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScoreStrip(players = gameState.players, currentPlayer = currentPlayer)
                            HintLine(
                                gameState = gameState,
                                currentPlayer = currentPlayer,
                                isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null
                            )
                        }
                    }

                    // 3. Top Player Panels
                    PlayerDiceRow(
                        players = topPlayers,
                        currentPlayer = currentPlayer,
                        gameState = gameState,
                        playerLastDiceValue = playerLastDiceValue,
                        turnTimerProgress = timerProgress,
                        isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null,
                        isTop = true,
                        onBounce = {},
                        onRollClick = handleDiceRollClick
                    )

                    // 4. Centered Board
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoBoardView(
                            players = gameState.players,
                            currentPlayer = currentPlayer,
                            movableTokenIds = if (isHopAnimating || currentPlayer.isBot || activeCaptureAnim != null || activeFinishAnim != null) emptySet() else gameState.movableTokenIds,
                            diceValue = gameState.diceValue,
                            onTokenClick = handleTokenClick,
                            animatingTokenId = animatingTokenId,
                            animatingTokenCoord = animatingTokenCoord,
                            animatingHopElevationPx = animatingHopElevationPx,
                            showColorBlindSymbol = isColorBlindEnabled,
                            boardTheme = activeTheme,
                            activeCaptureAnim = activeCaptureAnim,
                            activeFinishAnim = activeFinishAnim,
                            shakeOffsetX = shakeOffsetX,
                            shakeOffsetY = shakeOffsetY,
                            enableAmbientAnimation = animationIntensity == AnimationIntensity.FULL,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Toast Overlay
                        GameToastBanner(
                            toast = activeToast,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                        )

                        // P2P Quick Emote Bar in Board Corner
                        if (isP2PMultiplayer) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 8.dp, bottom = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    AnimatedVisibility(visible = showEmotePicker) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(SurfaceCard)
                                                .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("🎲", "🔥", "😂", "👏", "👑", "GG!", "Your turn!").forEach { emote ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(SurfaceElevated)
                                                        .clickable {
                                                            onP2PSendEmote?.invoke(emote)
                                                            showEmotePicker = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Text(text = emote, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceCard)
                                            .border(1.2.dp, AccentPrimary, RoundedCornerShape(20.dp))
                                            .clickable { showEmotePicker = !showEmotePicker }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Emotes",
                                                tint = AccentPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Emotes",
                                                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Bottom Player Panels
                    PlayerDiceRow(
                        players = bottomPlayers,
                        currentPlayer = currentPlayer,
                        gameState = gameState,
                        playerLastDiceValue = playerLastDiceValue,
                        turnTimerProgress = timerProgress,
                        isHopAnimating = isHopAnimating || activeCaptureAnim != null || activeFinishAnim != null,
                        isTop = false,
                        onBounce = {},
                        onRollClick = handleDiceRollClick
                    )

                    // 6. Bottom Banner Ad
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        BannerAdView(
                            modifier = Modifier.fillMaxWidth(),
                            placementKey = AdConstants.KEY_BANNER_GAME,
                            hasCardContainer = true,
                            cornerRadius = 12.dp,
                            containerBackground = SurfaceCard,
                            containerBorderColor = BorderHairline
                        )
                    }
                }
            }

            // In-Game Confirm Pause / Leave Match Dialog
            GameDialog(
                visible = showPauseDialog,
                title = "Match Paused",
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
                        if (onWatchRewardAd != null && com.example.ludo.ads.RemoteConfigManager.isRewardedRerollEnabled()) {
                            val currentRerolls = gamePreferences?.luckyRerollsCount ?: 0
                            GameButton(
                                text = "Claim Lucky Reroll Perk",
                                subtitle = "Watch short video ($currentRerolls available, +500 XP)",
                                onClick = {
                                    soundManager.playButtonTap()
                                    onWatchRewardAd()
                                },
                                style = GameButtonStyle.SECONDARY_GLASS,
                                icon = Icons.Outlined.Stars,
                                testTag = "pause_dialog_reward_button"
                            )
                        }
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
                            text = "Save & Exit to Menu",
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
                    text = "Your match progress is automatically autosaved and can be resumed at any time from Home.",
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick Audio Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
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
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
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
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
private fun GameTopHeader(
    gameState: GameState,
    currentPlayer: Player,
    isSoundMuted: Boolean,
    onPauseClick: () -> Unit,
    onSoundToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isP2PMultiplayer: Boolean = false,
    p2pTransportName: String = "",
    p2pMyColor: com.example.ludo.model.PlayerColor? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(1.dp, BorderHairline, CircleShape)
                .testTag("pause_button")
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Turn indicator pill
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(
                    width = 1.2.dp,
                    color = currentPlayer.color.primaryColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(currentPlayer.color.primaryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TURN ${gameState.turnCount} • ${currentPlayer.name.uppercase()}",
                style = Typography.titleMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = TextPrimary
            )
            if (currentPlayer.isBot) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = StarGold,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        if (isP2PMultiplayer) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (p2pTransportName.contains("Bluetooth", ignoreCase = true)) Icons.Default.Bluetooth else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (p2pMyColor != null) "P2P • ${p2pMyColor.displayName}" else "P2P",
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = AccentPrimary
                    )
                }
            }
        }

        IconButton(
            onClick = onSoundToggleClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceCard)
                .border(1.dp, BorderHairline, CircleShape)
                .testTag("sound_toggle_button")
        ) {
            Icon(
                imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Toggle Sound",
                tint = if (isSoundMuted) TextMuted else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ScoreStrip(
    players: List<Player>,
    currentPlayer: Player,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEach { p ->
            val homeCount = p.tokens.count { it.isFinished }
            val isCurrent = p.id == currentPlayer.id
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(p.color.primaryColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$homeCount/4",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCurrent) StarGold else TextSecondary
                )
                if (p.capturesCount > 0) {
                    Text(
                        text = " ⚔${p.capturesCount}",
                        style = Typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun HintLine(
    gameState: GameState,
    currentPlayer: Player,
    isHopAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    val hintText = when {
        isHopAnimating -> "Moving pawn..."
        gameState.isDiceRolling -> "Rolling dice..."
        gameState.isWaitingForRoll && currentPlayer.isBot -> "AI thinking..."
        gameState.isWaitingForRoll -> "Tap your dice to roll"
        gameState.movableTokenIds.size > 1 -> "Tap a glowing pawn to move"
        gameState.movableTokenIds.size == 1 -> "Pawn advancing..."
        else -> "Passing turn..."
    }

    Text(
        text = hintText,
        style = Typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
        color = if (gameState.movableTokenIds.isNotEmpty()) StarGold else TextMuted,
        modifier = modifier.padding(horizontal = 4.dp)
    )
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
    onRollStarted: (Int) -> Unit = {},
    onShowToast: (GameToastNotification) -> Unit = {}
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
        delay(520)

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

        if (rolledValue == 6) {
            onShowToast(
                GameToastNotification(
                    title = "ROLLED A 6!",
                    subtitle = "Extra turn earned for ${currPlayer.name}",
                    accentColor = StarGold,
                    icon = Icons.Default.Stars
                )
            )
        }

        val status = when {
            movableIds.isEmpty() -> "${currPlayer.name} rolled $rolledValue. No valid moves."
            movableIds.size > 1 -> "Tap a highlighted pawn"
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
    animationIntensity: AnimationIntensity,
    onStateUpdated: (GameState) -> Unit,
    onGameFinished: (GameState) -> Unit,
    setAnimatingToken: (Int?, GridCoord?, Float) -> Unit,
    setHopAnimating: (Boolean) -> Unit,
    setCaptureAnimation: (ActiveCaptureAnimation?) -> Unit,
    setFinishAnimation: (ActiveFinishAnimation?) -> Unit,
    setShakeOffset: (Float, Float) -> Unit,
    onShowToast: (GameToastNotification) -> Unit
) {
    val currPlayer = currentState.currentPlayer
    val token = currPlayer.tokens.firstOrNull { it.id == tokenId } ?: return

    val hopPath = BoardCoordinates.computeHopPath(token, currentState.diceValue)

    setHopAnimating(true)

    coroutineScope.launch {
        // Step 1: Hop Path Animation along board perimeter / home stretch
        if (hopPath.size > 1 && animationIntensity != AnimationIntensity.OFF) {
            val frames = if (animationIntensity == AnimationIntensity.REDUCED) 4 else 7
            val frameDelay = if (animationIntensity == AnimationIntensity.REDUCED) 14L else 18L

            for (i in 1 until hopPath.size) {
                val startCoord = hopPath[i - 1]
                val endCoord = hopPath[i]

                for (f in 1..frames) {
                    val t = f.toFloat() / frames
                    val curRow = startCoord.row + (endCoord.row - startCoord.row) * t
                    val curCol = startCoord.col + (endCoord.col - startCoord.col) * t
                    val elevation = 20f * sin(PI.toFloat() * t)

                    setAnimatingToken(tokenId, GridCoord(curRow, curCol), elevation)
                    delay(frameDelay)
                }
                soundManager.playTokenStep()
            }
        }

        setAnimatingToken(null, null, 0f)

        // Step 2: Execute move in engine
        val (nextState, outcome) = LudoGameEngine.executeMove(currentState, tokenId)

        when (outcome) {
            is MoveOutcome.Moved -> {
                // Step 3: Check for capture animation (~1.2s)
                if (outcome.capturedPlayerColor != null && outcome.capturedTokenId != null) {
                    val victimColor = outcome.capturedPlayerColor
                    val victimTokenId = outcome.capturedTokenId
                    val victimYardSlots = when (victimColor) {
                        PlayerColor.RED -> BoardCoordinates.RED_YARD_SLOTS
                        PlayerColor.GREEN -> BoardCoordinates.GREEN_YARD_SLOTS
                        PlayerColor.YELLOW -> BoardCoordinates.YELLOW_YARD_SLOTS
                        PlayerColor.BLUE -> BoardCoordinates.BLUE_YARD_SLOTS
                    }
                    val targetYardSlot = victimYardSlots[victimTokenId.coerceIn(0, 3)]
                    val landingCoord = BoardCoordinates.TRACK_TILES[currPlayer.color.stepToTrackIndex(token.stepsTraveled + currentState.diceValue)]

                    soundManager.playCapture()

                    onShowToast(
                        GameToastNotification(
                            title = "CAPTURED!",
                            subtitle = "${currPlayer.name} captured ${victimColor.displayName}!",
                            accentColor = currPlayer.color.primaryColor,
                            icon = Icons.Default.ElectricBolt
                        )
                    )

                    // Execute ~1.2s capture animation if not OFF
                    if (animationIntensity != AnimationIntensity.OFF) {
                        val captureTotalFrames = if (animationIntensity == AnimationIntensity.REDUCED) 14 else 28
                        val frameMs = 1200L / captureTotalFrames

                        for (step in 1..captureTotalFrames) {
                            val progress = step.toFloat() / captureTotalFrames
                            setCaptureAnimation(
                                ActiveCaptureAnimation(
                                    attackerColor = currPlayer.color,
                                    victimColor = victimColor,
                                    victimTokenId = victimTokenId,
                                    startCoord = landingCoord,
                                    yardSlotCoord = targetYardSlot,
                                    progress = progress
                                )
                            )

                            // Board shake during impact (first 25%)
                            if (progress < 0.25f) {
                                val shakeProgress = progress / 0.25f
                                val shakeMagnitude = (1f - shakeProgress) * 7f
                                val sx = sin(shakeProgress * 6 * PI.toFloat()) * shakeMagnitude
                                val sy = cos(shakeProgress * 6 * PI.toFloat()) * shakeMagnitude
                                setShakeOffset(sx, sy)
                            } else {
                                setShakeOffset(0f, 0f)
                            }

                            delay(frameMs)
                        }
                    }

                    setShakeOffset(0f, 0f)
                    setCaptureAnimation(null)
                }

                // Step 4: Check for winning pawn finish celebration
                if (outcome.tokenFinished) {
                    soundManager.playTokenHome()
                    val isFourth = outcome.finishOrder == 4
                    val targetSlot = BoardCoordinates.finishedSlot(currPlayer.color, outcome.finishOrder)

                    onShowToast(
                        GameToastNotification(
                            title = if (isFourth) "MATCH WINNER!" else "HOME SAFE!",
                            subtitle = "${currPlayer.name}'s pawn reached the center!",
                            accentColor = StarGold,
                            icon = if (isFourth) Icons.Default.EmojiEvents else Icons.Default.Stars
                        )
                    )

                    if (animationIntensity != AnimationIntensity.OFF) {
                        val finishFrames = if (animationIntensity == AnimationIntensity.REDUCED) 12 else 22
                        val frameMs = 900L / finishFrames

                        for (step in 1..finishFrames) {
                            val progress = step.toFloat() / finishFrames
                            setFinishAnimation(
                                ActiveFinishAnimation(
                                    playerColor = currPlayer.color,
                                    tokenId = tokenId,
                                    targetSlotCoord = targetSlot,
                                    finishOrder = outcome.finishOrder,
                                    isFourthPawn = isFourth,
                                    progress = progress
                                )
                            )
                            delay(frameMs)
                        }
                    }

                    setFinishAnimation(null)

                    if (isFourth) {
                        // Grand celebration delay before victory screen transition
                        soundManager.playVictory()
                        delay(600)
                        setHopAnimating(false)
                        onGameFinished(nextState)
                        return@launch
                    }
                }
            }
            else -> {}
        }

        setHopAnimating(false)
        onStateUpdated(nextState)
    }
}
