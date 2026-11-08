package com.example.ludo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handcrafted Hero Artwork for Ludo Legends:
 *
 * Art Direction:
 * - Isometric miniature tournament board with 4 colored corner home yards.
 * - 4 lathe-turned tournament pawns positioned in dramatic competitive standoff.
 * - Realistic tumbled ivory 3D dice showing 6 and 5 pips with soft contact shadows.
 * - Handcrafted lighting from top-left (315°) casting realistic contact shadows.
 * - Pure Compose Canvas rendering for infinite vector sharpness.
 */
@Composable
fun LudoHeroIllustration(
    modifier: Modifier = Modifier,
    height: Dp = 175.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val canvasW = size.width
            val canvasH = size.height
            val cx = canvasW * 0.5f
            val cy = canvasH * 0.52f

            // 1. Perspective Miniature Board Diamond / Hexagon Podium
            val boardW = (canvasW * 0.78f).coerceAtMost(canvasH * 2.2f)
            val boardH = boardW * 0.52f

            // Ambient ground occlusion shadow
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(cx, cy + 12.dp.toPx()),
                    radius = boardW * 0.55f
                ),
                topLeft = Offset(cx - boardW * 0.55f, cy - boardH * 0.35f + 10.dp.toPx()),
                size = Size(boardW * 1.1f, boardH * 1.1f)
            )

            // Board Wood / Beveled Edge Rim (3D depth extrusion)
            val extrusionH = 12.dp.toPx()
            val rimPath = Path().apply {
                moveTo(cx - boardW * 0.5f, cy)
                lineTo(cx, cy + boardH * 0.5f)
                lineTo(cx + boardW * 0.5f, cy)
                lineTo(cx + boardW * 0.5f, cy + extrusionH)
                lineTo(cx, cy + boardH * 0.5f + extrusionH)
                lineTo(cx - boardW * 0.5f, cy + extrusionH)
                close()
            }
            drawPath(
                path = rimPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF152441), Color(0xFF091224)),
                    startY = cy,
                    endY = cy + extrusionH
                )
            )
            drawPath(
                path = rimPath,
                color = BorderHairline.copy(alpha = 0.8f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Board Surface Diamond
            val boardSurface = Path().apply {
                moveTo(cx, cy - boardH * 0.5f) // Top apex
                lineTo(cx + boardW * 0.5f, cy)  // Right apex
                lineTo(cx, cy + boardH * 0.5f)  // Bottom apex
                lineTo(cx - boardW * 0.5f, cy)  // Left apex
                close()
            }
            drawPath(
                path = boardSurface,
                brush = Brush.radialGradient(
                    colors = listOf(SurfaceElevated, SurfacePanel, Color(0xFF0A1326)),
                    center = Offset(cx, cy),
                    radius = boardW * 0.5f
                )
            )
            // Royal Gold Inlay Perimeter Border
            drawPath(
                path = boardSurface,
                brush = Brush.linearGradient(
                    colors = listOf(GoldHighlight, AccentPrimary, Color(0xFFB8860B), AccentPrimary),
                    start = Offset(cx - boardW * 0.5f, cy - boardH * 0.5f),
                    end = Offset(cx + boardW * 0.5f, cy + boardH * 0.5f)
                ),
                style = Stroke(width = 2.dp.toPx())
            )

            // 4 Isometric Corner Home Yards:
            // Top-Left (West): Red Yard
            drawIsometricCornerYard(
                cx = cx - boardW * 0.24f,
                cy = cy - boardH * 0.16f,
                radiusX = boardW * 0.18f,
                radiusY = boardH * 0.18f,
                fillColor = Color(0xFF201118),
                accentColor = LudoRed
            )
            // Top-Right (North): Green Yard
            drawIsometricCornerYard(
                cx = cx + boardW * 0.24f,
                cy = cy - boardH * 0.16f,
                radiusX = boardW * 0.18f,
                radiusY = boardH * 0.18f,
                fillColor = Color(0xFF10201A),
                accentColor = LudoGreen
            )
            // Bottom-Right (East): Yellow Yard
            drawIsometricCornerYard(
                cx = cx + boardW * 0.24f,
                cy = cy + boardH * 0.16f,
                radiusX = boardW * 0.18f,
                radiusY = boardH * 0.18f,
                fillColor = Color(0xFF231F14),
                accentColor = LudoYellow
            )
            // Bottom-Left (South): Blue Yard
            drawIsometricCornerYard(
                cx = cx - boardW * 0.24f,
                cy = cy + boardH * 0.16f,
                radiusX = boardW * 0.18f,
                radiusY = boardH * 0.18f,
                fillColor = Color(0xFF111A2B),
                accentColor = LudoBlue
            )

            // Center Cross Division & Brass Star
            val crossHairline = Brush.linearGradient(
                colors = listOf(Color.Transparent, AccentPrimary.copy(alpha = 0.5f), Color.Transparent)
            )
            drawLine(
                brush = crossHairline,
                start = Offset(cx - boardW * 0.35f, cy),
                end = Offset(cx + boardW * 0.35f, cy),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                brush = crossHairline,
                start = Offset(cx, cy - boardH * 0.35f),
                end = Offset(cx, cy + boardH * 0.35f),
                strokeWidth = 1.dp.toPx()
            )
            drawHeroCenterStar(cx, cy, radius = boardW * 0.07f, color = StarGold)

            // 2. The 4 Beautifully Modelled 2.5D Lathe-Turned Pawns
            val pawnScale = (canvasH / 165f).coerceIn(0.85f, 1.25f)
            val pawnW = 28.dp.toPx() * pawnScale
            val pawnH = 46.dp.toPx() * pawnScale

            // Midground Pawns (Back depth):
            // Green (Top-Right)
            drawHeroPawn(
                x = cx + boardW * 0.24f,
                y = cy - boardH * 0.16f,
                w = pawnW * 0.88f,
                h = pawnH * 0.88f,
                baseColor = LudoGreen,
                highlightColor = Color(0xFF34D399)
            )
            // Blue (Bottom-Left)
            drawHeroPawn(
                x = cx - boardW * 0.28f,
                y = cy + boardH * 0.14f,
                w = pawnW * 0.94f,
                h = pawnH * 0.94f,
                baseColor = LudoBlue,
                highlightColor = Color(0xFF60A5FA)
            )

            // Foreground Pawns (Prominent depth):
            // Red (Left foreground)
            drawHeroPawn(
                x = cx - boardW * 0.18f,
                y = cy - boardH * 0.05f,
                w = pawnW * 1.05f,
                h = pawnH * 1.05f,
                baseColor = LudoRed,
                highlightColor = Color(0xFFF87171)
            )
            // Yellow (Right foreground)
            drawHeroPawn(
                x = cx + boardW * 0.18f,
                y = cy + boardH * 0.20f,
                w = pawnW * 1.02f,
                h = pawnH * 1.02f,
                baseColor = LudoYellow,
                highlightColor = Color(0xFFFDE68A)
            )

            // 3. Realistic Tumbling Isometric Ivory Dice (Foreground Center)
            val diceSize = 38.dp.toPx() * pawnScale
            drawHeroIsometricDie(
                centerX = cx - 4.dp.toPx() * pawnScale,
                centerY = cy + 6.dp.toPx() * pawnScale,
                size = diceSize,
                topFacePips = 6,
                rotationAngleDeg = -10f
            )
        }
    }
}

private fun DrawScope.drawIsometricCornerYard(
    cx: Float,
    cy: Float,
    radiusX: Float,
    radiusY: Float,
    fillColor: Color,
    accentColor: Color
) {
    // Yard Disc Surface
    drawOval(
        color = fillColor,
        topLeft = Offset(cx - radiusX, cy - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f)
    )
    drawOval(
        color = accentColor.copy(alpha = 0.65f),
        topLeft = Offset(cx - radiusX, cy - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f),
        style = Stroke(width = 1.2.dp.toPx())
    )
    // Inner pedestal ring
    drawOval(
        color = accentColor.copy(alpha = 0.25f),
        topLeft = Offset(cx - radiusX * 0.55f, cy - radiusY * 0.55f),
        size = Size(radiusX * 1.1f, radiusY * 1.1f),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawHeroCenterStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.45f
    val numPoints = 8
    var angle = -PI / 2.0

    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (cx + cos(angle) * r).toFloat()
        val y = (cy + sin(angle) * r * 0.6f).toFloat() // Perspective squash
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += PI / numPoints
    }
    path.close()
    drawPath(path, color, style = Fill)
    drawCircle(GoldHighlight, radius = radius * 0.25f, center = Offset(cx, cy))
}

private fun DrawScope.drawHeroPawn(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    baseColor: Color,
    highlightColor: Color
) {
    // Ground drop shadow
    val shadowW = w * 0.90f
    val shadowH = h * 0.26f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.60f), Color.Transparent),
            center = Offset(x, y + h * 0.46f),
            radius = shadowW * 0.5f
        ),
        topLeft = Offset(x - shadowW * 0.5f, y + h * 0.35f),
        size = Size(shadowW, shadowH)
    )

    // Lathe-Turned Flared Base
    val footPath = Path().apply {
        moveTo(x - w * 0.40f, y + h * 0.48f)
        cubicTo(
            x - w * 0.35f, y + h * 0.32f,
            x - w * 0.16f, y + h * 0.26f,
            x - w * 0.16f, y + h * 0.14f
        )
        lineTo(x + w * 0.16f, y + h * 0.14f)
        cubicTo(
            x + w * 0.16f, y + h * 0.26f,
            x + w * 0.35f, y + h * 0.32f,
            x + w * 0.40f, y + h * 0.48f
        )
        close()
    }
    val bodyGradient = Brush.horizontalGradient(
        colors = listOf(highlightColor, baseColor, Color(0xFF0F1829)),
        startX = x - w * 0.40f,
        endX = x + w * 0.40f
    )
    drawPath(footPath, bodyGradient, style = Fill)
    drawPath(footPath, Color.Black.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))

    // Torus Collar Ring
    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(highlightColor, baseColor, Color(0xFF080F1F)),
            startX = x - w * 0.24f,
            endX = x + w * 0.24f
        ),
        topLeft = Offset(x - w * 0.22f, y + h * 0.10f),
        size = Size(w * 0.44f, h * 0.12f)
    )

    // Spherical Crown Head
    val headRadius = w * 0.28f
    val headCenter = Offset(x, y - h * 0.12f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.95f), highlightColor, baseColor, Color(0xFF0A1220)),
            center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
            radius = headRadius * 1.35f
        ),
        radius = headRadius,
        center = headCenter
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.35f),
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 0.9.dp.toPx())
    )

    // Specular Highlight Glint
    drawOval(
        color = Color.White.copy(alpha = 0.95f),
        topLeft = Offset(headCenter.x - headRadius * 0.60f, headCenter.y - headRadius * 0.65f),
        size = Size(headRadius * 0.52f, headRadius * 0.32f)
    )
}

private fun DrawScope.drawHeroIsometricDie(
    centerX: Float,
    centerY: Float,
    size: Float,
    topFacePips: Int,
    rotationAngleDeg: Float
) {
    val half = size * 0.5f
    val depth = size * 0.38f

    // Soft Contact Drop Shadow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.70f), Color.Transparent),
            center = Offset(centerX, centerY + depth + 4.dp.toPx()),
            radius = size * 0.6f
        ),
        topLeft = Offset(centerX - size * 0.6f, centerY + depth * 0.5f),
        size = Size(size * 1.2f, size * 0.65f)
    )

    // 1. Top Face (Isometric rhombus)
    val topFace = Path().apply {
        moveTo(centerX, centerY - depth)
        lineTo(centerX + half, centerY - depth * 0.3f)
        lineTo(centerX, centerY + depth * 0.4f)
        lineTo(centerX - half, centerY - depth * 0.3f)
        close()
    }
    drawPath(
        path = topFace,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE4E9F2)),
            startY = centerY - depth,
            endY = centerY + depth * 0.4f
        )
    )
    drawPath(path = topFace, color = Color(0xFFB8C4D6), style = Stroke(width = 1.dp.toPx()))

    // 2. Left Face (Slight shadow)
    val leftFace = Path().apply {
        moveTo(centerX - half, centerY - depth * 0.3f)
        lineTo(centerX, centerY + depth * 0.4f)
        lineTo(centerX, centerY + depth * 1.1f)
        lineTo(centerX - half, centerY + depth * 0.4f)
        close()
    }
    drawPath(
        path = leftFace,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)),
            startX = centerX - half,
            endX = centerX
        )
    )
    drawPath(path = leftFace, color = Color(0xFF64748B), style = Stroke(width = 0.8.dp.toPx()))

    // 3. Right Face (Deeper shadow)
    val rightFace = Path().apply {
        moveTo(centerX, centerY + depth * 0.4f)
        lineTo(centerX + half, centerY - depth * 0.3f)
        lineTo(centerX + half, centerY + depth * 0.4f)
        lineTo(centerX, centerY + depth * 1.1f)
        close()
    }
    drawPath(
        path = rightFace,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
            startX = centerX,
            endX = centerX + half
        )
    )
    drawPath(path = rightFace, color = Color(0xFF475569), style = Stroke(width = 0.8.dp.toPx()))

    // 4. Authentic Dice Pips on Top Face (6 Pips arranged in 2 columns)
    val pipR = size * 0.055f
    val pipColor = Color(0xFF1E293B)
    val topCenterY = centerY - depth * 0.3f

    // 6 pips perspective coordinates
    val leftColX = centerX - half * 0.40f
    val rightColX = centerX + half * 0.40f
    val topRowY = topCenterY - depth * 0.32f
    val midRowY = topCenterY
    val botRowY = topCenterY + depth * 0.32f

    drawCircle(pipColor, radius = pipR, center = Offset(leftColX, topRowY))
    drawCircle(pipColor, radius = pipR, center = Offset(leftColX, midRowY))
    drawCircle(pipColor, radius = pipR, center = Offset(leftColX, botRowY))
    drawCircle(pipColor, radius = pipR, center = Offset(rightColX, topRowY))
    drawCircle(pipColor, radius = pipR, center = Offset(rightColX, midRowY))
    drawCircle(pipColor, radius = pipR, center = Offset(rightColX, botRowY))
}
