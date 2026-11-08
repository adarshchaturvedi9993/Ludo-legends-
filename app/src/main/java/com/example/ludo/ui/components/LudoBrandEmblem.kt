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
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfacePanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Handcrafted Ludo Legends Royal Emblem:
 * - Beveled championship shield in polished royal gold (#F5C928) and highlight (#FFE078).
 * - Midnight navy core (#080F22) with 4 player color quadrant gemstones.
 * - Central polished 5-point star.
 */
@Composable
fun LudoBrandEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f

        // 1. Shield Path
        val shieldPath = Path().apply {
            moveTo(cx, h * 0.04f)
            lineTo(w * 0.94f, h * 0.20f)
            cubicTo(
                w * 0.94f, h * 0.62f,
                cx + w * 0.35f, h * 0.85f,
                cx, h * 0.98f
            )
            cubicTo(
                cx - w * 0.35f, h * 0.85f,
                w * 0.06f, h * 0.62f,
                w * 0.06f, h * 0.20f
            )
            close()
        }

        // Shield Outer Drop Shadow
        drawPath(
            path = shieldPath,
            color = Color.Black.copy(alpha = 0.45f)
        )

        // Shield Gold Border Gradient
        val goldBrush = Brush.linearGradient(
            colors = listOf(GoldHighlight, AccentPrimary, Color(0xFFC49310), AccentPrimary),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        drawPath(shieldPath, goldBrush, style = Fill)

        // Inner Navy Shield Core
        val innerShield = Path().apply {
            moveTo(cx, h * 0.12f)
            lineTo(w * 0.86f, h * 0.25f)
            cubicTo(
                w * 0.86f, h * 0.58f,
                cx + w * 0.28f, h * 0.78f,
                cx, h * 0.90f
            )
            cubicTo(
                cx - w * 0.28f, h * 0.78f,
                w * 0.14f, h * 0.58f,
                w * 0.14f, h * 0.25f
            )
            close()
        }
        val navyBrush = Brush.verticalGradient(
            colors = listOf(SurfacePanel, SurfaceBackground),
            startY = 0f,
            endY = h
        )
        drawPath(innerShield, navyBrush, style = Fill)
        drawPath(innerShield, AccentPrimary.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))

        // 4 Quadrant Color Gems inside shield
        val cy = h * 0.46f
        val gemDist = w * 0.16f
        val gemR = w * 0.08f

        // Red (Top-Left)
        drawCircle(LudoRed, radius = gemR, center = Offset(cx - gemDist, cy - gemDist * 0.7f))
        // Green (Top-Right)
        drawCircle(LudoGreen, radius = gemR, center = Offset(cx + gemDist, cy - gemDist * 0.7f))
        // Blue (Bottom-Left)
        drawCircle(LudoBlue, radius = gemR, center = Offset(cx - gemDist, cy + gemDist * 0.7f))
        // Yellow (Bottom-Right)
        drawCircle(LudoYellow, radius = gemR, center = Offset(cx + gemDist, cy + gemDist * 0.7f))

        // Center Gold Mini Star
        drawMiniStar(cx, cy, radius = w * 0.14f, color = GoldHighlight)
    }
}

private fun DrawScope.drawMiniStar(cx: Float, cy: Float, radius: Float, color: Color) {
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
