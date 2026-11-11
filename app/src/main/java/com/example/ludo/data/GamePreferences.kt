package com.example.ludo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.DailyClaimResult
import com.example.ludo.model.DailyRewardStatus
import com.example.ludo.model.DailyRewardSystem
import com.example.ludo.model.GameState
import com.example.ludo.model.MysteryReward
import com.example.ludo.model.MysteryRewardType
import com.example.ludo.model.PawnColorCatalog
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.json.JSONArray
import org.json.JSONObject

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var isAutoMoveSinglePawnEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_MOVE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_MOVE, value).apply()

    var isTurnTimerEnabled: Boolean
        get() = prefs.getBoolean(KEY_TURN_TIMER, true)
        set(value) = prefs.edit().putBoolean(KEY_TURN_TIMER, value).apply()

    var turnTimerSeconds: Int
        get() = prefs.getInt(KEY_TURN_TIMER_SECS, 15)
        set(value) = prefs.edit().putInt(KEY_TURN_TIMER_SECS, value).apply()

    var isColorBlindModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_COLOR_BLIND, false)
        set(value) = prefs.edit().putBoolean(KEY_COLOR_BLIND, value).apply()

    var isReduceMotionEnabled: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()

    // Statistics
    var totalMatches: Int
        get() = prefs.getInt(KEY_STATS_MATCHES, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_MATCHES, value).apply()

    var totalWins: Int
        get() = prefs.getInt(KEY_STATS_WINS, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_WINS, value).apply()

    var totalCaptures: Int
        get() = prefs.getInt(KEY_STATS_CAPTURES, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_CAPTURES, value).apply()

    var totalSixes: Int
        get() = prefs.getInt(KEY_STATS_SIXES, 0)
        set(value) = prefs.edit().putInt(KEY_STATS_SIXES, value).apply()

    var bonusPoints: Int
        get() = prefs.getInt(KEY_BONUS_POINTS, 0)
        set(value) = prefs.edit().putInt(KEY_BONUS_POINTS, value).apply()

    var luckyRerollsCount: Int
        get() = prefs.getInt(KEY_LUCKY_REROLLS, 0)
        set(value) = prefs.edit().putInt(KEY_LUCKY_REROLLS, value).apply()

    fun addBonusReward(points: Int = 500) {
        prefs.edit()
            .putInt(KEY_BONUS_POINTS, bonusPoints + points)
            .putInt(KEY_LUCKY_REROLLS, luckyRerollsCount + 1)
            .apply()
    }

    fun recordMatchFinished(isWinner: Boolean, captures: Int, sixes: Int) {
        prefs.edit()
            .putInt(KEY_STATS_MATCHES, totalMatches + 1)
            .putInt(KEY_STATS_WINS, if (isWinner) totalWins + 1 else totalWins)
            .putInt(KEY_STATS_CAPTURES, totalCaptures + captures.coerceAtLeast(0))
            .putInt(KEY_STATS_SIXES, totalSixes + sixes.coerceAtLeast(0))
            .apply()
    }

    fun recordCapture() {
        prefs.edit().putInt(KEY_STATS_CAPTURES, totalCaptures + 1).apply()
    }

    fun recordSixRolled() {
        prefs.edit().putInt(KEY_STATS_SIXES, totalSixes + 1).apply()
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_STATS_MATCHES, 0)
            .putInt(KEY_STATS_WINS, 0)
            .putInt(KEY_STATS_CAPTURES, 0)
            .putInt(KEY_STATS_SIXES, 0)
            .apply()
    }

    fun getPlayerName(slot: Int, default: String): String {
        return prefs.getString("player_name_$slot", default) ?: default
    }

    fun setPlayerName(slot: Int, name: String) {
        prefs.edit().putString("player_name_$slot", name).apply()
    }

    fun hasSavedGame(): Boolean {
        return prefs.contains(KEY_SAVED_GAME)
    }

    fun clearSavedGame() {
        prefs.edit().remove(KEY_SAVED_GAME).apply()
    }

    fun saveGame(state: GameState) {
        if (state.isGameOver) {
            clearSavedGame()
            return
        }
        try {
            val root = JSONObject()
            root.put("currentPlayerIndex", state.currentPlayerIndex)
            root.put("diceValue", state.diceValue)
            root.put("turnCount", state.turnCount)
            root.put("consecutiveSixes", state.consecutiveSixes)
            root.put("canRollAgain", state.canRollAgain)

            val playersArr = JSONArray()
            for (p in state.players) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                pObj.put("color", p.color.name)
                pObj.put("sixesCount", p.sixesCount)
                pObj.put("isBot", p.isBot)
                pObj.put("botDifficulty", p.botDifficulty.name)
                if (p.rank != null) pObj.put("rank", p.rank)

                val tokensArr = JSONArray()
                for (t in p.tokens) {
                    val tObj = JSONObject()
                    tObj.put("id", t.id)
                    tObj.put("state", t.state.name)
                    tObj.put("stepsTraveled", t.stepsTraveled)
                    tObj.put("homeStep", t.homeStep)
                    tokensArr.put(tObj)
                }
                pObj.put("tokens", tokensArr)
                playersArr.put(pObj)
            }
            root.put("players", playersArr)

            prefs.edit().putString(KEY_SAVED_GAME, root.toString()).apply()
        } catch (_: Exception) {
            // Ignore persistence errors
        }
    }

    fun loadSavedGame(): GameState? {
        val jsonStr = prefs.getString(KEY_SAVED_GAME, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val currentPlayerIndex = root.getInt("currentPlayerIndex")
            val diceValue = root.getInt("diceValue")
            val turnCount = root.optInt("turnCount", 1)
            val consecutiveSixes = root.optInt("consecutiveSixes", 0)
            val canRollAgain = root.optBoolean("canRollAgain", false)

            val playersArr = root.getJSONArray("players")
            val players = mutableListOf<Player>()
            for (i in 0 until playersArr.length()) {
                val pObj = playersArr.getJSONObject(i)
                val id = pObj.getInt("id")
                val name = pObj.getString("name")
                val color = PlayerColor.valueOf(pObj.getString("color"))
                val sixesCount = pObj.optInt("sixesCount", 0)
                val capturesCount = pObj.optInt("capturesCount", 0)
                val rank = if (pObj.has("rank")) pObj.getInt("rank") else null

                val tokensArr = pObj.getJSONArray("tokens")
                val tokens = mutableListOf<Token>()
                for (j in 0 until tokensArr.length()) {
                    val tObj = tokensArr.getJSONObject(j)
                    val tId = tObj.getInt("id")
                    val state = TokenState.valueOf(tObj.getString("state"))
                    val stepsTraveled = tObj.getInt("stepsTraveled")
                    val homeStep = tObj.getInt("homeStep")
                    tokens.add(
                        Token(
                            id = tId,
                            color = color,
                            state = state,
                            stepsTraveled = stepsTraveled,
                            homeStep = homeStep
                        )
                    )
                }
                val isBot = pObj.optBoolean("isBot", false)
                val botDiffStr = pObj.optString("botDifficulty", "MEDIUM")
                val botDifficulty = try { com.example.ludo.model.BotDifficulty.valueOf(botDiffStr) } catch (_: Exception) { com.example.ludo.model.BotDifficulty.MEDIUM }

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
                        botDifficulty = botDifficulty
                    )
                )
            }

            GameState(
                players = players,
                currentPlayerIndex = currentPlayerIndex,
                diceValue = diceValue,
                turnCount = turnCount,
                consecutiveSixes = consecutiveSixes,
                canRollAgain = canRollAgain,
                isWaitingForRoll = true,
                statusMessage = "${players[currentPlayerIndex].name}'s turn to roll"
            )
        } catch (_: Exception) {
            clearSavedGame()
            null
        }
    }

    data class SavedGameSummary(
        val playerCount: Int,
        val turnCount: Int,
        val currentPlayerName: String,
        val currentPlayerColor: PlayerColor,
        val hasBots: Boolean
    )

    fun getSavedGameSummary(): SavedGameSummary? {
        val jsonStr = prefs.getString(KEY_SAVED_GAME, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val turnCount = root.optInt("turnCount", 1)
            val currentPlayerIndex = root.optInt("currentPlayerIndex", 0)
            val playersArr = root.getJSONArray("players")
            val pCount = playersArr.length()
            var currentPName = "Player"
            var currentColor = PlayerColor.RED
            var hasBots = false

            for (i in 0 until pCount) {
                val p = playersArr.getJSONObject(i)
                if (p.optBoolean("isBot", false)) hasBots = true
                if (i == currentPlayerIndex) {
                    currentPName = p.optString("name", "Player")
                    currentColor = try {
                        PlayerColor.valueOf(p.optString("color", "RED"))
                    } catch (_: Exception) {
                        PlayerColor.RED
                    }
                }
            }
            SavedGameSummary(
                playerCount = pCount,
                turnCount = turnCount,
                currentPlayerName = currentPName,
                currentPlayerColor = currentColor,
                hasBots = hasBots
            )
        } catch (_: Exception) {
            null
        }
    }

    // In-game Coin Economy (50 coins start, +50 per rewarded ad, 25/color, 150/theme)
    var coinsBalance: Int
        get() = prefs.getInt(KEY_COINS_BALANCE, 50)
        set(value) = prefs.edit().putInt(KEY_COINS_BALANCE, value.coerceAtLeast(0)).apply()

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        coinsBalance += amount
    }

    fun spendCoins(amount: Int): Boolean {
        if (amount <= 0) return false
        val current = coinsBalance
        if (current < amount) return false
        coinsBalance = current - amount
        return true
    }

    // Unlimited Passes (granted via Day 7 Mystery Box)
    var hasUnlimitedPawnColorsPass: Boolean
        get() = prefs.getBoolean(KEY_PASS_UNLIMITED_COLORS, false)
        set(value) = prefs.edit().putBoolean(KEY_PASS_UNLIMITED_COLORS, value).apply()

    var hasUnlimitedBoardThemesPass: Boolean
        get() = prefs.getBoolean(KEY_PASS_UNLIMITED_THEMES, false)
        set(value) = prefs.edit().putBoolean(KEY_PASS_UNLIMITED_THEMES, value).apply()

    // Pawn Color Unlocks (25 coins each)
    fun getUnlockedPawnColorIds(): Set<String> {
        val defaultSet = setOf("classic_red", "classic_green", "classic_yellow", "classic_blue")
        val saved = prefs.getStringSet(KEY_UNLOCKED_PAWN_COLORS, null) ?: emptySet()
        return defaultSet + saved
    }

    fun isPawnColorUnlocked(skinId: String): Boolean {
        if (hasUnlimitedPawnColorsPass) return true
        val skin = PawnColorCatalog.getSkinById(skinId) ?: return false
        if (skin.isDefaultUnlocked) return true
        return getUnlockedPawnColorIds().contains(skinId)
    }

    fun unlockPawnColor(skinId: String): Boolean {
        if (isPawnColorUnlocked(skinId)) return true
        val skin = PawnColorCatalog.getSkinById(skinId) ?: return false
        if (spendCoins(skin.priceCoins)) {
            val current = getUnlockedPawnColorIds().toMutableSet()
            current.add(skinId)
            prefs.edit().putStringSet(KEY_UNLOCKED_PAWN_COLORS, current).apply()
            return true
        }
        return false
    }

    // Board Theme Customization (150 coins each)
    var selectedBoardThemeId: String
        get() = prefs.getString(KEY_SELECTED_BOARD_THEME, BoardThemeCatalog.THEME_ROYAL_NAVY.id) ?: BoardThemeCatalog.THEME_ROYAL_NAVY.id
        set(value) = prefs.edit().putString(KEY_SELECTED_BOARD_THEME, value).apply()

    fun getUnlockedBoardThemeIds(): Set<String> {
        val defaultSet = setOf(BoardThemeCatalog.THEME_ROYAL_NAVY.id)
        val saved = prefs.getStringSet(KEY_UNLOCKED_BOARD_THEMES, null) ?: emptySet()
        return defaultSet + saved
    }

    fun isBoardThemeUnlocked(themeId: String): Boolean {
        if (hasUnlimitedBoardThemesPass) return true
        val theme = BoardThemeCatalog.getThemeById(themeId)
        if (theme.isDefaultUnlocked) return true
        return getUnlockedBoardThemeIds().contains(themeId)
    }

    fun unlockBoardTheme(themeId: String): Boolean {
        if (isBoardThemeUnlocked(themeId)) return true
        val theme = BoardThemeCatalog.getThemeById(themeId)
        if (spendCoins(theme.priceCoins)) {
            val current = getUnlockedBoardThemeIds().toMutableSet()
            current.add(themeId)
            prefs.edit().putStringSet(KEY_UNLOCKED_BOARD_THEMES, current).apply()
            return true
        }
        return false
    }

    // 7-Day Consecutive Daily Login System
    var dailyStreakDay: Int
        get() = prefs.getInt(KEY_DAILY_STREAK_DAY, 1).coerceIn(1, 7)
        set(value) = prefs.edit().putInt(KEY_DAILY_STREAK_DAY, value.coerceIn(1, 7)).apply()

    var lastDailyClaimTimestamp: Long
        get() = prefs.getLong(KEY_LAST_DAILY_CLAIM, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_DAILY_CLAIM, value).apply()

    fun getDailyRewardStatus(): DailyRewardStatus {
        val now = System.currentTimeMillis()
        val lastClaim = lastDailyClaimTimestamp
        val currentDay = dailyStreakDay
        val tier = DailyRewardSystem.getTierForDay(currentDay)

        if (lastClaim == 0L) {
            return DailyRewardStatus(
                currentStreakDay = currentDay,
                canClaim = true,
                isStreakAtRisk = false,
                timeRemainingMs = 0L,
                lastClaimTimestamp = 0L,
                nextRewardAmount = tier.coinReward
            )
        }

        val elapsed = now - lastClaim
        val claimCooldownMs = 20 * 3600 * 1000L // 20 hours cooldown
        val streakExpiryMs = 48 * 3600 * 1000L  // 48 hours to preserve streak

        return when {
            elapsed < claimCooldownMs -> {
                DailyRewardStatus(
                    currentStreakDay = currentDay,
                    canClaim = false,
                    isStreakAtRisk = false,
                    timeRemainingMs = claimCooldownMs - elapsed,
                    lastClaimTimestamp = lastClaim,
                    nextRewardAmount = tier.coinReward
                )
            }
            elapsed <= streakExpiryMs -> {
                DailyRewardStatus(
                    currentStreakDay = currentDay,
                    canClaim = true,
                    isStreakAtRisk = false,
                    timeRemainingMs = 0L,
                    lastClaimTimestamp = lastClaim,
                    nextRewardAmount = tier.coinReward
                )
            }
            else -> {
                // Missed claim window
                DailyRewardStatus(
                    currentStreakDay = currentDay,
                    canClaim = false,
                    isStreakAtRisk = true,
                    timeRemainingMs = 0L,
                    lastClaimTimestamp = lastClaim,
                    nextRewardAmount = tier.coinReward
                )
            }
        }
    }

    fun claimDailyReward(): DailyClaimResult? {
        val status = getDailyRewardStatus()
        if (!status.canClaim) return null

        val now = System.currentTimeMillis()
        val day = dailyStreakDay
        val tier = DailyRewardSystem.getTierForDay(day)

        var coinsAwarded = 0
        var mysteryReward: MysteryReward? = null

        if (tier.isMysteryBox) {
            // Day 7: roll mystery box
            val mystery = DailyRewardSystem.rollMysteryBoxReward()
            mysteryReward = mystery
            when (mystery.type) {
                MysteryRewardType.UNLIMITED_BOTH -> {
                    hasUnlimitedPawnColorsPass = true
                    hasUnlimitedBoardThemesPass = true
                }
                MysteryRewardType.UNLIMITED_THEMES -> {
                    hasUnlimitedBoardThemesPass = true
                }
                MysteryRewardType.UNLIMITED_COLORS -> {
                    hasUnlimitedPawnColorsPass = true
                }
                MysteryRewardType.COINS_100 -> {
                    coinsAwarded = 100
                    addCoins(100)
                }
                MysteryRewardType.BETTER_LUCK -> {
                    coinsAwarded = 5
                    addCoins(5)
                }
            }
            // Reset streak after Day 7 completion
            dailyStreakDay = 1
        } else {
            // Days 1 through 6
            coinsAwarded = tier.coinReward
            addCoins(coinsAwarded)
            dailyStreakDay = (day + 1).coerceAtMost(7)
        }

        lastDailyClaimTimestamp = now
        return DailyClaimResult(
            dayClaimed = day,
            coinsEarned = coinsAwarded,
            mysteryReward = mysteryReward,
            newStreakDay = dailyStreakDay
        )
    }

    fun recoverStreakWithAd(): Boolean {
        val now = System.currentTimeMillis()
        // Reset timestamp so claim becomes active for today without losing current day's streak
        lastDailyClaimTimestamp = now - (20 * 3600 * 1000L + 1000L)
        return true
    }

    data class WelcomeBonusResult(
        val coinsAwarded: Int = 100,
        val freeSkinUnlocked: String = "ruby_royal",
        val freeSkinName: String = "Ruby Royal",
        val firstMatchDoubleBounty: Boolean = true
    )

    var isWelcomeBonusClaimed: Boolean
        get() = prefs.getBoolean(KEY_WELCOME_BONUS_CLAIMED, false)
        set(value) = prefs.edit().putBoolean(KEY_WELCOME_BONUS_CLAIMED, value).apply()

    var isFirstMatchDoubleActive: Boolean
        get() = prefs.getBoolean(KEY_FIRST_MATCH_DOUBLE_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_MATCH_DOUBLE_ACTIVE, value).apply()

    fun claimWelcomeBonus(): WelcomeBonusResult {
        if (isWelcomeBonusClaimed) {
            return WelcomeBonusResult(coinsAwarded = 0)
        }
        addCoins(100)
        val currentSkins = getUnlockedPawnColorIds().toMutableSet()
        currentSkins.add("ruby_royal")
        prefs.edit()
            .putStringSet(KEY_UNLOCKED_PAWN_COLORS, currentSkins)
            .putBoolean(KEY_WELCOME_BONUS_CLAIMED, true)
            .putBoolean(KEY_FIRST_MATCH_DOUBLE_ACTIVE, true)
            .apply()
        return WelcomeBonusResult()
    }

    fun consumeFirstMatchDoubleIfActive(): Boolean {
        if (isFirstMatchDoubleActive) {
            isFirstMatchDoubleActive = false
            addCoins(100)
            return true
        }
        return false
    }

    companion object {
        private const val KEY_SOUND = "key_sound_enabled"
        private const val KEY_MUSIC = "key_music_enabled"
        private const val KEY_HAPTICS = "key_haptics_enabled"
        private const val KEY_AUTO_MOVE = "key_auto_move_single"
        private const val KEY_TURN_TIMER = "key_turn_timer_enabled"
        private const val KEY_TURN_TIMER_SECS = "key_turn_timer_seconds"
        private const val KEY_COLOR_BLIND = "key_color_blind_mode"
        private const val KEY_REDUCE_MOTION = "key_reduce_motion"
        private const val KEY_SAVED_GAME = "key_saved_game_json"
        private const val KEY_STATS_MATCHES = "key_stats_matches"
        private const val KEY_STATS_WINS = "key_stats_wins"
        private const val KEY_STATS_CAPTURES = "key_stats_captures"
        private const val KEY_STATS_SIXES = "key_stats_sixes"
        private const val KEY_BONUS_POINTS = "key_bonus_points"
        private const val KEY_LUCKY_REROLLS = "key_lucky_rerolls"
        private const val KEY_COINS_BALANCE = "key_coins_balance"
        private const val KEY_UNLOCKED_PAWN_COLORS = "key_unlocked_pawn_colors"
        private const val KEY_UNLOCKED_BOARD_THEMES = "key_unlocked_board_themes"
        private const val KEY_SELECTED_BOARD_THEME = "key_selected_board_theme"
        private const val KEY_PASS_UNLIMITED_COLORS = "key_pass_unlimited_colors"
        private const val KEY_PASS_UNLIMITED_THEMES = "key_pass_unlimited_themes"
        private const val KEY_DAILY_STREAK_DAY = "key_daily_streak_day"
        private const val KEY_LAST_DAILY_CLAIM = "key_last_daily_claim"
        private const val KEY_WELCOME_BONUS_CLAIMED = "key_welcome_bonus_claimed"
        private const val KEY_FIRST_MATCH_DOUBLE_ACTIVE = "key_first_match_double_active"
    }
}
