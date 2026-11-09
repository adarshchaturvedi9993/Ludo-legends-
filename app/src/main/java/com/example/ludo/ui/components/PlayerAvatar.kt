package com.example.ludo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.PlayerColor
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.GoldHighlight

@Composable
fun PlayerAvatar(
    color: PlayerColor,
    isBot: Boolean = false,
    isActive: Boolean = false,
    isWinner: Boolean = false,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarSpin")
    val sweepRotation = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        ).value
    } else 0f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glowing active sweep ring
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(size + 6.dp)
                    .rotate(sweepRotation)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                color.primaryColor,
                                GoldHighlight,
                                Color.Transparent,
                                color.primaryColor
                            )
                        )
                    )
            )
        }

        // Inner Avatar core
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            color.surfaceColor,
                            Color(0xFF091222)
                        )
                    )
                )
                .border(
                    width = if (isActive) 2.dp else 1.5.dp,
                    color = if (isActive) AccentPrimary else color.primaryColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isBot) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Bot",
                    tint = color.primaryColor,
                    modifier = Modifier.size(size * 0.52f)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Player",
                    tint = color.primaryColor,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        // Winner Crown or Badge
        if (isWinner) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            ) {
                Text(text = "👑", fontSize = (size.value * 0.35f).sp)
            }
        }
    }
}
