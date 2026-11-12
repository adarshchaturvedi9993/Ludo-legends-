package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.BannerAdView
import com.example.ludo.model.GameState
import com.example.ludo.model.Player
import com.example.ludo.ui.components.ConfettiEffect
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.Real3DGoldCoin
import com.example.ludo.ui.components.TurnedPawn
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
 * World-Class Tournament Victory Screen:
 * - Dynamic celebratory confetti
 * - Clean gold trophy emblem
 * - Clear tournament winner banner
 * - Final standings roster with pawn avatars and progress
 * - Match telemetry (Total turns, sixes rolled, captures)
 * - Rematch & Main Menu actions
 */
@Composable
fun VictoryScreen(
    gameState: GameState,
    onPlayAgainClick: () -> Unit,
    onMainMenuClick: () -> Unit,
    onClaimDoubleReward: (() -> Unit)? = null,
    isRewardClaimed: Boolean = false
) {
    val winner = gameState.winner ?: gameState.players.first()
    val scrollState = rememberScrollState()

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("victory_screen")
        ) {
            ConfettiEffect()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header / Trophy
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(2.dp, AccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = StarGold,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "VICTORY",
                        style = Typography.displayMedium.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.0.sp
                        ),
                        color = AccentPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${winner.name.uppercase()} WINS",
                        style = Typography.titleLarge.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Text(
                        text = "First contender to guide all 4 pawns home",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Standings & Stats Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.2.dp, AccentPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "FINAL STANDINGS",
                            style = Typography.labelLarge.copy(
                                fontSize = 11.sp,
                                letterSpacing = 1.0.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AccentPrimary
                        )

                        val rankedPlayers = if (gameState.rankings.isNotEmpty()) {
                            gameState.rankings + gameState.players.filter { !gameState.rankings.contains(it) }
                        } else {
                            gameState.players
                        }

                        rankedPlayers.forEachIndexed { index, player ->
                            StandingRow(rank = index + 1, player = player)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatSummaryItem(label = "TURNS", value = gameState.turnCount.toString())
                            StatSummaryItem(label = "SIXES", value = gameState.players.sumOf { it.sixesCount }.toString())
                            StatSummaryItem(label = "CAPTURES", value = gameState.players.sumOf { it.capturesCount }.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Rewarded Ad: Claim 2X Victory Bonus
                if (onClaimDoubleReward != null) {
                    if (isRewardClaimed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, StarGold, RoundedCornerShape(12.dp))
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Real3DGoldCoin(size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "2X VICTORY REWARDS CLAIMED (+50 Coins & Reroll)",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StarGold
                                )
                            }
                        }
                    } else {
                        GameButton(
                            text = "Claim 2X Rewards",
                            subtitle = "Watch a short video to double stars & win XP",
                            onClick = onClaimDoubleReward,
                            style = GameButtonStyle.PRIMARY_GOLD,
                            icon = Icons.Outlined.Stars,
                            testTag = "claim_double_reward_button"
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameButton(
                        text = "Play Again",
                        onClick = onPlayAgainClick,
                        style = GameButtonStyle.PRIMARY_GOLD,
                        icon = Icons.Default.PlayArrow,
                        testTag = "play_again_button"
                    )

                    GameButton(
                        text = "Main Menu",
                        onClick = onMainMenuClick,
                        style = GameButtonStyle.SECONDARY_GLASS,
                        icon = Icons.Outlined.Home,
                        testTag = "victory_main_menu_button"
                    )
                }

                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 10.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_VICTORY,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = SurfaceCard,
                    containerBorderColor = BorderHairline
                )
            }
        }
    }
}

@Composable
private fun StandingRow(rank: Int, player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(1.dp, if (rank == 1) AccentPrimary.copy(alpha = 0.5f) else BorderHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (rank == 1) "#1 👑" else "#$rank",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = if (rank == 1) AccentPrimary else TextMuted,
                modifier = Modifier.width(44.dp)
            )

            TurnedPawn(
                color = player.color,
                isMovable = false,
                isFinished = false,
                size = 24.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = player.name,
                style = Typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (rank == 1) TextPrimary else TextSecondary
            )
        }

        Text(
            text = "${player.finishedTokensCount}/4 Home",
            style = Typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = if (rank == 1) AccentPrimary else TextMuted
        )
    }
}

@Composable
private fun StatSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.8.sp),
            color = TextMuted
        )
    }
}
