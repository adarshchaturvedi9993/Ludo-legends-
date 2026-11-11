package com.example

import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.DailyRewardSystem
import com.example.ludo.model.MysteryRewardType
import com.example.ludo.model.PawnColorCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LudoEconomyAndRewardsTest {

    @Test
    fun testDailyRewardScheduleProgression() {
        // Day 1: 10 coins
        assertEquals(10, DailyRewardSystem.getTierForDay(1).coinReward)
        // Day 2: 12 coins (10 * 1.25 = 12.5 -> 12)
        assertEquals(12, DailyRewardSystem.getTierForDay(2).coinReward)
        // Day 3: 15 coins (12.5 * 1.25 = 15.625 -> 15)
        assertEquals(15, DailyRewardSystem.getTierForDay(3).coinReward)
        // Day 4: 19 coins (15.625 * 1.25 = 19.53 -> 19)
        assertEquals(19, DailyRewardSystem.getTierForDay(4).coinReward)
        // Day 5: 24 coins (19.53 * 1.25 = 24.4 -> 24)
        assertEquals(24, DailyRewardSystem.getTierForDay(5).coinReward)
        // Day 6: 30 coins (24.4 * 1.25 = 30.5 -> 30)
        assertEquals(30, DailyRewardSystem.getTierForDay(6).coinReward)
        // Day 7: Mystery Box
        val day7 = DailyRewardSystem.getTierForDay(7)
        assertTrue("Day 7 must be Mystery Box", day7.isMysteryBox)
    }

    @Test
    fun testDay7MysteryBoxExactDistribution() {
        // Mock random generator to test exact boundaries:
        // 0..9 (10%) -> UNLIMITED_BOTH
        // 10..29 (20%) -> UNLIMITED_THEMES
        // 30..49 (20%) -> UNLIMITED_COLORS
        // 50..74 (25%) -> COINS_100
        // 75..99 (25%) -> BETTER_LUCK

        class DeterministicRandom(private val fixedVal: Int) : Random() {
            override fun nextBits(bitCount: Int): Int = fixedVal
            override fun nextInt(until: Int): Int = fixedVal
        }

        assertEquals(MysteryRewardType.UNLIMITED_BOTH, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(0)).type)
        assertEquals(MysteryRewardType.UNLIMITED_BOTH, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(9)).type)

        assertEquals(MysteryRewardType.UNLIMITED_THEMES, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(10)).type)
        assertEquals(MysteryRewardType.UNLIMITED_THEMES, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(29)).type)

        assertEquals(MysteryRewardType.UNLIMITED_COLORS, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(30)).type)
        assertEquals(MysteryRewardType.UNLIMITED_COLORS, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(49)).type)

        val r50 = DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(50))
        assertEquals(MysteryRewardType.COINS_100, r50.type)
        assertEquals(100, r50.coinAmount)
        val r74 = DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(74))
        assertEquals(MysteryRewardType.COINS_100, r74.type)

        assertEquals(MysteryRewardType.BETTER_LUCK, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(75)).type)
        assertEquals(MysteryRewardType.BETTER_LUCK, DailyRewardSystem.rollMysteryBoxReward(DeterministicRandom(99)).type)
    }

    @Test
    fun testDay7MysteryBoxEmpiricalRolls() {
        var unlimitedBothFound = false
        var unlimitedThemesFound = false
        var unlimitedColorsFound = false
        var coins100Found = false
        var betterLuckFound = false

        for (i in 0 until 500) {
            val reward = DailyRewardSystem.rollMysteryBoxReward()
            assertNotNull(reward)
            when (reward.type) {
                MysteryRewardType.UNLIMITED_BOTH -> unlimitedBothFound = true
                MysteryRewardType.UNLIMITED_THEMES -> unlimitedThemesFound = true
                MysteryRewardType.UNLIMITED_COLORS -> unlimitedColorsFound = true
                MysteryRewardType.COINS_100 -> coins100Found = true
                MysteryRewardType.BETTER_LUCK -> betterLuckFound = true
            }
        }

        assertTrue("Both Unlimited (10%) should roll", unlimitedBothFound)
        assertTrue("Themes Pass (20%) should roll", unlimitedThemesFound)
        assertTrue("Colors Pass (20%) should roll", unlimitedColorsFound)
        assertTrue("100 Coins (25%) should roll", coins100Found)
        assertTrue("Better Luck Next Time (25%) should roll", betterLuckFound)
    }

    @Test
    fun testPawnColorPricingAndCatalog() {
        val premiumColors = PawnColorCatalog.allPremiumColors
        assertEquals("Must have 6 premium pawn colors", 6, premiumColors.size)
        premiumColors.forEach { skin ->
            assertEquals("Each premium pawn color must cost 25 coins", 25, skin.priceCoins)
        }
    }

    @Test
    fun testBoardThemesPricingAndCatalog() {
        val exclusiveThemes = BoardThemeCatalog.allThemes.filter { !it.isDefaultUnlocked }
        assertEquals("Must have 5 exclusive board themes", 5, exclusiveThemes.size)
        exclusiveThemes.forEach { theme ->
            assertEquals("Each exclusive board theme must cost 150 coins", 150, theme.priceCoins)
        }
    }
}
