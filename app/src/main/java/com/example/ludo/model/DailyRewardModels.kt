package com.example.ludo.model

import kotlin.random.Random

/**
 * Daily Login Reward definitions and Mystery Box roll logic.
 */
enum class MysteryRewardType {
    UNLIMITED_BOTH,
    UNLIMITED_THEMES,
    UNLIMITED_COLORS,
    COINS_100,
    BETTER_LUCK
}

data class MysteryReward(
    val type: MysteryRewardType,
    val title: String,
    val description: String,
    val coinAmount: Int = 0
)

data class DailyRewardTier(
    val dayNumber: Int,
    val coinReward: Int,
    val isMysteryBox: Boolean = false,
    val label: String
)

data class DailyRewardStatus(
    val currentStreakDay: Int,      // 1..7
    val canClaim: Boolean,
    val isStreakAtRisk: Boolean,    // Missed yesterday's claim (> 48h since previous claim)
    val timeRemainingMs: Long,      // Cooldown until next claim can be made (if already claimed today)
    val lastClaimTimestamp: Long,
    val nextRewardAmount: Int
)

data class DailyClaimResult(
    val dayClaimed: Int,
    val coinsEarned: Int,
    val mysteryReward: MysteryReward?,
    val newStreakDay: Int
)

object DailyRewardSystem {
    // 10 coins starting Day 1, scaling 1.25x daily through Day 6
    val REWARD_SCHEDULE = listOf(
        DailyRewardTier(dayNumber = 1, coinReward = 10, isMysteryBox = false, label = "Day 1"),
        DailyRewardTier(dayNumber = 2, coinReward = 12, isMysteryBox = false, label = "Day 2"),
        DailyRewardTier(dayNumber = 3, coinReward = 15, isMysteryBox = false, label = "Day 3"),
        DailyRewardTier(dayNumber = 4, coinReward = 19, isMysteryBox = false, label = "Day 4"),
        DailyRewardTier(dayNumber = 5, coinReward = 24, isMysteryBox = false, label = "Day 5"),
        DailyRewardTier(dayNumber = 6, coinReward = 30, isMysteryBox = false, label = "Day 6"),
        DailyRewardTier(dayNumber = 7, coinReward = 0, isMysteryBox = true, label = "Day 7")
    )

    fun getTierForDay(day: Int): DailyRewardTier {
        val safeDay = day.coerceIn(1, 7)
        return REWARD_SCHEDULE[safeDay - 1]
    }

    /**
     * Exact probabilities requested:
     * - Both Unlimited: 10% (roll 0..9)
     * - Themes Pass: 20% (roll 10..29)
     * - Colors Pass: 20% (roll 30..49)
     * - 100 Coins: 25% (roll 50..74)
     * - Better Luck Next Time: 25% (roll 75..99)
     */
    fun rollMysteryBoxReward(randomSeed: Random = Random.Default): MysteryReward {
        val roll = randomSeed.nextInt(100) // 0..99
        return when {
            roll < 10 -> MysteryReward(
                type = MysteryRewardType.UNLIMITED_BOTH,
                title = "UNLIMITED ALL ACCESS PASS",
                description = "Unlocked all present & future Board Themes AND Pawn Colors forever!"
            )
            roll < 30 -> MysteryReward(
                type = MysteryRewardType.UNLIMITED_THEMES,
                title = "UNLIMITED THEMES PASS",
                description = "Unlocked all 5 exclusive luxury Board Themes forever!"
            )
            roll < 50 -> MysteryReward(
                type = MysteryRewardType.UNLIMITED_COLORS,
                title = "UNLIMITED COLORS PASS",
                description = "Unlocked all 6 premium custom Pawn Color skins forever!"
            )
            roll < 75 -> MysteryReward(
                type = MysteryRewardType.COINS_100,
                title = "100 GOLD COINS",
                description = "Massive fortune of 100 coins credited directly to your treasury!",
                coinAmount = 100
            )
            else -> MysteryReward(
                type = MysteryRewardType.BETTER_LUCK,
                title = "BETTER LUCK NEXT TIME",
                description = "No luck this round! Complete your next 7-day streak for another shot.",
                coinAmount = 0
            )
        }
    }
}
