package com.example.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline

/**
 * Reusable 16dp rounded card with subtle frosted gradient, inner highlight,
 * and 1dp hairline border for tournament clarity.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    isActive: Boolean = false,
    activeBorderColor: Color = AccentPrimary,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF132242),
            Color(0xFF0C162A)
        )
    )

    val borderBrush = if (isActive) {
        Brush.linearGradient(
            listOf(
                activeBorderColor,
                activeBorderColor.copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFF33466B),
                BorderHairline
            )
        )
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true, color = AccentPrimary),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .then(clickableModifier)
            .padding(14.dp),
        content = content
    )
}
