package com.example.ludo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Dedicated Player Panel with Individual 3D Dice matching Reference Image 1:
 * - 2.5D TurnedPawn avatar in player's color.
 * - Player name and 4 colored circular status pips.
 * - White square dice showing real engine roll.
 * - Player-color tinted gradient background and glowing border when active.
 * - Active pointer triangle pointing toward the board.
 */
@Composable
fun PlayerDicePanel(
    player: Player,
    isActive: Boolean,
    diceValue: Int,
    isRolling: Boolean,
    canRoll: Boolean,
    turnStatusHint: String? = null,
    alignDiceRight: Boolean = true,
    isTopPanel: Boolean = true,
    onBounce: () -> Unit = {},
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) player.color.primaryColor else BorderHairline,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "panelBorder"
    )

    val isInteractive = isActive && canRoll

    val backgroundBrush = if (isActive) {
        Brush.horizontalGradient(
            colors = listOf(
                player.color.primaryColor.copy(alpha = 0.28f),
                Color(0xFF0D1D38)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0D1D38),
                Color(0xFF091426)
            )
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // If bottom panel and active: pointer points upwards to the board
        if (!isTopPanel && isActive) {
            Canvas(modifier = Modifier.size(14.dp, 7.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = player.color.primaryColor)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundBrush)
                .border(
                    width = if (isActive) 1.8.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = if (isInteractive) ripple(bounded = true) else null,
                    enabled = isInteractive,
                    onClick = onRollClick
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("player_panel_${player.color.name.lowercase()}"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!alignDiceRight) {
                    Realistic3DDiceView(
                        value = diceValue,
                        isRolling = isRolling,
                        enabled = isInteractive,
                        size = 44.dp,
                        accentColor = player.color.primaryColor,
                        onBounce = onBounce,
                        onRollClick = onRollClick,
                        modifier = Modifier.testTag("dice_${player.color.name.lowercase()}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Left: Pawn Avatar + Name + 4 Status Dots
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 2.5D TurnedPawn Miniature Avatar
                    TurnedPawn(
                        color = player.color,
                        isMovable = false,
                        isFinished = false,
                        size = 28.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player.name,
                                style = Typography.titleMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isActive) Color.White else TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (player.isBot) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI",
                                    tint = StarGold,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // 4 Token Status Pips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                                tint = Color.Black,
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
                                                .border(0.8.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        )
                                    }
                                    TokenState.YARD -> {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(player.color.primaryColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (alignDiceRight) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Realistic3DDiceView(
                        value = diceValue,
                        isRolling = isRolling,
                        enabled = isInteractive,
                        size = 44.dp,
                        accentColor = player.color.primaryColor,
                        onBounce = onBounce,
                        onRollClick = onRollClick,
                        modifier = Modifier.testTag("dice_${player.color.name.lowercase()}")
                    )
                }
            }
        }

        // If top panel and active: pointer points downwards to the board
        if (isTopPanel && isActive) {
            Spacer(modifier = Modifier.height(2.dp))
            Canvas(modifier = Modifier.size(14.dp, 7.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                drawPath(path, color = player.color.primaryColor)
            }
        }
    }
}
