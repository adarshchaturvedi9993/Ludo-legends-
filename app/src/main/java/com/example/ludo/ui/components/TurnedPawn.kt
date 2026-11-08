package com.example.ludo.ui.components

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
 * Premium Commercial 2.5D Vector Pawn:
 *
 * Requirements:
 * - Consistent, elegant pawn silhouette across all four colors.
 * - Perfectly rounded spherical head with directional specular highlight.
 * - Sculpted lathe-turned body proportions (flared foot, tapered waist, collar torus).
 * - Smooth 2.5D volume with subtle shadows and directional key light from top-left.
 * - Distinct player colors (#F04444, #19B96B, #FFC928, #398BFF) with dark contour separation.
 * - Dynamic contact drop shadow that reacts to hop-by-hop animation height.
 * - Pure Compose Canvas rendering with zero emojis or Unicode symbols.
 */
@Composable
fun TurnedPawn(
    color: PlayerColor,
    isMovable: Boolean,
    isFinished: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    hopElevationPx: Float = 0f
) {
    Canvas(modifier = modifier.size(size)) {
        drawTurnedPawn(
            color = color,
            isMovable = isMovable,
            isFinished = isFinished,
            hopElevation = hopElevationPx
        )
    }
}

fun DrawScope.drawTurnedPawn(
    color: PlayerColor,
    isMovable: Boolean,
    isFinished: Boolean,
    hopElevation: Float = 0f
) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f

    // 1. Dynamic Contact Shadow on cell surface
    val shadowW = (w * 0.82f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
    val shadowH = (h * 0.22f) * (1f - (hopElevation / 140f).coerceIn(0f, 0.40f))
    val shadowAlpha = (0.52f - (hopElevation / 160f).coerceIn(0f, 0.35f))
    drawOval(
        color = Color.Black.copy(alpha = shadowAlpha),
        topLeft = Offset(cx - shadowW / 2f, h * 0.78f + (hopElevation * 0.08f)),
        size = Size(shadowW, shadowH)
    )

    // Vertical hop elevation translation
    val lift = -hopElevation

    // 2. Luminous Selection / Movable Indicator Ring
    if (isMovable) {
        val haloW = w * 0.94f
        val haloH = h * 0.28f
        // Golden glow halo
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(StarGold.copy(alpha = 0.95f), StarGold.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(cx, h * 0.84f + lift),
                radius = haloW * 0.55f
            ),
            topLeft = Offset(cx - haloW / 2f, h * 0.70f + lift),
            size = Size(haloW, haloH)
        )
        // Crisp inner gold stroke ring
        drawOval(
            color = StarGold,
            topLeft = Offset(cx - haloW * 0.46f, h * 0.72f + lift),
            size = Size(haloW * 0.92f, haloH * 0.85f),
            style = Stroke(width = 2.dp.toPx())
        )
        // Pure white inner glint
        drawOval(
            color = Color.White.copy(alpha = 0.90f),
            topLeft = Offset(cx - haloW * 0.42f, h * 0.74f + lift),
            size = Size(haloW * 0.84f, haloH * 0.70f),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // 3. Flared Foot Pedestal Base
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

    // 2.5D Lighting Gradient: Key light from 315° (top-left) to deep shadow on right
    val baseGradient = Brush.horizontalGradient(
        colors = listOf(
            color.accentColor,
            color.primaryColor,
            color.darkShade
        ),
        startX = cx - footHalfW,
        endX = cx + footHalfW
    )
    drawPath(footPath, baseGradient, style = Fill)
    // Dark outline for crisp separation against any cell color
    drawPath(footPath, color.darkShade.copy(alpha = 0.95f), style = Stroke(width = 1.dp.toPx()))

    // Pedestal Bottom Rim Bevel
    val rimH = h * 0.10f
    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(color.accentColor, color.primaryColor, color.darkShade),
            startX = cx - footHalfW,
            endX = cx + footHalfW
        ),
        topLeft = Offset(cx - footHalfW, footBottomY - rimH * 0.6f),
        size = Size(footHalfW * 2f, rimH)
    )
    drawOval(
        color = color.darkShade.copy(alpha = 0.95f),
        topLeft = Offset(cx - footHalfW, footBottomY - rimH * 0.6f),
        size = Size(footHalfW * 2f, rimH),
        style = Stroke(width = 1.dp.toPx())
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

    val waistGradient = Brush.horizontalGradient(
        colors = listOf(
            color.accentColor,
            color.primaryColor,
            color.darkShade
        ),
        startX = cx - footTopHalfW,
        endX = cx + footTopHalfW
    )
    drawPath(waistPath, waistGradient, style = Fill)
    drawPath(waistPath, color.darkShade.copy(alpha = 0.95f), style = Stroke(width = 0.9.dp.toPx()))

    // 5. Torus Collar Ring below Head
    val collarY = h * 0.42f + lift
    val collarHalfW = w * 0.23f
    val collarH = h * 0.09f

    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                color.accentColor,
                color.primaryColor,
                color.darkShade
            ),
            startX = cx - collarHalfW,
            endX = cx + collarHalfW
        ),
        topLeft = Offset(cx - collarHalfW, collarY - collarH / 2f),
        size = Size(collarHalfW * 2f, collarH)
    )
    drawOval(
        color = color.darkShade.copy(alpha = 0.95f),
        topLeft = Offset(cx - collarHalfW, collarY - collarH / 2f),
        size = Size(collarHalfW * 2f, collarH),
        style = Stroke(width = 0.9.dp.toPx())
    )

    // Collar Specular Highlight Glint
    drawOval(
        color = Color.White.copy(alpha = 0.90f),
        topLeft = Offset(cx - collarHalfW * 0.60f, collarY - collarH * 0.35f),
        size = Size(collarHalfW * 0.45f, collarH * 0.35f)
    )

    // 6. Spherical Crown Head (Smooth 2.5D with 315° radial specular key light)
    val headRadius = w * 0.27f
    val headCenter = Offset(cx, h * 0.25f + lift)

    // Spherical lighting gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                color.accentColor,
                color.primaryColor,
                color.darkShade
            ),
            center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
            radius = headRadius * 1.30f
        ),
        radius = headRadius,
        center = headCenter
    )

    // Dark contour stroke around spherical head
    drawCircle(
        color = color.darkShade.copy(alpha = 0.95f),
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 1.1.dp.toPx())
    )

    // Luminous animated outline for eligible/movable pawns
    if (isMovable) {
        drawCircle(
            color = StarGold,
            radius = headRadius + 2.2.dp.toPx(),
            center = headCenter,
            style = Stroke(width = 1.8.dp.toPx())
        )
    }

    // Specular Highlight Glint (soft white oval on top-left)
    drawOval(
        color = Color.White.copy(alpha = 0.95f),
        topLeft = Offset(headCenter.x - headRadius * 0.62f, headCenter.y - headRadius * 0.68f),
        size = Size(headRadius * 0.54f, headRadius * 0.34f)
    )

    // Secondary subtle rim reflection on opposite bottom-right edge
    drawArc(
        color = Color.White.copy(alpha = 0.30f),
        startAngle = 25f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(headCenter.x - headRadius * 0.90f, headCenter.y - headRadius * 0.90f),
        size = Size(headRadius * 1.80f, headRadius * 1.80f),
        style = Stroke(width = 0.9.dp.toPx())
    )

    // 7. Finished Winner Badge (Golden Star on Crown)
    if (isFinished) {
        drawCircle(
            color = StarGold,
            radius = headRadius * 0.42f,
            center = headCenter
        )
        drawPawnMiniStar(headCenter.x, headCenter.y, headRadius * 0.32f, Color.White)
    }
}

/**
 * Draws a crisp miniature 5-point star on crown of finished tokens.
 */
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
