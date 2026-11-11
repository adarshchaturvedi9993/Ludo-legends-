package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ads.NativeAdCard
import com.example.ludo.ads.RemoteConfigManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameDialog
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.StatChip
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Dedicated Tournament Statistics Screen:
 * - Match telemetry (matches played, won, win-rate %)
 * - Win-Rate progress visualization bar
 * - In-game achievements & counters (tokens captured, sixes rolled)
 * - Safe reset dialog
 */
@Composable
fun StatsScreen(
    gamePreferences: GamePreferences?,
    onBackClick: () -> Unit,
    onWatchRewardAd: (() -> Unit)? = null
) {
    var totalMatches by remember { mutableIntStateOf(gamePreferences?.totalMatches ?: 0) }
    var totalWins by remember { mutableIntStateOf(gamePreferences?.totalWins ?: 0) }
    var totalCaptures by remember { mutableIntStateOf(gamePreferences?.totalCaptures ?: 0) }
    var totalSixes by remember { mutableIntStateOf(gamePreferences?.totalSixes ?: 0) }
    var showResetDialog by remember { mutableStateOf(false) }

    val winRate = if (totalMatches > 0) ((totalWins.toDouble() / totalMatches) * 100).toInt() else 0

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("stats_screen")
        ) {
            GameTopBar(
                title = "Player Statistics",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Hero Win-Rate Card with Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.2.dp, AccentPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CAREER PERFORMANCE",
                                style = Typography.labelLarge.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 1.0.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AccentPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$winRate%",
                            style = Typography.displayLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "OVERALL WIN RATE",
                            style = Typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(winRate / 100f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentPrimary)
                            )
                        }
                    }
                }

                // 2. Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Matches",
                        value = totalMatches.toString()
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Victories",
                        value = totalWins.toString(),
                        isAccent = true
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        label = "Defeats",
                        value = (totalMatches - totalWins).coerceAtLeast(0).toString()
                    )
                }

                // 3. In-Game Feats
                Text(
                    text = "CAREER FEATS",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.0.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.MilitaryTech,
                        label = "Pawns Captured",
                        value = totalCaptures.toString()
                    )
                    FeatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.FlashOn,
                        label = "Sixes Rolled",
                        value = totalSixes.toString()
                    )
                }

                // Optional Rewarded Vault Bonus
                if (onWatchRewardAd != null && RemoteConfigManager.isRewardedRerollEnabled()) {
                    val currentPoints = gamePreferences?.bonusPoints ?: 0
                    val currentRerolls = gamePreferences?.luckyRerollsCount ?: 0

                    Text(
                        text = "BONUS PERKS SUPPLY",
                        style = Typography.labelLarge.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.0.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentPrimary,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Stars,
                                    contentDescription = null,
                                    tint = StarGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FREE BONUS SUPPLY",
                                    style = Typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = StarGold
                                )
                                Text(
                                    text = "+500 XP & 1 Reroll ($currentPoints XP, $currentRerolls Rerolls)",
                                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentPrimary)
                                .clickable { onWatchRewardAd() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF090E1A),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Claim",
                                    style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF090E1A)
                                )
                            }
                        }
                    }
                }

                // Native Sponsored Ad Card for Stats & Profile
                NativeAdCard(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_NATIVE_STATS
                )

                // Reset Button
                GameButton(
                    text = "Reset All Stats",
                    onClick = { showResetDialog = true },
                    style = GameButtonStyle.SECONDARY_GLASS,
                    icon = Icons.Outlined.DeleteOutline,
                    height = 46.dp,
                    testTag = "reset_stats_button"
                )

                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_STATS,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = SurfaceCard,
                    containerBorderColor = BorderHairline
                )

                AppFooter(modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
            }
        }

        // Reset Confirmation Dialog
        GameDialog(
            visible = showResetDialog,
            title = "Reset Statistics?",
            onDismissRequest = { showResetDialog = false },
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameButton(
                        text = "Yes, Clear Everything",
                        onClick = {
                            gamePreferences?.resetStatistics()
                            totalMatches = 0
                            totalWins = 0
                            totalCaptures = 0
                            totalSixes = 0
                            showResetDialog = false
                        },
                        style = GameButtonStyle.DANGER
                    )
                    GameButton(
                        text = "Cancel",
                        onClick = { showResetDialog = false },
                        style = GameButtonStyle.SECONDARY_GLASS
                    )
                }
            }
        ) {
            Text(
                text = "This will permanently clear your match records and achievement counts.",
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                color = TextMuted
            )
        }
    }
}
