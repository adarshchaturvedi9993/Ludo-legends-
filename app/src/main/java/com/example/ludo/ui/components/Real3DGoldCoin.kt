package com.example.ludo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tactile, realistic 3D Gold Medallion Coin:
 * - Layered physical coin geometry with depth.
 * - Outer drop shadow for tactile elevation.
 * - Metallic 24K gold rim bevel with dual-tone shading (highlight on top-left, depth on bottom-right).
 * - Fine milled coin serration / beaded ridge ring along the inner rim.
 * - Recessed coin plate with smooth metallic radial/linear gradient.
 * - Embossed 3D center star/emblem with specular highlight and bottom bevel shadow.
 * - Top-left crescent specular luster highlight.
 */
@Composable
fun Real3DGoldCoin(
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawRealistic3DGoldCoin()
        }
    }
}

private fun DrawScope.drawRealistic3DGoldCoin() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val radius = (w.coerceAtMost(h) / 2f) * 0.95f

    // 1. Soft bottom-right drop shadow
    drawCircle(
        color = Color(0x66000000),
        radius = radius * 0.98f,
        center = Offset(cx + radius * 0.08f, cy + radius * 0.12f)
    )

    // Palette for rich, authentic physical gold
    val goldHighlight = Color(0xFFFFF7C2) // Lustrous bright specular gold
    val goldLight = Color(0xFFFFE066)     // Polished gold
    val goldPrimary = Color(0xFFF59E0B)   // Classic 24k gold
    val goldDeep = Color(0xFFB45309)      // Shadowed warm brass gold
    val goldDark = Color(0xFF78350F)      // Deep bevel shadow gold
    val goldRimBorder = Color(0xFF92400E)

    // 2. Outer Beveled Rim Ring (angled linear metallic gradient from top-left to bottom-right)
    val outerRimBrush = Brush.linearGradient(
        colors = listOf(goldHighlight, goldLight, goldPrimary, goldDeep, goldDark),
        start = Offset(cx - radius, cy - radius),
        end = Offset(cx + radius, cy + radius)
    )
    drawCircle(
        brush = outerRimBrush,
        radius = radius,
        center = Offset(cx, cy)
    )

    // 3. Crisp outer coin edge definition stroke
    drawCircle(
        color = goldRimBorder.copy(alpha = 0.7f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = (radius * 0.06f).coerceAtLeast(0.8f))
    )

    // 4. Milled inner serrations / beaded ridge ring
    val milledRadius = radius * 0.84f
    val numBeads = 18
    val beadRadius = radius * 0.045f
    for (i in 0 until numBeads) {
        val angle = (i * 2.0 * PI / numBeads).toFloat()
        val bx = cx + cos(angle) * milledRadius
        val by = cy + sin(angle) * milledRadius
        drawCircle(
            color = if (angle in (PI * 0.75)..(PI * 1.75)) goldHighlight.copy(alpha = 0.85f) else goldDark.copy(alpha = 0.65f),
            radius = beadRadius,
            center = Offset(bx, by)
        )
    }

    // 5. Recessed Inner Coin Field Plate
    val innerPlateRadius = radius * 0.74f
    val innerPlateBrush = Brush.radialGradient(
        colors = listOf(goldLight, goldPrimary, goldDeep),
        center = Offset(cx - innerPlateRadius * 0.25f, cy - innerPlateRadius * 0.25f),
        radius = innerPlateRadius * 1.2f
    )
    drawCircle(
        brush = innerPlateBrush,
        radius = innerPlateRadius,
        center = Offset(cx, cy)
    )

    // Inner plate bevel shadow (creates depth illusion into recessed center)
    drawCircle(
        color = goldDark.copy(alpha = 0.55f),
        radius = innerPlateRadius,
        center = Offset(cx, cy),
        style = Stroke(width = (innerPlateRadius * 0.09f).coerceAtLeast(0.7f))
    )

    // 6. Embossed 3D Center Star Emblem with bevel depth
    val starRadius = innerPlateRadius * 0.58f
    val starInnerRadius = starRadius * 0.42f
    val starPoints = 5

    fun buildStarPath(offset: Offset, rOut: Float, rIn: Float): Path {
        val path = Path()
        var angle = -PI / 2.0
        val step = PI / starPoints
        for (i in 0 until starPoints * 2) {
            val r = if (i % 2 == 0) rOut else rIn
            val px = (offset.x + cos(angle) * r).toFloat()
            val py = (offset.y + sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            angle += step
        }
        path.close()
        return path
    }

    // Star bottom bevel shadow
    val shadowOffset = Offset(cx, cy + radius * 0.045f)
    drawPath(
        path = buildStarPath(shadowOffset, starRadius, starInnerRadius),
        color = goldDark.copy(alpha = 0.85f),
        style = Fill
    )

    // Star gold body
    val starBrush = Brush.verticalGradient(
        colors = listOf(goldHighlight, goldLight, goldPrimary),
        startY = cy - starRadius,
        endY = cy + starRadius
    )
    drawPath(
        path = buildStarPath(Offset(cx, cy), starRadius, starInnerRadius),
        brush = starBrush,
        style = Fill
    )

    // Star highlight ridge
    drawPath(
        path = buildStarPath(Offset(cx, cy), starRadius, starInnerRadius),
        color = goldHighlight.copy(alpha = 0.9f),
        style = Stroke(width = (radius * 0.04f).coerceAtLeast(0.6f))
    )

    // 7. Top-Left Arc Specular Gloss Reflection (metallic shine)
    val glossPath = Path().apply {
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = cx - radius * 0.82f,
                top = cy - radius * 0.82f,
                right = cx + radius * 0.82f,
                bottom = cy + radius * 0.82f
            ),
            startAngleDegrees = 190f,
            sweepAngleDegrees = 80f,
            forceMoveTo = true
        )
    }
    drawPath(
        path = glossPath,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = (radius * 0.10f).coerceAtLeast(1.0f))
    )
}
