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
 * Premium lathe-turned vector pawn:
 * - Spherical polished head with 315° specular key light
 * - Torus collar ring with clean metallic glint
 * - Sculpted tapered waist & flared foot with contact drop shadow
 * - Movable state: elegant pulsing team halo ring at the pedestal
 * - Accessibility: Optional color-blind symbols (●, ▲, ■, ★) on crown
 * - Ghost pawn support for destination preview
 */
@Composable
fun TurnedPawn(
    color: PlayerColor,
    isMovable: Boolean = false,
    isFinished: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    hopElevationPx: Float = 0f,
    isGhost: Boolean = false,
    showColorBlindSymbol: Boolean = false,
    customPrimaryColor: Color? = null,
    customAccentColor: Color? = null,
    customDarkShade: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pawnGlow")
    val pulseAlpha = if (isMovable) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else 0f

    Canvas(modifier = modifier.size(size)) {
        drawTurnedPawn(
            color = color,
            isMovable = isMovable,
            isFinished = isFinished,
            hopElevation = hopElevationPx,
            isGhost = isGhost,
            pulseAlpha = pulseAlpha,
            showColorBlindSymbol = showColorBlindSymbol,
            customPrimaryColor = customPrimaryColor,
            customAccentColor = customAccentColor,
            customDarkShade = customDarkShade
        )
    }
}

fun DrawScope.drawTurnedPawn(
    color: PlayerColor,
    isMovable: Boolean,
    isFinished: Boolean,
    hopElevation: Float = 0f,
    isGhost: Boolean = false,
    pulseAlpha: Float = 0f,
    showColorBlindSymbol: Boolean = false,
    customPrimaryColor: Color? = null,
    customAccentColor: Color? = null,
    customDarkShade: Color? = null
) {
    val primaryColor = customPrimaryColor ?: color.primaryColor
    val accentColor = customAccentColor ?: color.accentColor
    val darkShade = customDarkShade ?: color.darkShade

    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val alphaFactor = if (isGhost) 0.38f else 1.0f

    // 1. Dynamic Contact Drop Shadow
    if (!isGhost) {
        val shadowW = (w * 0.82f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
        val shadowH = (h * 0.22f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
        val shadowAlpha = (0.50f - (hopElevation / 160f).coerceIn(0f, 0.35f))
        drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(cx - shadowW / 2f, h * 0.78f + (hopElevation * 0.08f)),
            size = Size(shadowW, shadowH)
        )
    }

    val lift = -hopElevation

    // 2. Movable Indicator: Elegant Pulsing Halo Ring at Base
    if (isMovable && !isGhost) {
        val haloW = w * 0.94f
        val haloH = h * 0.28f
        drawOval(
            color = StarGold.copy(alpha = pulseAlpha * 0.35f),
            topLeft = Offset(cx - haloW / 2f, h * 0.72f + lift),
            size = Size(haloW, haloH)
        )
        drawOval(
            color = StarGold.copy(alpha = pulseAlpha),
            topLeft = Offset(cx - haloW * 0.44f, h * 0.74f + lift),
            size = Size(haloW * 0.88f, haloH * 0.80f),
            style = Stroke(width = 1.8.dp.toPx())
        )
    }

    // 3. Flared Foot Pedestal
    val footBottomY = h * 0.94f + lift
    val footTopY = h * 0.72f + lift
    val footHalfW = w * 0.38f
    val footTopHalfW = w * 0.23f

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
            accentColor.copy(alpha = alphaFactor),
            primaryColor.copy(alpha = alphaFactor),
            darkShade.copy(alpha = alphaFactor)
        ),
        startX = cx - footHalfW,
        endX = cx + footHalfW
    )
    drawPath(footPath, baseGradient, style = Fill)
    drawPath(
        footPath,
        darkShade.copy(alpha = 0.95f * alphaFactor),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // Pedestal Bottom Rim Bevel
    val rimH = h * 0.10f
    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                accentColor.copy(alpha = alphaFactor),
                primaryColor.copy(alpha = alphaFactor),
                darkShade.copy(alpha = alphaFactor)
            ),
            startX = cx - footHalfW,
            endX = cx + footHalfW
        ),
        topLeft = Offset(cx - footHalfW, footBottomY - rimH * 0.6f),
        size = Size(footHalfW * 2f, rimH)
    )

    // 4. Conical Sculpted Waist
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
        darkShade.copy(alpha = 0.95f * alphaFactor),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // 5. Torus Collar Ring
    val collarY = h * 0.42f + lift
    val collarHalfW = w * 0.22f
    val collarH = h * 0.09f

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.80f * alphaFactor),
                accentColor.copy(alpha = alphaFactor),
                primaryColor.copy(alpha = alphaFactor),
                darkShade.copy(alpha = alphaFactor)
            ),
            startX = cx - collarHalfW,
            endX = cx + collarHalfW
        ),
        topLeft = Offset(cx - collarHalfW, collarY - collarH / 2f),
        size = Size(collarHalfW * 2f, collarH)
    )

    // 6. Spherical Crown Head with 315° radial specular key light
    val headRadius = w * 0.26f
    val headCenter = Offset(cx, h * 0.25f + lift)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * alphaFactor),
                accentColor.copy(alpha = alphaFactor),
                primaryColor.copy(alpha = alphaFactor),
                darkShade.copy(alpha = alphaFactor)
            ),
            center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
            radius = headRadius * 1.30f
        ),
        radius = headRadius,
        center = headCenter
    )

    drawCircle(
        color = darkShade.copy(alpha = 0.95f * alphaFactor),
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 1.0.dp.toPx())
    )

    // Specular Highlight Glint on sphere
    drawOval(
        color = Color.White.copy(alpha = 0.92f * alphaFactor),
        topLeft = Offset(headCenter.x - headRadius * 0.60f, headCenter.y - headRadius * 0.66f),
        size = Size(headRadius * 0.50f, headRadius * 0.32f)
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
}

private fun DrawScope.drawColorBlindSymbol(cx: Float, cy: Float, radius: Float, color: PlayerColor) {
    val symColor = Color.White.copy(alpha = 0.95f)
    val stroke = Stroke(width = 1.4.dp.toPx())
    when (color) {
        PlayerColor.RED -> {
            drawCircle(color = symColor, radius = radius, center = Offset(cx, cy), style = stroke)
        }
        PlayerColor.GREEN -> {
            val p = Path().apply {
                moveTo(cx, cy - radius)
                lineTo(cx + radius, cy + radius * 0.8f)
                lineTo(cx - radius, cy + radius * 0.8f)
                close()
            }
            drawPath(p, color = symColor, style = stroke)
        }
        PlayerColor.YELLOW -> {
            drawRect(
                color = symColor,
                topLeft = Offset(cx - radius * 0.8f, cy - radius * 0.8f),
                size = Size(radius * 1.6f, radius * 1.6f),
                style = stroke
            )
        }
        PlayerColor.BLUE -> {
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
