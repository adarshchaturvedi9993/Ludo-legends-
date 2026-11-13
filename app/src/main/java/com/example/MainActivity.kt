package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ludo.ads.LudoAds
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.screens.BotSetupScreen
import com.example.ludo.ui.screens.ColorSelectionScreen
import com.example.ludo.ui.screens.GameScreen
import com.example.ludo.ui.screens.HomeScreen
import com.example.ludo.ui.screens.HowToPlayScreen
import com.example.ludo.ui.screens.PlayerCountScreen
import com.example.ludo.ui.screens.SettingsScreen
import com.example.ludo.ui.screens.SplashScreen
import com.example.ludo.ui.screens.StatsScreen
import com.example.ludo.ui.screens.VictoryScreen
import androidx.compose.runtime.collectAsState
import com.example.ludo.p2p.P2PGameManager
import com.example.ludo.p2p.P2PEmoteMessage
import com.example.ludo.p2p.P2PTransportType
import com.example.ludo.ui.screens.NearbyMultiplayerScreen
import com.example.ui.theme.LudoTheme
import com.example.ui.theme.SurfaceBackground

enum class Screen {
    SPLASH,
    HOME,
    PLAYER_COUNT,
    COLOR_SELECTION,
    BOT_SETUP,
    NEARBY_MULTIPLAYER,
    GAME,
    VICTORY,
    SETTINGS,
    STATS,
    HOW_TO_PLAY,
    AD_DIAGNOSTICS
}

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var gamePreferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LudoAds.setCurrentActivity(this)
        soundManager = SoundManager(this)
        gamePreferences = GamePreferences(this)
        soundManager.isSoundEnabled = gamePreferences.isSoundEnabled
        soundManager.isMusicEnabled = gamePreferences.isMusicEnabled
        soundManager.isHapticsEnabled = gamePreferences.isHapticsEnabled
        soundManager.startBackgroundMusic()

        setContent {
            LudoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SurfaceBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(SurfaceBackground)
                    ) {
                        LudoAppContent(
                            soundManager = soundManager,
                            gamePreferences = gamePreferences
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LudoAds.setCurrentActivity(this)
        if (::soundManager.isInitialized) {
            soundManager.resumeMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::soundManager.isInitialized) {
            soundManager.pauseMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LudoAds.clearCurrentActivity(this)
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}

@Composable
fun LudoAppContent(
    soundManager: SoundManager,
    gamePreferences: GamePreferences
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
    var selectedPlayerCount by rememberSaveable { mutableIntStateOf(4) }
    var activeGameState by remember { mutableStateOf<GameState?>(null) }
    var hasSavedMatch by remember { mutableStateOf(gamePreferences.hasSavedGame()) }
    var isDoubleRewardClaimed by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    val p2pManager = remember { P2PGameManager.getInstance(context) }
    val p2pSyncedState by p2pManager.syncedGameState.collectAsState()
    val p2pLocalPlayerIndex by p2pManager.localPlayerIndex.collectAsState()
    val p2pTransportType by p2pManager.transportType.collectAsState()
    var isP2PMatchActive by rememberSaveable { mutableStateOf(false) }
    var latestP2PEmote by remember { mutableStateOf<P2PEmoteMessage?>(null) }

    LaunchedEffect(Unit) {
        p2pManager.emotesFlow.collect { emote ->
            latestP2PEmote = emote
        }
    }

    LaunchedEffect(currentScreen) {
        LudoAds.setGameActive(currentScreen == Screen.GAME)
        if (currentScreen == Screen.GAME) {
            soundManager.pauseMusic()
        } else if (gamePreferences.isMusicEnabled) {
            soundManager.resumeMusic()
        }
        if (currentScreen != Screen.VICTORY) {
            isDoubleRewardClaimed = false
        }
    }

    when (currentScreen) {
        Screen.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    currentScreen = Screen.HOME
                    if (activity != null && com.example.ludo.ads.RemoteConfigManager.isAppOpenEnabled()) {
                        LudoAds.appOpenAdManager.showAdIfAvailable(activity)
                    }
                }
            )
        }

        Screen.HOME -> {
            HomeScreen(
                hasSavedGame = hasSavedMatch,
                gamePreferences = gamePreferences,
                onPassAndPlayClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.PLAYER_COUNT
                },
                onPlayWithComputerClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.BOT_SETUP
                },
                onNearbyMultiplayerClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.NEARBY_MULTIPLAYER
                },
                onResumeMatchClick = {
                    soundManager.playButtonTap()
                    val saved = gamePreferences.loadSavedGame()
                    if (saved != null) {
                        activeGameState = saved
                        currentScreen = Screen.GAME
                    } else {
                        hasSavedMatch = false
                    }
                },
                onRulesClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOW_TO_PLAY
                },
                onStatsClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.STATS
                },
                onSettingsClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.SETTINGS
                },
                onWatchRewardAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addCoins(50)
                                gamePreferences.addBonusReward(500)
                            },
                            onAdClosed = {}
                        )
                    }
                },
                onRecoverStreakAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.recoverStreakWithAd()
                            },
                            onAdClosed = {}
                        )
                    }
                }
            )
        }

        Screen.PLAYER_COUNT -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            PlayerCountScreen(
                initialCount = selectedPlayerCount,
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOME
                },
                onContinue = { count ->
                    soundManager.playButtonTap()
                    selectedPlayerCount = count
                    currentScreen = Screen.COLOR_SELECTION
                }
            )
        }

        Screen.COLOR_SELECTION -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.PLAYER_COUNT
            }

            val savedNames = listOf(
                gamePreferences.getPlayerName(0, "Player 1"),
                gamePreferences.getPlayerName(1, "Player 2"),
                gamePreferences.getPlayerName(2, "Player 3"),
                gamePreferences.getPlayerName(3, "Player 4")
            )

            ColorSelectionScreen(
                playerCount = selectedPlayerCount,
                initialNames = savedNames,
                gamePreferences = gamePreferences,
                onWatchRewardAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addCoins(50)
                            },
                            onAdClosed = {}
                        )
                    }
                },
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.PLAYER_COUNT
                },
                onStartGame = { configuredPlayers ->
                    soundManager.playButtonTap()
                    configuredPlayers.forEachIndexed { i, p ->
                        gamePreferences.setPlayerName(i, p.name)
                    }

                    // Order players clockwise around the board
                    val boardOrderedPlayers = configuredPlayers.sortedBy { it.color.startIndex }

                    val initialState = GameState(
                        players = boardOrderedPlayers,
                        currentPlayerIndex = 0,
                        diceValue = 1,
                        isWaitingForRoll = true,
                        statusMessage = "${boardOrderedPlayers[0].name}'s turn to roll"
                    )
                    activeGameState = initialState
                    gamePreferences.saveGame(initialState)
                    hasSavedMatch = true
                    currentScreen = Screen.GAME
                }
            )
        }

        Screen.BOT_SETUP -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            val defaultUserName = gamePreferences.getPlayerName(0, "Player 1")

            BotSetupScreen(
                initialUserName = defaultUserName,
                gamePreferences = gamePreferences,
                onWatchRewardAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addCoins(50)
                            },
                            onAdClosed = {}
                        )
                    }
                },
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOME
                },
                onStartGame = { configuredPlayers ->
                    soundManager.playButtonTap()
                    val human = configuredPlayers.firstOrNull { !it.isBot }
                    if (human != null) {
                        gamePreferences.setPlayerName(0, human.name)
                    }

                    val boardOrderedPlayers = configuredPlayers.sortedBy { it.color.startIndex }

                    val initialState = GameState(
                        players = boardOrderedPlayers,
                        currentPlayerIndex = 0,
                        diceValue = 1,
                        isWaitingForRoll = true,
                        statusMessage = "${boardOrderedPlayers[0].name}'s turn to roll"
                    )
                    activeGameState = initialState
                    gamePreferences.saveGame(initialState)
                    hasSavedMatch = true
                    currentScreen = Screen.GAME
                }
            )
        }

        Screen.NEARBY_MULTIPLAYER -> {
            BackHandler {
                soundManager.playButtonTap()
                p2pManager.leaveGame()
                currentScreen = Screen.HOME
            }

            NearbyMultiplayerScreen(
                gamePreferences = gamePreferences,
                onBackClick = {
                    soundManager.playButtonTap()
                    p2pManager.leaveGame()
                    currentScreen = Screen.HOME
                },
                onGameStarted = {
                    soundManager.playButtonTap()
                    isP2PMatchActive = true
                    currentScreen = Screen.GAME
                }
            )
        }

        Screen.GAME -> {
            val state = if (isP2PMatchActive) {
                p2pSyncedState ?: activeGameState ?: GameState(
                    players = listOf(
                        Player(0, "Player 1", PlayerColor.RED),
                        Player(1, "Player 2", PlayerColor.YELLOW)
                    ),
                    statusMessage = "Player 1's turn to roll"
                )
            } else {
                activeGameState ?: GameState(
                    players = listOf(
                        Player(0, "Player 1", PlayerColor.RED),
                        Player(1, "Player 2", PlayerColor.YELLOW)
                    ),
                    statusMessage = "Player 1's turn to roll"
                )
            }

            GameScreen(
                gameState = state,
                soundManager = soundManager,
                gamePreferences = gamePreferences,
                isP2PMultiplayer = isP2PMatchActive,
                p2pLocalPlayerIndex = p2pLocalPlayerIndex,
                p2pTransportName = if (p2pTransportType == P2PTransportType.WIFI_DIRECT_LAN) "Wi-Fi Direct" else "Bluetooth",
                onP2PRollDice = { p2pManager.rollDice() },
                onP2PSelectPawn = { tokenId -> p2pManager.selectPawn(tokenId) },
                onP2PSendEmote = { emote -> p2pManager.sendQuickEmote(emote) },
                p2pLatestEmote = latestP2PEmote,
                onStateUpdated = { newState ->
                    if (!isP2PMatchActive) {
                        activeGameState = newState
                        gamePreferences.saveGame(newState)
                        hasSavedMatch = !newState.isGameOver
                    }
                },
                onGameFinished = { finalState ->
                    if (isP2PMatchActive) {
                        p2pManager.leaveGame()
                        isP2PMatchActive = false
                    }
                    activeGameState = finalState
                    val winner = finalState.players.firstOrNull { it.rank == 1 }
                    val isHumanWinner = winner != null && !winner.isBot
                    val totalCaptures = finalState.players.sumOf { it.capturesCount }
                    val totalSixes = finalState.players.sumOf { it.sixesCount }
                    gamePreferences.recordMatchFinished(isHumanWinner, totalCaptures, totalSixes)
                    if (isHumanWinner) {
                        if (!gamePreferences.consumeFirstMatchDoubleIfActive()) {
                            gamePreferences.addCoins(25)
                        }
                    }
                    gamePreferences.clearSavedGame()
                    hasSavedMatch = false
                    if (activity != null && com.example.ludo.ads.RemoteConfigManager.isInterstitialMatchEndEnabled()) {
                        LudoAds.showInterstitial(activity) {
                            currentScreen = Screen.VICTORY
                        }
                    } else {
                        currentScreen = Screen.VICTORY
                    }
                },
                onExitToMenu = {
                    soundManager.playButtonTap()
                    if (isP2PMatchActive) {
                        p2pManager.leaveGame()
                        isP2PMatchActive = false
                    }
                    if (activity != null && com.example.ludo.ads.RemoteConfigManager.isInterstitialExitEnabled()) {
                        LudoAds.showInterstitial(activity) {
                            currentScreen = Screen.HOME
                        }
                    } else {
                        currentScreen = Screen.HOME
                    }
                },
                onRestartGame = {
                    soundManager.playButtonTap()
                    if (isP2PMatchActive) {
                        p2pManager.leaveGame()
                        isP2PMatchActive = false
                        currentScreen = Screen.NEARBY_MULTIPLAYER
                    } else {
                        val resetPlayers = state.players.map { p ->
                            Player(
                                id = p.id,
                                name = p.name,
                                color = p.color,
                                isBot = p.isBot,
                                botDifficulty = p.botDifficulty,
                                customSkinId = p.customSkinId
                            )
                        }
                        val resetState = GameState(
                            players = resetPlayers,
                            currentPlayerIndex = 0,
                            diceValue = 1,
                            isWaitingForRoll = true,
                            statusMessage = "${resetPlayers[0].name}'s turn to roll"
                        )
                        activeGameState = resetState
                        gamePreferences.saveGame(resetState)
                        hasSavedMatch = true
                    }
                },
                onWatchRewardAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addCoins(50)
                                gamePreferences.addBonusReward(500)
                            },
                            onAdClosed = {}
                        )
                    }
                }
            )
        }

        Screen.VICTORY -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            val state = activeGameState ?: GameState(
                players = listOf(Player(0, "Player 1", PlayerColor.RED))
            )

            VictoryScreen(
                gameState = state,
                isRewardClaimed = isDoubleRewardClaimed,
                onClaimDoubleReward = {
                    if (activity != null && com.example.ludo.ads.RemoteConfigManager.isRewardedVictoryDoubleEnabled()) {
                        LudoAds.showRewardedInterstitialAd(
                            activity = activity,
                            onRewardEarned = {
                                gamePreferences.addCoins(50)
                                gamePreferences.addBonusReward(1000)
                                isDoubleRewardClaimed = true
                            },
                            onAdClosed = {}
                        )
                    }
                },
                onPlayAgainClick = {
                    soundManager.playButtonTap()
                    val resetPlayers = state.players.map { p ->
                        Player(
                            id = p.id,
                            name = p.name,
                            color = p.color,
                            isBot = p.isBot,
                            botDifficulty = p.botDifficulty,
                            customSkinId = p.customSkinId
                        )
                    }
                    val resetState = GameState(
                        players = resetPlayers,
                        currentPlayerIndex = 0,
                        diceValue = 1,
                        isWaitingForRoll = true,
                        statusMessage = "${resetPlayers[0].name}'s turn to roll"
                    )
                    activeGameState = resetState
                    gamePreferences.saveGame(resetState)
                    hasSavedMatch = true
                    currentScreen = Screen.GAME
                },
                onMainMenuClick = {
                    soundManager.playButtonTap()
                    if (activity != null) {
                        LudoAds.showInterstitial(activity) {
                            currentScreen = Screen.HOME
                        }
                    } else {
                        currentScreen = Screen.HOME
                    }
                }
            )
        }

        Screen.SETTINGS -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            SettingsScreen(
                gamePreferences = gamePreferences,
                soundManager = soundManager,
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOME
                },
                onWatchSupportAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addBonusReward(500)
                            },
                            onAdClosed = {}
                        )
                    }
                },
                onDiagnosticsClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.AD_DIAGNOSTICS
                }
            )
        }

        Screen.STATS -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            StatsScreen(
                gamePreferences = gamePreferences,
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOME
                },
                onWatchRewardAd = {
                    activity?.let { act ->
                        LudoAds.showRewardedAd(
                            activity = act,
                            onRewardEarned = {
                                gamePreferences.addBonusReward(500)
                            },
                            onAdClosed = {}
                        )
                    }
                }
            )
        }

        Screen.HOW_TO_PLAY -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            HowToPlayScreen(
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.AD_DIAGNOSTICS -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.SETTINGS
            }

            com.example.ludo.ui.screens.AdDiagnosticsScreen(
                onBackClick = {
                    soundManager.playButtonTap()
                    currentScreen = Screen.SETTINGS
                }
            )
        }
    }
}
