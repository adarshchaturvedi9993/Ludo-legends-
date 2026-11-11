package com.example.ludo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.DailyClaimResult
import com.example.ludo.model.DailyRewardStatus
import com.example.ludo.model.DailyRewardSystem
import com.example.ludo.model.DailyRewardTier
import com.example.ludo.model.MysteryReward
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardDialog(
    status: DailyRewardStatus,
    onClaimClick: () -> Unit,
    onRecoverStreakClick: () -> Unit,
    onDismiss: () -> Unit,
    lastClaimResult: DailyClaimResult? = null,
    onAcknowledgeMysteryResult: () -> Unit = {}
) {
    var remainingMs by remember(status.timeRemainingMs) { mutableLongStateOf(status.timeRemainingMs) }

    // Live countdown ticker
    LaunchedEffect(status.timeRemainingMs) {
        remainingMs = status.timeRemainingMs
        while (remainingMs > 0) {
            delay(1000L)
            remainingMs = (remainingMs - 1000L).coerceAtLeast(0L)
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C1322))
            .border(1.2.dp, BorderHairline, RoundedCornerShape(20.dp))
            .testTag("daily_reward_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY LOGIN REWARDS",
                        style = Typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        ),
                        color = StarGold
                    )
                    Text(
                        text = "7-Day Consecutive Streak Cycle",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, BorderHairline, CircleShape)
                        .clickable { onDismiss() }
                        .testTag("close_daily_reward_dialog"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mystery Box reveal modal if just claimed on Day 7
            if (lastClaimResult?.mysteryReward != null) {
                MysteryRewardRevealCard(
                    reward = lastClaimResult.mysteryReward,
                    onAcknowledge = onAcknowledgeMysteryResult
                )
            } else {
                // Streak Warning if at risk
                if (status.isStreakAtRisk) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusWarning.copy(alpha = 0.12f))
                            .border(1.dp, StatusWarning.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = StatusWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Streak At Risk!",
                                    style = Typography.titleSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = StatusWarning
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You missed yesterday's claim. Watch a video ad to preserve Day ${status.currentStreakDay} streak, or it resets to Day 1.",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusWarning)
                                    .clickable { onRecoverStreakClick() }
                                    .testTag("recover_streak_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF1E1005),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Watch Ad & Save Streak",
                                        style = Typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E1005)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 7 Days Grid: Top row (Days 1 to 4), Bottom row (Days 5 to 7)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Days 1, 2, 3, 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 1..4) {
                            val tier = DailyRewardSystem.getTierForDay(day)
                            DayRewardCard(
                                tier = tier,
                                currentStreak = status.currentStreakDay,
                                canClaim = status.canClaim,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Row 2: Days 5, 6, 7 (Day 7 wider mystery box)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 5..6) {
                            val tier = DailyRewardSystem.getTierForDay(day)
                            DayRewardCard(
                                tier = tier,
                                currentStreak = status.currentStreakDay,
                                canClaim = status.canClaim,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Day 7 Mystery Box card (double width)
                        val tier7 = DailyRewardSystem.getTierForDay(7)
                        DayRewardCard(
                            tier = tier7,
                            currentStreak = status.currentStreakDay,
                            canClaim = status.canClaim,
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action: Claim or Countdown
                if (status.canClaim) {
                    val currentTier = DailyRewardSystem.getTierForDay(status.currentStreakDay)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StarGold)
                            .clickable { onClaimClick() }
                            .testTag("claim_daily_reward_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentTier.isMysteryBox) Icons.Default.CardGiftcard else Icons.Outlined.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFF101626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentTier.isMysteryBox) "Open Day 7 Mystery Box!" else "Claim Day ${status.currentStreakDay} (+${currentTier.coinReward} Coins)",
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF101626)
                            )
                        }
                    }
                } else if (!status.isStreakAtRisk) {
                    // Already claimed today countdown
                    val hours = remainingMs / (3600 * 1000L)
                    val minutes = (remainingMs % (3600 * 1000L)) / (60 * 1000L)
                    val seconds = (remainingMs % (60 * 1000L)) / 1000L
                    val timeStr = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next reward available in: ",
                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = timeStr,
                                style = Typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = StarGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRewardCard(
    tier: DailyRewardTier,
    currentStreak: Int,
    canClaim: Boolean,
    modifier: Modifier = Modifier
) {
    val isPast = tier.dayNumber < currentStreak
    val isCurrent = tier.dayNumber == currentStreak
    val isLocked = tier.dayNumber > currentStreak

    val infiniteTransition = rememberInfiniteTransition(label = "goldGlow")
    val glowAlpha = if (isCurrent && canClaim) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "borderGlow"
        ).value
    } else 0f

    val borderColor = when {
        isCurrent && canClaim -> StarGold.copy(alpha = glowAlpha)
        isCurrent -> StarGold.copy(alpha = 0.6f)
        isPast -> StatusSuccess.copy(alpha = 0.4f)
        else -> BorderHairline
    }

    val cardBg = when {
        isCurrent && canClaim -> Color(0xFF1E1A0C)
        isCurrent -> SurfaceElevated
        isPast -> Color(0xFF0F1A17)
        else -> SurfaceCard
    }

    Box(
        modifier = modifier
            .height(if (tier.isMysteryBox) 78.dp else 78.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(if (isCurrent && canClaim) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Day label
            Text(
                text = tier.label,
                style = Typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = if (isCurrent) StarGold else if (isPast) StatusSuccess else TextMuted
            )

            // Center Icon / Visual
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (tier.isMysteryBox) StarGold.copy(alpha = 0.2f)
                        else if (isPast) StatusSuccess.copy(alpha = 0.2f)
                        else SurfaceElevated
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isPast -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Claimed",
                            tint = StatusSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    tier.isMysteryBox -> {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Mystery Box",
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    isLocked -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Outlined.MonetizationOn,
                            contentDescription = "Coins",
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Reward Amount Text
            Text(
                text = if (tier.isMysteryBox) "Mystery Box" else "+${tier.coinReward}",
                style = Typography.labelSmall.copy(
                    fontSize = if (tier.isMysteryBox) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isPast) TextMuted else if (isCurrent) TextPrimary else TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MysteryRewardRevealCard(
    reward: MysteryReward,
    onAcknowledge: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1F180B))
            .border(1.5.dp, StarGold, RoundedCornerShape(16.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(StarGold.copy(alpha = 0.2f))
                    .border(2.dp, StarGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "DAY 7 REWARD UNLOCKED!",
                style = Typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                ),
                color = StarGold
            )

            Text(
                text = reward.title,
                style = Typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = reward.description,
                style = Typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarGold)
                    .clickable { onAcknowledge() }
                    .testTag("mystery_box_claim_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awesome!",
                    style = Typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101626)
                    )
                )
            }
        }
    }
}
