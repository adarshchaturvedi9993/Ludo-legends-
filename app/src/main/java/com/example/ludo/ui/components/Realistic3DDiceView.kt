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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StarGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Commercial-grade 3D ivory dice cube:
 * - Proper 3D isometric perspective cube proportions.
 * - Clean white surface with subtle realistic bevel and corner radii.
 * - Accurate deep black circular dots (pips) from 1 to 6 with authentic opposite layout.
 * - Soft realistic contact shadow that responds to bounce height.
 * - Tumble-and-bounce roll physics landing flat on the pre-decided result.
 * - Fully interactive and responsive to touch.
 */
@Composable
fun Realistic3DDiceView(
    value: Int,
    isRolling: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    accentColor: Color = StarGold,
    onBounce: () -> Unit = {},
    onRollClick: () -> Unit
) {
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }
    val bounceY = remember { Animatable(0f) }
    val displayValue = remember { androidx.compose.runtime.mutableIntStateOf(value) }

    val infiniteTransition = rememberInfiniteTransition(label = "diceGlow")
    val pulseGlow = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dicePulse"
    )

    LaunchedEffect(isRolling) {
        if (isRolling) {
            val rollDuration = 550L
            val startTime = System.currentTimeMillis()

            // Bounce 1: launch up
            bounceY.animateTo(-26f, tween(120, easing = FastOutSlowInEasing))
            onBounce()
            // Drop 1
            bounceY.animateTo(0f, tween(100, easing = LinearEasing))
            onBounce()

            // Bounce 2: medium hop
            bounceY.animateTo(-12f, tween(90, easing = FastOutSlowInEasing))
            // Drop 2
            bounceY.animateTo(0f, tween(80, easing = LinearEasing))
            onBounce()

            // Bounce 3: micro settle
            bounceY.animateTo(-4f, tween(60))
            bounceY.animateTo(0f, tween(50))
            onBounce()

            // Tumble rapid rotations while generating random face values
            while (System.currentTimeMillis() - startTime < rollDuration) {
                displayValue.intValue = Random.nextInt(1, 7)
                rotX.snapTo(rotX.value + Random.nextFloat() * 45f)
                rotY.snapTo(rotY.value + Random.nextFloat() * 55f)
                rotZ.snapTo(rotZ.value + Random.nextFloat() * 35f)
                kotlinx.coroutines.delay(35)
            }

            // Settle flat on the pre-decided roll
            displayValue.intValue = value
            rotX.animateTo(0f, tween(130, easing = FastOutSlowInEasing))
            rotY.animateTo(0f, tween(130, easing = FastOutSlowInEasing))
            rotZ.animateTo(0f, tween(130, easing = FastOutSlowInEasing))
            bounceY.snapTo(0f)
        } else {
            displayValue.intValue = value
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            bounceY.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
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
            drawRealistic3DCube(
                value = displayValue.intValue.coerceIn(1, 6),
                rotX = rotX.value,
                rotY = rotY.value,
                bounceY = bounceY.value,
                isRolling = isRolling,
                enabled = enabled,
                accentColor = accentColor,
                pulseAlpha = if (enabled && !isRolling) pulseGlow.value else 0f
            )
        }
    }
}

private fun DrawScope.drawRealistic3DCube(
    value: Int,
    rotX: Float,
    rotY: Float,
    bounceY: Float,
    isRolling: Boolean,
    enabled: Boolean,
    accentColor: Color,
    pulseAlpha: Float
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.5f + bounceY

    // 1. Soft realistic contact shadow on felt surface
    val shadowW = w * 0.85f * (1f - abs(bounceY) / 60f).coerceIn(0.45f, 1f)
    val shadowH = h * 0.26f * (1f - abs(bounceY) / 60f).coerceIn(0.45f, 1f)
    val shadowAlpha = (0.50f - abs(bounceY) / 80f).coerceIn(0.12f, 0.50f)
    drawOval(
        color = Color.Black.copy(alpha = shadowAlpha),
        topLeft = Offset(cx - shadowW / 2f, h * 0.74f),
        size = Size(shadowW, shadowH)
    )

    // Palette of genuine clean white & ivory tones
    val whiteTop = Color(0xFFFFFFFF)
    val whiteFront = Color(0xFFFCFCFD)
    val whiteBevel = Color(0xFFF1F0EC)
    val whiteSide = Color(0xFFE5E2DA)
    val pipColor = Color(0xFF111111) // Pure solid black circular dots
    val pipShadow = Color(0x33000000)

    val cubeSize = w * 0.72f
    val half = cubeSize / 2f
    val depth = cubeSize * 0.20f

    if (!isRolling || (abs(rotX) < 15f && abs(rotY) < 15f)) {
        // Flat perspective 3D isometric view landing on exact value
        val frontTopLeft = Offset(cx - half, cy - half + depth * 0.5f)
        val frontSize = Size(cubeSize, cubeSize - depth * 0.5f)

        // Pulsing selection ring when waiting for player roll
        if (enabled && pulseAlpha > 0f) {
            drawRoundRect(
                color = accentColor.copy(alpha = pulseAlpha * 0.8f),
                topLeft = Offset(frontTopLeft.x - 3.dp.toPx(), frontTopLeft.y - 3.dp.toPx()),
                size = Size(frontSize.width + 6.dp.toPx(), frontSize.height + 6.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Front Face (Clean white with subtle vertical gradient)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(whiteFront, whiteBevel),
                startY = frontTopLeft.y,
                endY = frontTopLeft.y + frontSize.height
            ),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Front face subtle outline border tinted with player's color
        drawRoundRect(
            color = if (enabled) accentColor.copy(alpha = 0.85f) else accentColor.copy(alpha = 0.35f),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = if (enabled) 1.5.dp.toPx() else 1.dp.toPx())
        )

        // Top Face perspective trapezoid (angled top surface)
        val topPath = Path().apply {
            moveTo(frontTopLeft.x, frontTopLeft.y)
            lineTo(frontTopLeft.x + depth, frontTopLeft.y - depth * 0.6f)
            lineTo(frontTopLeft.x + frontSize.width + depth, frontTopLeft.y - depth * 0.6f)
            lineTo(frontTopLeft.x + frontSize.width, frontTopLeft.y)
            close()
        }
        drawPath(topPath, whiteTop, style = Fill)
        drawPath(topPath, if (enabled) accentColor.copy(alpha = 0.4f) else Color(0xFFDCD7CB), style = Stroke(width = 1.dp.toPx()))

        // Right Face perspective trapezoid (angled side surface)
        val rightPath = Path().apply {
            moveTo(frontTopLeft.x + frontSize.width, frontTopLeft.y)
            lineTo(frontTopLeft.x + frontSize.width + depth, frontTopLeft.y - depth * 0.6f)
            lineTo(frontTopLeft.x + frontSize.width + depth, frontTopLeft.y + frontSize.height - depth * 0.6f)
            lineTo(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height)
            close()
        }
        drawPath(rightPath, whiteSide, style = Fill)
        drawPath(rightPath, if (enabled) accentColor.copy(alpha = 0.35f) else Color(0xFFC7C1B2), style = Stroke(width = 1.dp.toPx()))

        // Draw clean black circular pips on the front face
        drawCubePips(
            value = value,
            center = Offset(frontTopLeft.x + frontSize.width / 2f, frontTopLeft.y + frontSize.height / 2f),
            faceSize = frontSize.width,
            pipColor = pipColor,
            pipShadow = pipShadow
        )
    } else {
        // Dynamic tumble rotation view
        val radX = rotX * PI.toFloat() / 180f
        val radY = rotY * PI.toFloat() / 180f

        val skewX = sin(radY) * (cubeSize * 0.28f)
        val skewY = sin(radX) * (cubeSize * 0.28f)

        drawRoundRect(
            color = whiteFront,
            topLeft = Offset(cx - half + skewX, cy - half + skewY),
            size = Size(cubeSize, cubeSize),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawRoundRect(
            color = accentColor.copy(alpha = 0.85f),
            topLeft = Offset(cx - half + skewX, cy - half + skewY),
            size = Size(cubeSize, cubeSize),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawCubePips(
            value = value,
            center = Offset(cx + skewX, cy + skewY),
            faceSize = cubeSize,
            pipColor = pipColor,
            pipShadow = pipShadow
        )
    }
}

private fun DrawScope.drawCubePips(
    value: Int,
    center: Offset,
    faceSize: Float,
    pipColor: Color,
    pipShadow: Color
) {
    val pipRadius = faceSize * 0.092f
    val d = faceSize * 0.26f

    fun drawPip(x: Float, y: Float) {
        // Soft drop shadow inside sunken pip
        drawCircle(
            color = pipShadow,
            radius = pipRadius * 1.05f,
            center = Offset(x, y + 1.2f)
        )
        // Solid black circular dot
        drawCircle(
            color = pipColor,
            radius = pipRadius,
            center = Offset(x, y)
        )
        // Subtle specular dot reflection
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = pipRadius * 0.32f,
            center = Offset(x - pipRadius * 0.32f, y - pipRadius * 0.32f)
        )
    }

    val cx = center.x
    val cy = center.y

    when (value) {
        1 -> {
            drawPip(cx, cy)
        }
        2 -> {
            drawPip(cx - d, cy - d)
            drawPip(cx + d, cy + d)
        }
        3 -> {
            drawPip(cx - d, cy - d)
            drawPip(cx, cy)
            drawPip(cx + d, cy + d)
        }
        4 -> {
            drawPip(cx - d, cy - d)
            drawPip(cx + d, cy - d)
            drawPip(cx - d, cy + d)
            drawPip(cx + d, cy + d)
        }
        5 -> {
            drawPip(cx - d, cy - d)
            drawPip(cx + d, cy - d)
            drawPip(cx, cy)
            drawPip(cx - d, cy + d)
            drawPip(cx + d, cy + d)
        }
        6 -> {
            drawPip(cx - d, cy - d)
            drawPip(cx + d, cy - d)
            drawPip(cx - d, cy)
            drawPip(cx + d, cy)
            drawPip(cx - d, cy + d)
            drawPip(cx + d, cy + d)
        }
    }
}
