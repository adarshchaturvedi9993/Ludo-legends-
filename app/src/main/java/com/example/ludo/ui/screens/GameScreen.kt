package com.example.ludo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludo.ai.LudoBotAI
import com.example.ludo.audio.SoundManager
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.engine.MoveOutcome
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GameState
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.ui.components.LudoBoardView
import com.example.ludo.ui.components.PawnSelectionBottomSheet
import com.example.ludo.ui.components.PlayerBar
import com.example.ludo.ui.components.PlayerDicePanel
import com.example.ludo.ui.components.Realistic3DDiceView
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
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

@Composable
fun GameScreen(
    gameState: GameState,
    soundManager: SoundManager,
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

    // Individual player dice state tracking (retains last rolled value for each player)
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

    // Partition players to position panels adjacent to their board corner:
    // Top: Red (0) & Green (1)
    // Bottom: Blue (3) & Yellow (2)
    val (topPlayers, bottomPlayers) = remember(gameState.players) {
        when (gameState.players.size) {
            2 -> Pair(
                listOf(gameState.players[1]),
                listOf(gameState.players[0])
            )
            3 -> Pair(
                listOf(gameState.players[0], gameState.players[1]),
                listOf(gameState.players[2])
            )
            else -> Pair(
                listOf(gameState.players[0], gameState.players[1]),
                listOf(gameState.players[3], gameState.players[2])
            )
        }
    }

    // Hop-by-hop animation tracking states
    var animatingTokenId by remember { mutableStateOf<Int?>(null) }
    var animatingTokenCoord by remember { mutableStateOf<GridCoord?>(null) }
    var animatingHopElevationPx by remember { mutableFloatStateOf(0f) }
    var isHopAnimating by remember { mutableStateOf(false) }

    // Intelligent Pawn Selection Bottom Sheet state
    var showPawnSelectionSheet by remember { mutableStateOf(false) }

    // Centralized validated pawn selection & movement executor
    val onSelectPawn: (Int) -> Unit = { tokenId ->
        if (!isHopAnimating && !currentPlayer.isBot && gameState.movableTokenIds.contains(tokenId)) {
            showPawnSelectionSheet = false
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

    // Auto-open intelligent pawn selection bottom sheet when multiple pawns are eligible
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating, currentPlayer.isBot) {
        if (!currentPlayer.isBot && !gameState.isWaitingForRoll && !gameState.isDiceRolling &&
            gameState.movableTokenIds.size > 1 && !isHopAnimating
        ) {
            delay(120)
            showPawnSelectionSheet = true
        } else {
            showPawnSelectionSheet = false
        }
    }

    // Handle game over condition
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            soundManager.playVictory()
            delay(600)
            onGameFinished(gameState)
        }
    }

    // Auto-advance turn if no moves available
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating) {
        if (!gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isEmpty() && !isHopAnimating) {
            delay(1200)
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

    // Single legal move auto-execution for human players (no unnecessary popup)
    LaunchedEffect(gameState.isWaitingForRoll, gameState.movableTokenIds, gameState.isDiceRolling, isHopAnimating, currentPlayer.isBot) {
        if (!currentPlayer.isBot && !gameState.isWaitingForRoll && !gameState.isDiceRolling &&
            gameState.movableTokenIds.size == 1 && !isHopAnimating
        ) {
            val singleTokenId = gameState.movableTokenIds.first()
            delay(350)
            onSelectPawn(singleTokenId)
        }
    }

    // Coordinate automated Bot turns (Play With Computer)
    LaunchedEffect(currentPlayer.isBot, gameState.isWaitingForRoll, gameState.isDiceRolling, isHopAnimating, gameState.isGameOver) {
        if (currentPlayer.isBot && !gameState.isGameOver && !isHopAnimating) {
            if (gameState.isWaitingForRoll && !gameState.isDiceRolling) {
                // Realistic bot think time before rolling
                delay(450)
                handleRoll(
                    currentState = gameState,
                    soundManager = soundManager,
                    coroutineScope = coroutineScope,
                    onStateUpdated = onStateUpdated,
                    onRollStarted = { rolled ->
                        playerLastDiceValue[currentPlayer.id] = rolled
                    }
                )
            } else if (!gameState.isWaitingForRoll && !gameState.isDiceRolling && gameState.movableTokenIds.isNotEmpty()) {
                // Realistic bot tactical decision time before moving
                delay(420)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("game_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header: Pause button, Crown Turn indicator pill, Sound toggle (Reference Image 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Menu Button with Gold Outer Rim
                IconButton(
                    onClick = {
                        soundManager.playButtonTap()
                        showPauseDialog = true
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1D38))
                        .border(1.5.dp, Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Turn Indicator Pill with Gold Border and Crown
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
                    Text(
                        text = "👑",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Turn ${gameState.turnCount} • ${currentPlayer.name}",
                        style = Typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentPrimary
                    )
                    if (currentPlayer.isBot) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = StarGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Sound Toggle with Gold Outer Rim
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
                        .border(1.5.dp, Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)), CircleShape)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Sound",
                        tint = if (isSoundMuted) TextMuted else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Top Player Panels with Individual 3D Dice (Red Top-Left, Green Top-Right)
            PlayerDiceRow(
                players = topPlayers,
                currentPlayer = currentPlayer,
                gameState = gameState,
                playerLastDiceValue = playerLastDiceValue,
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
                            onRollStarted = { rolled ->
                                playerLastDiceValue[player.id] = rolled
                            }
                        )
                    }
                },
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 3. Central Ludo Board (Main visual focus, perfectly square)
            LudoBoardView(
                players = gameState.players,
                currentPlayer = currentPlayer,
                movableTokenIds = if (isHopAnimating || currentPlayer.isBot) emptySet() else gameState.movableTokenIds,
                onTokenClick = onSelectPawn,
                animatingTokenId = animatingTokenId,
                animatingTokenCoord = animatingTokenCoord,
                animatingHopElevationPx = animatingHopElevationPx,
                modifier = Modifier.weight(1f, fill = false)
            )

            // 4. Bottom Player Panels with Individual 3D Dice (Blue Bottom-Left, Yellow Bottom-Right)
            PlayerDiceRow(
                players = bottomPlayers,
                currentPlayer = currentPlayer,
                gameState = gameState,
                playerLastDiceValue = playerLastDiceValue,
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
                            onRollStarted = { rolled ->
                                playerLastDiceValue[player.id] = rolled
                            }
                        )
                    }
                },
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 5. Dominant Bottom Turn & Dice Action Bar (Reference Image 1)
            val canRollCurrent = gameState.isWaitingForRoll && !gameState.isDiceRolling && !currentPlayer.isBot && !isHopAnimating
            val actionTitle = "${currentPlayer.name}'s turn"
            val actionSubtitle = when {
                gameState.isGameOver -> "${gameState.winner?.name ?: currentPlayer.name} won!"
                gameState.isDiceRolling -> "Rolling the dice..."
                isHopAnimating -> "Moving pawn..."
                gameState.isWaitingForRoll && currentPlayer.isBot -> "AI thinking..."
                gameState.isWaitingForRoll -> "Roll the dice!"
                gameState.movableTokenIds.isEmpty() -> "No valid moves (Rolled ${gameState.diceValue})"
                gameState.movableTokenIds.size > 1 -> "Rolled ${gameState.diceValue} • Select a pawn"
                gameState.diceValue == 6 -> "Rolled 6! Extra turn!"
                else -> "Rolled ${gameState.diceValue} • Moving..."
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF080F22))
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFE078),
                                Color(0xFFF5C928),
                                Color(0xFF8A6A12),
                                Color(0xFFF5C928),
                                Color(0xFFFFE078)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = if (canRollCurrent) ripple(bounded = true, color = AccentPrimary) else null,
                        enabled = canRollCurrent,
                        onClick = {
                            handleRoll(
                                currentState = gameState,
                                soundManager = soundManager,
                                coroutineScope = coroutineScope,
                                onStateUpdated = onStateUpdated,
                                onRollStarted = { rolled ->
                                    playerLastDiceValue[currentPlayer.id] = rolled
                                }
                            )
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("roll_dice_bottom_bar"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left: Big 3D Dice on glowing gold pedestal
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AccentPrimary.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.5.dp, AccentPrimary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Realistic3DDiceView(
                            value = if (gameState.diceValue in 1..6) gameState.diceValue else (playerLastDiceValue[currentPlayer.id] ?: 1),
                            isRolling = gameState.isDiceRolling,
                            enabled = canRollCurrent,
                            size = 44.dp,
                            accentColor = currentPlayer.color.primaryColor,
                            onBounce = { soundManager.playDiceRoll() },
                            onRollClick = {
                                if (canRollCurrent) {
                                    handleRoll(
                                        currentState = gameState,
                                        soundManager = soundManager,
                                        coroutineScope = coroutineScope,
                                        onStateUpdated = onStateUpdated,
                                        onRollStarted = { rolled ->
                                            playerLastDiceValue[currentPlayer.id] = rolled
                                        }
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = actionTitle,
                            style = Typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = actionSubtitle,
                            style = Typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            ),
                            color = if (gameState.isWaitingForRoll) AccentPrimary else GoldHighlight
                        )
                    }

                    if (gameState.movableTokenIds.size > 1 && !showPawnSelectionSheet && !isHopAnimating && !currentPlayer.isBot) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentPrimary)
                                .clickable { showPawnSelectionSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Choose",
                                style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF080F22)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
        }

        // Pause Menu Dialog
        if (showPauseDialog) {
            Dialog(onDismissRequest = { showPauseDialog = false }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePanel)
                        .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Game Paused",
                            style = Typography.headlineSmall,
                            color = TextPrimary
                        )

                        Text(
                            text = "Match is saved automatically.",
                            style = Typography.bodyMedium,
                            color = TextMuted
                        )

                        // Audio Quick Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                                    .clickable {
                                        isSoundMuted = !isSoundMuted
                                        soundManager.isSoundEnabled = !isSoundMuted
                                        soundManager.playButtonTap()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
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
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Resume button
                        Button(
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = SurfaceBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resume", style = Typography.labelLarge)
                        }

                        // Restart button
                        OutlinedButton(
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                                onRestartGame()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderHairline)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restart Match", style = Typography.labelLarge)
                        }

                        // Exit to Main Menu
                        OutlinedButton(
                            onClick = {
                                soundManager.playButtonTap()
                                showPauseDialog = false
                                onExitToMenu()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(BorderHairline)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Main Menu", style = Typography.labelLarge)
                        }
                    }
                }
            }
        }

        // 6. Intelligent Pawn Selection Bottom Sheet
        PawnSelectionBottomSheet(
            visible = showPawnSelectionSheet,
            player = currentPlayer,
            diceValue = gameState.diceValue,
            eligibleTokenIds = gameState.movableTokenIds,
            onSelectToken = onSelectPawn,
            onDismiss = { showPawnSelectionSheet = false }
        )
    }
}

@Composable
private fun PlayerDiceRow(
    players: List<Player>,
    currentPlayer: Player,
    gameState: GameState,
    playerLastDiceValue: Map<Int, Int>,
    isHopAnimating: Boolean,
    isTop: Boolean = true,
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
            // In 2-panel rows, mirror dice towards outer screen edges adjacent to home yards
            val alignDiceRight = if (isSingle) true else (index % 2 == 1)
            val canRoll = isActive &&
                gameState.isWaitingForRoll &&
                !gameState.isDiceRolling &&
                !player.isBot &&
                !isHopAnimating
            val diceVal = if (isActive && gameState.diceValue in 1..6) {
                gameState.diceValue
            } else {
                playerLastDiceValue[player.id] ?: 1
            }

            val turnHint = when {
                !isActive -> null
                gameState.isDiceRolling -> "ROLLING"
                canRoll -> "TAP DICE"
                player.isBot -> "AI THINKING"
                gameState.movableTokenIds.isNotEmpty() -> "SELECT PAWN"
                else -> null
            }

            PlayerDicePanel(
                player = player,
                isActive = isActive,
                diceValue = diceVal,
                isRolling = isActive && gameState.isDiceRolling,
                canRoll = canRoll,
                turnStatusHint = turnHint,
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

    // Roll animation start
    soundManager.playDiceRoll()
    val rollingState = currentState.copy(
        isDiceRolling = true,
        isWaitingForRoll = false,
        diceValue = rolledValue
    )
    onStateUpdated(rollingState)

    // After roll completes (520ms matching 3D tumble-and-bounce physics)
    coroutineScope.launch {
        delay(540)

        // 3 consecutive sixes anti-monopoly penalty
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
            rolledValue == 6 -> "${currPlayer.name} rolled 6! Extra turn!"
            else -> "${currPlayer.name} rolled $rolledValue. Select a pawn."
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

/**
 * Animates a pawn hop-by-hop along its path with vertical elevation arcs and synchronized step taps.
 */
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
        // Direct move if no intermediate path
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
        // Hop step-by-step
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

            // Clack sound on landing each hop
            soundManager.playTokenStep()
        }

        // Clear animation
        setAnimatingToken(null, null, 0f)
        setHopAnimating(false)

        // Finalize engine state
        val (nextState, outcome) = LudoGameEngine.executeMove(currentState, tokenId)
        when (outcome) {
            is MoveOutcome.Moved -> {
                if (outcome.capturedPlayerColor != null) {
                    soundManager.playCapture()
                } else if (outcome.tokenFinished) {
                    soundManager.playTokenHome()
                }
            }
            else -> {}
        }
        onStateUpdated(nextState)
    }
}
