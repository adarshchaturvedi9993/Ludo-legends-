package com.example.ludo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
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
 * Clean, modern Player Turn Card & Dice HUD:
 * - Active player panel highlights with team color border and turn badge.
 * - Inactive player panels dimmed to 55% for instant visual focus.
 * - 4 token state pips showing Yard, Track, and Home progress.
 * - Integrated circular turn timer ring.
 * - Uncluttered, distraction-free layout.
 */
@Composable
fun PlayerDicePanel(
    player: Player,
    isActive: Boolean,
    diceValue: Int,
    isRolling: Boolean,
    canRoll: Boolean,
    turnStatusHint: String? = null,
    turnTimerProgress: Float? = null,
    alignDiceRight: Boolean = true,
    isTopPanel: Boolean = true,
    onBounce: () -> Unit = {},
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) player.color.primaryColor else BorderHairline,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "panelBorder"
    )

    val panelAlpha by animateFloatAsState(
        targetValue = if (isActive) 1.0f else 0.55f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "panelDim"
    )

    val isInteractive = isActive && canRoll

    Box(
        modifier = modifier
            .alpha(panelAlpha)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) SurfaceElevated else SurfaceCard)
            .border(
                width = if (isActive) 1.6.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = if (isInteractive) ripple(bounded = true) else null,
                enabled = isInteractive,
                onClick = onRollClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("player_panel_${player.color.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Dice on left
            if (!alignDiceRight) {
                Box(contentAlignment = Alignment.Center) {
                    Realistic3DDiceView(
                        value = diceValue,
                        isRolling = isRolling,
                        enabled = isInteractive,
                        size = 40.dp,
                        accentColor = player.color.primaryColor,
                        onBounce = onBounce,
                        onRollClick = onRollClick,
                        modifier = Modifier.testTag("dice_${player.color.name.lowercase()}")
                    )
                    if (isActive && turnTimerProgress != null) {
                        Canvas(modifier = Modifier.size(46.dp)) {
                            drawArc(
                                color = AccentPrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * turnTimerProgress,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Avatar + Name + Turn Badge + 4 Token Progress Dots
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TurnedPawn(
                    color = player.color,
                    isMovable = false,
                    isFinished = false,
                    size = 28.dp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    // Name & AI Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            style = Typography.titleMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isActive) TextPrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isBot) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI",
                                tint = StarGold,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 4 Token Status Pips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        player.tokens.forEach { token ->
                            when (token.state) {
                                TokenState.FINISHED -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StarGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF090E1A),
                                            modifier = Modifier.size(6.dp)
                                        )
                                    }
                                }
                                TokenState.TRACK, TokenState.HOME_STRETCH -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(player.color.primaryColor)
                                            .border(0.8.dp, player.color.accentColor, CircleShape)
                                    )
                                }
                                TokenState.YARD -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(player.color.primaryColor.copy(alpha = 0.35f))
                                            .border(0.6.dp, player.color.darkShade, CircleShape)
                                    )
                                }
                            }
                        }

                        // Turn status badge if active
                        if (isActive && turnStatusHint != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(player.color.primaryColor.copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = turnStatusHint,
                                    style = Typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = player.color.primaryColor
                                )
                            }
                        }
                    }
                }
            }

            // Dice on right
            if (alignDiceRight) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(contentAlignment = Alignment.Center) {
                    Realistic3DDiceView(
                        value = diceValue,
                        isRolling = isRolling,
                        enabled = isInteractive,
                        size = 40.dp,
                        accentColor = player.color.primaryColor,
                        onBounce = onBounce,
                        onRollClick = onRollClick,
                        modifier = Modifier.testTag("dice_${player.color.name.lowercase()}")
                    )
                    if (isActive && turnTimerProgress != null) {
                        Canvas(modifier = Modifier.size(46.dp)) {
                            drawArc(
                                color = AccentPrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * turnTimerProgress,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
