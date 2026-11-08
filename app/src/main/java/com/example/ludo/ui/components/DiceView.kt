package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import kotlin.random.Random

/**
 * Premium tactile 3D die with authentic pips and tumble physics animation.
 */
@Composable
fun DiceView(
    value: Int,
    isRolling: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    accentColor: Color = Color.White,
    onRollClick: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val displayValue = remember { androidx.compose.runtime.mutableIntStateOf(value) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            val rollDuration = 450
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < rollDuration) {
                displayValue.intValue = Random.nextInt(1, 7)
                rotation.snapTo(rotation.value + Random.nextFloat() * 45f)
                kotlinx.coroutines.delay(40)
            }
            displayValue.intValue = value
            rotation.animateTo(
                targetValue = (rotation.value / 360f).toInt() * 360f,
                animationSpec = tween(durationMillis = 150, easing = LinearEasing)
            )
            scale.animateTo(1.15f, tween(60))
            scale.animateTo(1f, tween(90))
        } else {
            displayValue.intValue = value
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .rotate(rotation.value)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(
                width = if (enabled) 2.dp else 1.dp,
                color = if (enabled) accentColor else BorderHairline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2),
                enabled = enabled && !isRolling,
                onClick = onRollClick
            )
            .testTag("dice_view"),
        contentAlignment = Alignment.Center
    ) {
        DiceFaceCanvas(
            value = displayValue.intValue.coerceIn(1, 6),
            size = size
        )
    }
}

@Composable
private fun DiceFaceCanvas(
    value: Int,
    size: Dp
) {
    val pipColor = Color(0xFF0F172A)
    val shadowColor = Color(0xFFCBD5E1)

    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val pipRadius = w * 0.085f

        // Subtle inset bevel
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(w * 0.04f, h * 0.04f),
            size = Size(w * 0.92f, h * 0.92f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.02f)
        )

        val left = w * 0.26f
        val midX = w * 0.50f
        val right = w * 0.74f

        val top = h * 0.26f
        val midY = h * 0.50f
        val bottom = h * 0.74f

        fun drawPip(x: Float, y: Float) {
            // Drop shadow under pip
            drawCircle(
                color = Color(0x33000000),
                radius = pipRadius,
                center = Offset(x, y + 1.5f)
            )
            // Pip body
            drawCircle(
                color = pipColor,
                radius = pipRadius,
                center = Offset(x, y)
            )
        }

        when (value) {
            1 -> {
                drawPip(midX, midY)
            }
            2 -> {
                drawPip(left, top)
                drawPip(right, bottom)
            }
            3 -> {
                drawPip(left, top)
                drawPip(midX, midY)
                drawPip(right, bottom)
            }
            4 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            5 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(midX, midY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
            6 -> {
                drawPip(left, top)
                drawPip(right, top)
                drawPip(left, midY)
                drawPip(right, midY)
                drawPip(left, bottom)
                drawPip(right, bottom)
            }
        }
    }
}
