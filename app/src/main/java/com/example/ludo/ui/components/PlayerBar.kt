package com.example.ludo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
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
 * Professional Player cards display bar:
 * - Player name & color indicator.
 * - 4 small pawn-status indicators (Yard, On Track / Active, Finished).
 * - Active player highlighted border with smooth turn-switch animation.
 * - AI bot indicator tag.
 */
@Composable
fun PlayerBar(
    players: List<Player>,
    currentPlayer: Player,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (player in players) {
            val isActive = player.id == currentPlayer.id
            PlayerCard(
                player = player,
                isActive = isActive,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) player.color.primaryColor else BorderHairline,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "playerBorder"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) SurfaceElevated else SurfacePanel,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "playerBg"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(if (isActive) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Name Row with Color Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(player.color.primaryColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = player.name,
                style = Typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isActive) TextPrimary else TextSecondary,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (player.isBot) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4 Pawn-Status Indicators:
        // - Hollow ring = In Yard
        // - Colored dot = In Play (Track / Home Corridor)
        // - Gold star = Finished
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (token in player.tokens) {
                when (token.state) {
                    TokenState.FINISHED -> {
                        // Finished goal pawn: miniature gold star
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Finished",
                            tint = StarGold,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                    TokenState.TRACK, TokenState.HOME_STRETCH -> {
                        // In play pawn: solid colored dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(player.color.primaryColor)
                        )
                    }
                    TokenState.YARD -> {
                        // Waiting in yard: hollow ring
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .border(1.dp, BorderActive, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
