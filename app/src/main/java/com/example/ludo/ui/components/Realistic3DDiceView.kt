package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StarGold
import kotlin.math.abs
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Commercial-grade 3D Ivory Dice:
 * - Smooth, rounded physical ivory cube with seamless convex surface shading.
 * - NO harsh white lining, NO protruding trapezoid flaps.
 * - Deep sunken circular black pips with authentic layout (1 to 6).
 * - Soft dynamic contact drop shadow responding to bounce height.
 * - Ultra-smooth tumble-and-bounce roll physics with ease-out landing.
 * - Soft ambient glow aura when it's the player's turn to roll.
 */
@Composable
fun Realistic3DDiceView(
    value: Int,
    isRolling: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    accentColor: Color = StarGold,
    onBounce: () -> Unit = {},
    onRollClick: () -> Unit
) {
    val rotZ = remember { Animatable(0f) }
    val tiltX = remember { Animatable(0f) }
    val bounceY = remember { Animatable(0f) }
    val scalePulse = remember { Animatable(1f) }
    val displayValue = remember { mutableIntStateOf(value) }

    val infiniteTransition = rememberInfiniteTransition(label = "diceGlow")
    val pulseGlow = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dicePulse"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            val rollDuration = 520L
            val startTime = System.currentTimeMillis()

            // Smooth bounce sequence
            bounceY.animateTo(-22f, tween(110, easing = FastOutSlowInEasing))
            onBounce()
            bounceY.animateTo(0f, tween(90, easing = LinearEasing))
            onBounce()

            bounceY.animateTo(-10f, tween(80, easing = FastOutSlowInEasing))
            bounceY.animateTo(0f, tween(70, easing = LinearEasing))
            onBounce()

            bounceY.animateTo(-3f, tween(50))
            bounceY.animateTo(0f, tween(40))
            onBounce()

            // Smooth tumbling rotation during roll
            while (System.currentTimeMillis() - startTime < rollDuration) {
                displayValue.intValue = Random.nextInt(1, 7)
                rotZ.animateTo(
                    targetValue = rotZ.value + Random.nextInt(-40, 40),
                    animationSpec = tween(35, easing = LinearEasing)
                )
                tiltX.snapTo((Random.nextFloat() - 0.5f) * 16f)
                delay(35)
            }

            // Settle smoothly on the final pre-decided result
            displayValue.intValue = value
            rotZ.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            tiltX.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            scalePulse.animateTo(1.1f, tween(60))
            scalePulse.animateTo(1.0f, tween(70))
            bounceY.snapTo(0f)
        } else {
            displayValue.intValue = value
            rotZ.snapTo(0f)
            tiltX.snapTo(0f)
            scalePulse.snapTo(1f)
            bounceY.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scalePulse.value)
            .rotate(rotZ.value)
            .clip(RoundedCornerShape(size * 0.22f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2),
                enabled = enabled && !isRolling,
                onClick = onRollClick
            )
            .testTag("realistic_3d_dice"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawSmooth3DDice(
                value = displayValue.intValue.coerceIn(1, 6),
                bounceY = bounceY.value,
                tiltX = tiltX.value,
                isRolling = isRolling,
                enabled = enabled,
                accentColor = accentColor,
                pulseAlpha = if (enabled && !isRolling) pulseGlow.value else 0f
            )
        }
    }
}

private fun DrawScope.drawSmooth3DDice(
    value: Int,
    bounceY: Float,
    tiltX: Float,
    isRolling: Boolean,
    enabled: Boolean,
    accentColor: Color,
    pulseAlpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.5f + bounceY

    // 1. Soft realistic contact drop shadow beneath the dice
    val shadowW = w * 0.78f * (1f - abs(bounceY) / 50f).coerceIn(0.40f, 1f)
    val shadowH = h * 0.22f * (1f - abs(bounceY) / 50f).coerceIn(0.40f, 1f)
    val shadowAlpha = (0.45f - abs(bounceY) / 70f).coerceIn(0.10f, 0.45f)
    drawOval(
        color = Color(0x77000000).copy(alpha = shadowAlpha),
        topLeft = Offset(cx - shadowW / 2f, h * 0.76f + (bounceY * 0.15f)),
        size = Size(shadowW, shadowH)
    )

    val cubeSize = w * 0.82f
    val half = cubeSize / 2f
    val cubeLeft = cx - half
    val cubeTop = cy - half
    val cornerRadiusPx = cubeSize * 0.20f
    val corner = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    // 2. Luminous Ambient Glow when it's the active player's turn to roll (NO harsh white lines)
    if (enabled && pulseAlpha > 0f && !isRolling) {
        val glowPadding = cubeSize * 0.08f
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = pulseAlpha * 0.55f),
                    accentColor.copy(alpha = pulseAlpha * 0.15f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = cubeSize * 0.72f
            ),
            topLeft = Offset(cubeLeft - glowPadding, cubeTop - glowPadding),
            size = Size(cubeSize + glowPadding * 2, cubeSize + glowPadding * 2),
            cornerRadius = CornerRadius(cornerRadiusPx + glowPadding, cornerRadiusPx + glowPadding)
        )
    }

    // 3. Luxurious Ivory Body with smooth 3D gradient (Clean, smooth, NO white borders)
    // Top-left is warmly lit by ambient light, bottom-right has soft depth
    val ivoryTop = Color(0xFFFFFFFF)
    val ivoryMid = Color(0xFFFBFBFA)
    val ivoryBottom = Color(0xFFEBE8E0)
    val ivoryShadowEdge = Color(0xFFD8D4C8)

    // Base Cube Body with subtle vertical shading
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(ivoryTop, ivoryMid, ivoryBottom),
            startY = cubeTop,
            endY = cubeTop + cubeSize
        ),
        topLeft = Offset(cubeLeft, cubeTop),
        size = Size(cubeSize, cubeSize),
        cornerRadius = corner
    )

    // Soft bevel lighting: inner highlight stroke along top and left, depth on bottom
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.35f),
                ivoryShadowEdge.copy(alpha = 0.60f)
            ),
            start = Offset(cubeLeft, cubeTop),
            end = Offset(cubeLeft + cubeSize, cubeTop + cubeSize)
        ),
        topLeft = Offset(cubeLeft, cubeTop),
        size = Size(cubeSize, cubeSize),
        cornerRadius = corner,
        style = Stroke(width = (cubeSize * 0.035f).coerceAtLeast(1.0f))
    )

    // Subtle player accent tint ring on active dice (ultra-fine 1dp, soft alpha)
    if (enabled) {
        drawRoundRect(
            color = accentColor.copy(alpha = (pulseAlpha * 0.6f).coerceIn(0.25f, 0.7f)),
            topLeft = Offset(cubeLeft + 1f, cubeTop + 1f),
            size = Size(cubeSize - 2f, cubeSize - 2f),
            cornerRadius = CornerRadius(cornerRadiusPx - 1f, cornerRadiusPx - 1f),
            style = Stroke(width = (cubeSize * 0.03f).coerceAtLeast(1.0f))
        )
    }

    // 4. Draw Pure Circular Sunken Black Pips
    drawSmoothPips(
        value = value,
        center = Offset(cx, cy),
        faceSize = cubeSize
    )
}

/**
 * Draws sunken, circular authentic black dice pips with radial gradient and soft specular dot.
 */
private fun DrawScope.drawSmoothPips(
    value: Int,
    center: Offset,
    faceSize: Float
) {
    val pipRadius = faceSize * 0.096f
    val spacing = faceSize * 0.25f

    val pipColorDeep = Color(0xFF141416)
    val pipColorShadow = Color(0x38000000)
    val pipReflection = Color(0x45FFFFFF)

    fun drawPip(x: Float, y: Float) {
        // Soft sunken bevel shadow underneath the pip
        drawCircle(
            color = pipColorShadow,
            radius = pipRadius * 1.06f,
            center = Offset(x, y + (pipRadius * 0.18f))
        )
        // Solid deep black pip dot
        drawCircle(
            color = pipColorDeep,
            radius = pipRadius,
            center = Offset(x, y)
        )
        // Subtle specular reflection highlight on top-left of dot
        drawCircle(
            color = pipReflection,
            radius = pipRadius * 0.30f,
            center = Offset(x - pipRadius * 0.30f, y - pipRadius * 0.30f)
        )
    }

    val cx = center.x
    val cy = center.y

    when (value) {
        1 -> {
            // Large focal center pip for 1
            drawPip(cx, cy)
        }
        2 -> {
            drawPip(cx - spacing, cy - spacing)
            drawPip(cx + spacing, cy + spacing)
        }
        3 -> {
            drawPip(cx - spacing, cy - spacing)
            drawPip(cx, cy)
            drawPip(cx + spacing, cy + spacing)
        }
        4 -> {
            drawPip(cx - spacing, cy - spacing)
            drawPip(cx + spacing, cy - spacing)
            drawPip(cx - spacing, cy + spacing)
            drawPip(cx + spacing, cy + spacing)
        }
        5 -> {
            drawPip(cx - spacing, cy - spacing)
            drawPip(cx + spacing, cy - spacing)
            drawPip(cx, cy)
            drawPip(cx - spacing, cy + spacing)
            drawPip(cx + spacing, cy + spacing)
        }
        6 -> {
            drawPip(cx - spacing, cy - spacing)
            drawPip(cx + spacing, cy - spacing)
            drawPip(cx - spacing, cy)
            drawPip(cx + spacing, cy)
            drawPip(cx - spacing, cy + spacing)
            drawPip(cx + spacing, cy + spacing)
        }
    }
}
