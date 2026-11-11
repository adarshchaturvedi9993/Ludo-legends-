package com.example.ludo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import com.example.ui.theme.MotionSpec
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.Typography

enum class GameButtonStyle {
    PRIMARY_GOLD,
    SECONDARY_GLASS,
    DANGER
}

/**
 * Premium tournament game button with tactile press-scale feedback,
 * high-contrast color styling, and haptic integration.
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
    height: Dp = 54.dp,
    enabled: Boolean = true,
    testTag: String = "game_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = MotionSpec.springBouncy(),
        label = "btnScale"
    )

    val shape = RoundedCornerShape(14.dp)

    val (bgColor, borderColor, textColor, iconColor) = when (style) {
        GameButtonStyle.PRIMARY_GOLD -> {
            val bg = if (enabled) AccentPrimary else Color(0xFF3D3014)
            val border = if (enabled) Color(0xFFFBBF24) else Color(0xFF52421D)
            val txt = if (enabled) Color(0xFF090E1A) else Color(0xFF8A7752)
            Tuple4(bg, border, txt, txt)
        }
        GameButtonStyle.SECONDARY_GLASS -> {
            val bg = SurfaceElevated
            val border = if (isPressed) AccentPrimary else BorderHairline
            Tuple4(bg, border, TextPrimary, AccentPrimary)
        }
        GameButtonStyle.DANGER -> {
            val bg = Color(0xFF2A1015)
            val border = StatusDanger.copy(alpha = 0.7f)
            Tuple4(bg, border, Color.White, StatusDanger)
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.2.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = if (style == GameButtonStyle.PRIMARY_GOLD) Color.Black else AccentPrimary),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 18.dp)
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = Typography.titleMedium.copy(
                        fontSize = if (subtitle != null) 15.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = Typography.bodyMedium.copy(fontSize = 11.sp),
                        color = textColor.copy(alpha = 0.82f)
                    )
                }
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
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
