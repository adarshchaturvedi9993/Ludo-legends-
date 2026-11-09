package com.example.ludo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ludo.model.PlayerColor
import com.example.ui.theme.StarGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium 2.5D lathe-turned vector pawn:
 * - Spherical glossy head with 315° radial specular key light
 * - Torus collar ring with specular glint
 * - Sculpted tapered waist & flared foot with contact drop shadow
 * - Movable state: pulsing gold/player glow ring, bouncing chevron arrow above crown
 * - Accessibility: Optional color-blind symbols (●, ▲, ■, ★) on crown
 * - Ghost pawn support for destination preview
 */
@Composable
fun TurnedPawn(
    color: PlayerColor,
    isMovable: Boolean,
    isFinished: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    hopElevationPx: Float = 0f,
    isGhost: Boolean = false,
    showColorBlindSymbol: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pawnArrow")
    val arrowBounce = if (isMovable) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrow"
        ).value
    } else 0f

    Canvas(modifier = modifier.size(size)) {
        drawTurnedPawn(
            color = color,
            isMovable = isMovable,
            isFinished = isFinished,
            hopElevation = hopElevationPx,
            isGhost = isGhost,
            arrowOffsetPx = arrowBounce,
            showColorBlindSymbol = showColorBlindSymbol
        )
    }
}

fun DrawScope.drawTurnedPawn(
    color: PlayerColor,
    isMovable: Boolean,
    isFinished: Boolean,
    hopElevation: Float = 0f,
    isGhost: Boolean = false,
    arrowOffsetPx: Float = 0f,
    showColorBlindSymbol: Boolean = false
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val alphaFactor = if (isGhost) 0.42f else 1.0f

    // 1. Dynamic Contact Drop Shadow
    if (!isGhost) {
        val shadowW = (w * 0.82f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
        val shadowH = (h * 0.22f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
        val shadowAlpha = (0.52f - (hopElevation / 160f).coerceIn(0f, 0.35f))
        drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(cx - shadowW / 2f, h * 0.78f + (hopElevation * 0.08f)),
            size = Size(shadowW, shadowH)
        )
    }

    val lift = -hopElevation

    // 2. Movable Indicator: Pulsing glow halo
    if (isMovable && !isGhost) {
        val haloW = w * 0.94f
        val haloH = h * 0.28f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.accentColor.copy(alpha = 0.95f),
                    StarGold.copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = Offset(cx, h * 0.84f + lift),
                radius = haloW * 0.55f
            ),
            topLeft = Offset(cx - haloW / 2f, h * 0.70f + lift),
            size = Size(haloW, haloH)
        )
        drawOval(
            color = StarGold,
            topLeft = Offset(cx - haloW * 0.46f, h * 0.72f + lift),
            size = Size(haloW * 0.92f, haloH * 0.85f),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // 3. Flared Foot Pedestal
    val footBottomY = h * 0.94f + lift
    val footTopY = h * 0.72f + lift
    val footHalfW = w * 0.39f
    val footTopHalfW = w * 0.24f

    val footPath = Path().apply {
        moveTo(cx - footHalfW, footBottomY)
        cubicTo(
            cx - footHalfW * 0.92f, footBottomY - h * 0.08f,
            cx - footTopHalfW * 1.10f, footTopY + h * 0.04f,
            cx - footTopHalfW, footTopY
        )
        lineTo(cx + footTopHalfW, footTopY)
        cubicTo(
            cx + footTopHalfW * 1.10f, footTopY + h * 0.04f,
            cx + footHalfW * 0.92f, footBottomY - h * 0.08f,
            cx + footHalfW, footBottomY
        )
        close()
    }

    val baseGradient = Brush.horizontalGradient(
        colors = listOf(
            color.accentColor.copy(alpha = alphaFactor),
            color.primaryColor.copy(alpha = alphaFactor),
            color.darkShade.copy(alpha = alphaFactor)
        ),
        startX = cx - footHalfW,
        endX = cx + footHalfW
    )
    drawPath(footPath, baseGradient, style = Fill)
    drawPath(
        footPath,
        color.darkShade.copy(alpha = 0.95f * alphaFactor),
        style = Stroke(width = 1.dp.toPx())
    )

    // Pedestal Bottom Rim Bevel
    val rimH = h * 0.10f
    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                color.accentColor.copy(alpha = alphaFactor),
                color.primaryColor.copy(alpha = alphaFactor),
                color.darkShade.copy(alpha = alphaFactor)
            ),
            startX = cx - footHalfW,
            endX = cx + footHalfW
        ),
        topLeft = Offset(cx - footHalfW, footBottomY - rimH * 0.6f),
        size = Size(footHalfW * 2f, rimH)
    )

    // 4. Conical Sculpted Body / Waist
    val waistBottomY = footTopY
    val waistTopY = h * 0.43f + lift
    val waistMidHalfW = w * 0.14f
    val waistTopHalfW = w * 0.17f

    val waistPath = Path().apply {
        moveTo(cx - footTopHalfW, waistBottomY)
        cubicTo(
            cx - waistMidHalfW * 1.05f, waistBottomY - h * 0.09f,
            cx - waistMidHalfW, waistTopY + h * 0.08f,
            cx - waistTopHalfW, waistTopY
        )
        lineTo(cx + waistTopHalfW, waistTopY)
        cubicTo(
            cx + waistMidHalfW, waistTopY + h * 0.08f,
            cx + waistMidHalfW * 1.05f, waistBottomY - h * 0.09f,
            cx + footTopHalfW, waistBottomY
        )
        close()
    }
    drawPath(waistPath, baseGradient, style = Fill)
    drawPath(
        waistPath,
        color.darkShade.copy(alpha = 0.95f * alphaFactor),
        style = Stroke(width = 0.9.dp.toPx())
    )

    // 5. Torus Collar Ring
    val collarY = h * 0.42f + lift
    val collarHalfW = w * 0.23f
    val collarH = h * 0.09f

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * alphaFactor),
                color.accentColor.copy(alpha = alphaFactor),
                color.primaryColor.copy(alpha = alphaFactor),
                color.darkShade.copy(alpha = alphaFactor)
            ),
            startX = cx - collarHalfW,
            endX = cx + collarHalfW
        ),
        topLeft = Offset(cx - collarHalfW, collarY - collarH / 2f),
        size = Size(collarHalfW * 2f, collarH)
    )

    // 6. Spherical Crown Head with 315° radial specular key light
    val headRadius = w * 0.27f
    val headCenter = Offset(cx, h * 0.25f + lift)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * alphaFactor),
                color.accentColor.copy(alpha = alphaFactor),
                color.primaryColor.copy(alpha = alphaFactor),
                color.darkShade.copy(alpha = alphaFactor)
            ),
            center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
            radius = headRadius * 1.30f
        ),
        radius = headRadius,
        center = headCenter
    )

    drawCircle(
        color = color.darkShade.copy(alpha = 0.95f * alphaFactor),
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 1.1.dp.toPx())
    )

    // Specular Highlight Glint on sphere
    drawOval(
        color = Color.White.copy(alpha = 0.95f * alphaFactor),
        topLeft = Offset(headCenter.x - headRadius * 0.62f, headCenter.y - headRadius * 0.68f),
        size = Size(headRadius * 0.54f, headRadius * 0.34f)
    )

    // 7. Finished Winner Badge (Golden Star)
    if (isFinished) {
        drawCircle(
            color = StarGold.copy(alpha = alphaFactor),
            radius = headRadius * 0.42f,
            center = headCenter
        )
        drawPawnMiniStar(headCenter.x, headCenter.y, headRadius * 0.32f, Color.White.copy(alpha = alphaFactor))
    }

    // 8. Color-Blind Mode Symbol (● for Red, ▲ for Green, ■ for Yellow, ★ for Blue)
    if (showColorBlindSymbol && !isFinished) {
        drawColorBlindSymbol(cx, headCenter.y, headRadius * 0.36f, color)
    }

    // 9. Bouncing Chevron Arrow above head when movable
    if (isMovable && !isGhost) {
        val arrowTipY = headCenter.y - headRadius - 4.dp.toPx() + arrowOffsetPx
        val arrowW = 8.dp.toPx()
        val arrowH = 6.dp.toPx()
        val arrowPath = Path().apply {
            moveTo(cx - arrowW / 2f, arrowTipY - arrowH)
            lineTo(cx, arrowTipY)
            lineTo(cx + arrowW / 2f, arrowTipY - arrowH)
        }
        drawPath(
            path = arrowPath,
            color = StarGold,
            style = Stroke(width = 2.5.dp.toPx())
        )
        drawPath(
            path = arrowPath,
            color = Color.White,
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
}

private fun DrawScope.drawColorBlindSymbol(cx: Float, cy: Float, radius: Float, color: PlayerColor) {
    val symColor = Color.White.copy(alpha = 0.95f)
    val stroke = Stroke(width = 1.4.dp.toPx())
    when (color) {
        PlayerColor.RED -> { // Circle
            drawCircle(color = symColor, radius = radius, center = Offset(cx, cy), style = stroke)
        }
        PlayerColor.GREEN -> { // Triangle
            val p = Path().apply {
                moveTo(cx, cy - radius)
                lineTo(cx + radius, cy + radius * 0.8f)
                lineTo(cx - radius, cy + radius * 0.8f)
                close()
            }
            drawPath(p, color = symColor, style = stroke)
        }
        PlayerColor.YELLOW -> { // Square
            drawRect(
                color = symColor,
                topLeft = Offset(cx - radius * 0.8f, cy - radius * 0.8f),
                size = Size(radius * 1.6f, radius * 1.6f),
                style = stroke
            )
        }
        PlayerColor.BLUE -> { // Star
            drawPawnMiniStar(cx, cy, radius, symColor)
        }
    }
}

private fun DrawScope.drawPawnMiniStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.42f
    val numPoints = 5
    var angle = -PI / 2.0

    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (cx + cos(angle) * r).toFloat()
        val y = (cy + sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += PI / numPoints
    }
    path.close()
    drawPath(path, color, style = Fill)
}
