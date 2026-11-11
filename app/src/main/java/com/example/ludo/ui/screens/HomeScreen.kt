package com.example.ludo.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.ludo.ads.AdConstants
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ads.NativeAdCard
import com.example.ludo.ads.RemoteConfigManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.DailyClaimResult
import com.example.ludo.model.DailyRewardStatus
import com.example.ludo.ui.components.CoinBalanceChip
import com.example.ludo.ui.components.DailyRewardDialog
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.StatChip
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

import com.example.ludo.ui.components.StarterWelcomeDialog

/**
 * Ludo Legends Home Lobby:
 * - Restored classic styled UI design.
 * - Displays complete full-sized uncropped hero image artwork.
 * - Profile header badge with stats, rules, and settings navigation.
 * - Coin treasury balance and rewarded video ad triggers.
 * - Daily login reward system dialog with 7-day streak track and Day 7 Mystery Box.
 * - Starter Welcome Bounty induction dialog for first-time players (+100 Coins & Ruby Royal).
 * - Saved game resume banner when an in-progress match exists.
 * - Game mode cards: Pass & Play (Local Multiplayer) and Play vs Computer (Solo AI).
 * - Real-time career stat chips (Matches, Victories, Win Rate).
 * - Utility strip for fast access to rules, stats, daily rewards, and ad bonus perks.
 * - Native AdCard and developer credit footer.
 * - Bottom Banner sticked firmly to the bottom of the screen.
 */
@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    gamePreferences: GamePreferences? = null,
    onPassAndPlayClick: () -> Unit,
    onPlayWithComputerClick: () -> Unit,
    onResumeMatchClick: () -> Unit,
    onRulesClick: () -> Unit,
    onStatsClick: () -> Unit = onRulesClick,
    onSettingsClick: () -> Unit,
    onWatchRewardAd: (() -> Unit)? = null,
    onRecoverStreakAd: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val savedSummary = remember(hasSavedGame) {
        if (hasSavedGame) gamePreferences?.getSavedGameSummary() else null
    }

    var userCoins by remember { mutableIntStateOf(gamePreferences?.coinsBalance ?: 0) }
    var dailyRewardStatus by remember {
        mutableStateOf(
            gamePreferences?.getDailyRewardStatus() ?: DailyRewardStatus(
                currentStreakDay = 1,
                canClaim = true,
                isStreakAtRisk = false,
                timeRemainingMs = 0L,
                lastClaimTimestamp = 0L,
                nextRewardAmount = 10
            )
        )
    }
    var showDailyRewardDialog by remember { mutableStateOf(false) }
    var lastClaimResult by remember { mutableStateOf<DailyClaimResult?>(null) }
    var showWelcomeDialog by remember {
        mutableStateOf(gamePreferences != null && !gamePreferences.isWelcomeBonusClaimed)
    }

    // Sync on resume (e.g. after watching a rewarded ad)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                userCoins = gamePreferences?.coinsBalance ?: userCoins
                dailyRewardStatus = gamePreferences?.getDailyRewardStatus() ?: dailyRewardStatus
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Top Header: User Profile Badge + Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderHairline, RoundedCornerShape(24.dp))
                                .clickable { onStatsClick() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profile",
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                val defaultName = gamePreferences?.getPlayerName(0, "Player 1") ?: "Player 1"
                                val wins = gamePreferences?.totalWins ?: 0
                                Text(
                                    text = defaultName,
                                    style = Typography.titleMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$wins Wins • Career Stats",
                                    style = Typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // First-time user welcome bounty chip
                            if (gamePreferences != null && !gamePreferences.isWelcomeBonusClaimed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(StarGold.copy(alpha = 0.18f))
                                        .border(1.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                        .clickable { showWelcomeDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                        .testTag("welcome_bounty_chip"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "GIFT +100",
                                            style = Typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.6.sp
                                            ),
                                            color = StarGold
                                        )
                                    }
                                }
                            }

                            CoinBalanceChip(
                                coins = userCoins,
                                onEarnCoinsClick = onWatchRewardAd
                            )

                            // Daily Login Gift Button with notification indicator
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (dailyRewardStatus.canClaim || dailyRewardStatus.isStreakAtRisk) StarGold.copy(alpha = 0.15f) else SurfaceCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (dailyRewardStatus.canClaim) StarGold else if (dailyRewardStatus.isStreakAtRisk) StatusWarning else BorderHairline,
                                        shape = CircleShape
                                    )
                                    .clickable { showDailyRewardDialog = true }
                                    .testTag("daily_reward_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Daily Rewards",
                                    tint = if (dailyRewardStatus.canClaim) StarGold else if (dailyRewardStatus.isStreakAtRisk) StatusWarning else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (dailyRewardStatus.canClaim || dailyRewardStatus.isStreakAtRisk) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(if (dailyRewardStatus.isStreakAtRisk) StatusWarning else StarGold)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderHairline, CircleShape)
                                    .clickable(onClick = onSettingsClick)
                                    .testTag("settings_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 2. Complete Full Sized Hero Image (Uncropped, Natural Aspect Ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.home_hero_card),
                            contentDescription = "Ludo Legends Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    // 3. Conditional First Match 2X Bounty Banner
                    if (gamePreferences?.isFirstMatchDoubleActive == true) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF19140A))
                                .border(1.2.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(StarGold.copy(alpha = 0.2f))
                                    .border(1.dp, StarGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Stars,
                                    contentDescription = null,
                                    tint = StarGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "FIRST MATCH 2X BOUNTY ACTIVE",
                                    style = Typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = StarGold
                                )
                                Text(
                                    text = "Win your first game to earn 2X bonus (+100 Gold Coins)!",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // 4. Conditional Resume Match Banner
                    if (hasSavedGame) {
                        ResumeMatchBanner(
                            summary = savedSummary,
                            onResumeClick = onResumeMatchClick
                        )
                    }

                    // 4. Primary Game Modes (Pass & Play & Solo AI)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GameModeCard(
                            title = "Pass & Play",
                            subtitle = "Local match on this device (2, 3 or 4 Players)",
                            badgeText = "LOCAL MULTIPLAYER",
                            icon = Icons.Default.Group,
                            isPrimary = true,
                            onClick = onPassAndPlayClick,
                            testTag = "play_now_button"
                        )

                        GameModeCard(
                            title = "Play vs Computer",
                            subtitle = "Challenge intelligent AI bots with custom difficulty",
                            badgeText = "SOLO ARENA",
                            icon = Icons.Default.SmartToy,
                            isPrimary = false,
                            onClick = onPlayWithComputerClick,
                            testTag = "play_with_ai_button"
                        )
                    }

                    // 5. Career Quick Stats Chips
                    if (gamePreferences != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip(
                                label = "Matches",
                                value = "${gamePreferences.totalMatches}",
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = "Victories",
                                value = "${gamePreferences.totalWins}",
                                modifier = Modifier.weight(1f),
                                isAccent = true
                            )
                            val winRate = if (gamePreferences.totalMatches > 0) {
                                (gamePreferences.totalWins * 100) / gamePreferences.totalMatches
                            } else 0
                            StatChip(
                                label = "Win Rate",
                                value = "$winRate%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 6. Utility Strip: Stats, Rules, Daily Rewards, Free Coins
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UtilityButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.BarChart,
                            title = "Stats",
                            onClick = onStatsClick,
                            testTag = "home_stats_card"
                        )

                        UtilityButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Outlined.HelpOutline,
                            title = "Rules",
                            onClick = onRulesClick,
                            testTag = "home_rules_card"
                        )

                        UtilityButton(
                            modifier = Modifier.weight(1.3f),
                            icon = Icons.Default.CardGiftcard,
                            title = "Daily Bonus",
                            badge = if (dailyRewardStatus.canClaim) "CLAIM!" else "Day ${dailyRewardStatus.currentStreakDay}",
                            onClick = { showDailyRewardDialog = true },
                            testTag = "home_daily_card"
                        )

                        if (onWatchRewardAd != null) {
                            UtilityButton(
                                modifier = Modifier.weight(1.3f),
                                icon = Icons.Outlined.Stars,
                                title = "Free Coins",
                                badge = "+50 🪙",
                                onClick = onWatchRewardAd,
                                testTag = "home_bonus_card"
                            )
                        }
                    }

                    // 7. Native Ad Card
                    NativeAdCard(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 8. Bottom Banner Ad - Sticked firmly to the bottom of the screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BannerAdView(
                        modifier = Modifier.fillMaxWidth(),
                        placementKey = AdConstants.KEY_BANNER_HOME,
                        hasCardContainer = true,
                        cornerRadius = 14.dp,
                        containerBackground = SurfaceCard,
                        containerBorderColor = BorderHairline
                    )
                }
            }

            // Daily Reward Dialog Modal
            if (showDailyRewardDialog) {
                DailyRewardDialog(
                    status = dailyRewardStatus,
                    onClaimClick = {
                        val result = gamePreferences?.claimDailyReward()
                        if (result != null) {
                            lastClaimResult = result
                            dailyRewardStatus = gamePreferences.getDailyRewardStatus()
                            userCoins = gamePreferences.coinsBalance
                        }
                    },
                    onRecoverStreakClick = {
                        onRecoverStreakAd?.invoke()
                    },
                    onDismiss = {
                        showDailyRewardDialog = false
                        lastClaimResult = null
                    },
                    lastClaimResult = lastClaimResult,
                    onAcknowledgeMysteryResult = {
                        lastClaimResult = null
                        showDailyRewardDialog = false
                    }
                )
            }

            // Starter Welcome Bounty Dialog for first-time players
            if (showWelcomeDialog) {
                StarterWelcomeDialog(
                    onClaimClick = {
                        val result = gamePreferences?.claimWelcomeBonus()
                        if (result != null) {
                            userCoins = gamePreferences.coinsBalance
                        }
                        showWelcomeDialog = false
                    },
                    onDismiss = {
                        showWelcomeDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPrimary) SurfaceElevated else SurfaceCard)
            .border(
                width = if (isPrimary) 1.5.dp else 1.dp,
                color = if (isPrimary) AccentPrimary else BorderHairline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = AccentPrimary),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPrimary) AccentPrimary.copy(alpha = 0.15f) else SurfaceElevated)
                        .border(
                            1.dp,
                            if (isPrimary) AccentPrimary.copy(alpha = 0.5f) else BorderHairline,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPrimary) AccentPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isPrimary) AccentPrimary.copy(alpha = 0.2f) else SurfaceCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = if (isPrimary) AccentPrimary else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = title,
                        style = Typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Text(
                        text = subtitle,
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isPrimary) AccentPrimary else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ResumeMatchBanner(
    summary: GamePreferences.SavedGameSummary?,
    onResumeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.2.dp, AccentPrimary, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onResumeClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
            .testTag("resume_match_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "MATCH IN PROGRESS",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = AccentPrimary
                )
                val subtitleText = if (summary != null) {
                    "${summary.playerCount} Players • Turn ${summary.turnCount} • ${summary.currentPlayerName}'s turn"
                } else {
                    "Tap to resume your preserved game"
                }
                Text(
                    text = subtitleText,
                    style = Typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPrimary)
                .clickable { onResumeClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF090E1A),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Resume",
                    style = Typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF090E1A)
                )
            }
        }
    }
}

@Composable
private fun UtilityButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = Typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )
        if (badge != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    style = Typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary
                )
            }
        }
    }
}
