package com.example.ludo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.MotionSpec
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.Typography

enum class GameButtonStyle {
    PRIMARY_GOLD,
    SECONDARY_GLASS,
    DANGER
}

/**
 * Premium tournament game button with press-scale feedback (0.96),
 * metallic gold gradient or frosted glass styling, and haptic integration.
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GameButtonStyle = GameButtonStyle.PRIMARY_GOLD,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    subtitle: String? = null,
    height: Dp = 56.dp,
    enabled: Boolean = true,
    testTag: String = "game_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = MotionSpec.springBouncy(),
        label = "btnScale"
    )

    val shape = RoundedCornerShape(20.dp)

    val (backgroundBrush, borderBrush, textColor, iconColor) = when (style) {
        GameButtonStyle.PRIMARY_GOLD -> {
            val bg = if (enabled) {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFDF60),
                        Color(0xFFF5B612),
                        Color(0xFFE5A812)
                    )
                )
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF4A4430), Color(0xFF383320)))
            }
            val border = if (enabled) {
                Brush.horizontalGradient(listOf(Color(0xFFFFF7C0), Color(0xFFF5C928)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFF6B654E), Color(0xFF524C36)))
            }
            val txt = if (enabled) Color(0xFF080F22) else Color(0xFF9E9882)
            Tuple4(bg, border, txt, txt)
        }
        GameButtonStyle.SECONDARY_GLASS -> {
            val bg = Brush.verticalGradient(
                listOf(
                    Color(0xFF132242),
                    Color(0xFF0D172E)
                )
            )
            val border = Brush.linearGradient(
                listOf(
                    if (isPressed) AccentPrimary else Color(0xFF3F5886),
                    BorderHairline
                )
            )
            Tuple4(bg, border, Color.White, AccentPrimary)
        }
        GameButtonStyle.DANGER -> {
            val bg = Brush.verticalGradient(
                listOf(
                    Color(0xFF3A1218),
                    Color(0xFF220A0E)
                )
            )
            val border = Brush.linearGradient(
                listOf(
                    StatusDanger,
                    StatusDanger.copy(alpha = 0.5f)
                )
            )
            Tuple4(bg, border, Color.White, StatusDanger)
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 1.5.dp, brush = borderBrush, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = if (style == GameButtonStyle.PRIMARY_GOLD) Color.Black else AccentPrimary),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text.uppercase(),
                    style = Typography.titleLarge.copy(
                        fontSize = if (subtitle != null) 16.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    ),
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = Typography.bodyMedium.copy(fontSize = 11.sp),
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
