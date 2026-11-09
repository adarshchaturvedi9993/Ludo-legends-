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
import com.example.ui.theme.LudoTheme
import com.example.ui.theme.SurfaceBackground

enum class Screen {
    SPLASH,
    HOME,
    PLAYER_COUNT,
    COLOR_SELECTION,
    BOT_SETUP,
    GAME,
    VICTORY,
    SETTINGS,
    STATS,
    HOW_TO_PLAY
}

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var gamePreferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)
        gamePreferences = GamePreferences(this)
        soundManager.isSoundEnabled = gamePreferences.isSoundEnabled
        soundManager.isMusicEnabled = gamePreferences.isMusicEnabled
        soundManager.isHapticsEnabled = gamePreferences.isHapticsEnabled
        soundManager.startBackgroundMusic()

        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
        } catch (_: Exception) {
        }

        try {
            com.google.android.gms.ads.MobileAds.initialize(this) {}
        } catch (_: Exception) {
        }

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
        if (::soundManager.isInitialized) {
            soundManager.stopBackgroundMusic()
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

    LaunchedEffect(currentScreen) {
        LudoAds.setGameActive(currentScreen == Screen.GAME)
        if (currentScreen != Screen.VICTORY) {
            isDoubleRewardClaimed = false
        }
    }

    when (currentScreen) {
        Screen.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    currentScreen = Screen.HOME
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
                                gamePreferences.addBonusReward(500)
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

        Screen.GAME -> {
            BackHandler {
                soundManager.playButtonTap()
                currentScreen = Screen.HOME
            }

            val state = activeGameState ?: GameState(
                players = listOf(
                    Player(0, "Player 1", PlayerColor.RED),
                    Player(1, "Player 2", PlayerColor.YELLOW)
                ),
                statusMessage = "Player 1's turn to roll"
            )

            GameScreen(
                gameState = state,
                soundManager = soundManager,
                gamePreferences = gamePreferences,
                onStateUpdated = { newState ->
                    activeGameState = newState
                    gamePreferences.saveGame(newState)
                    hasSavedMatch = !newState.isGameOver
                },
                onGameFinished = { finalState ->
                    activeGameState = finalState
                    val winner = finalState.players.firstOrNull { it.rank == 1 }
                    val isHumanWinner = winner != null && !winner.isBot
                    val totalCaptures = finalState.players.sumOf { it.capturesCount }
                    val totalSixes = finalState.players.sumOf { it.sixesCount }
                    gamePreferences.recordMatchFinished(isHumanWinner, totalCaptures, totalSixes)
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
                    val resetPlayers = state.players.map { p ->
                        Player(
                            id = p.id,
                            name = p.name,
                            color = p.color,
                            isBot = p.isBot,
                            botDifficulty = p.botDifficulty
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
                            botDifficulty = p.botDifficulty
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
    }
}
