package com.example.ludo.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.BoardStyle
import com.example.ludo.model.BoardTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural visual renderers for the 6 distinctive BoardStyle themes.
 * Each theme features a unique surface pattern, tile profile, ambient effect, and frame.
 */
object LudoBoardStyling {

    /**
     * Draws the style-specific decorative background, patterns, and ambient sweeps
     * beneath the cells and yards.
     */
    fun drawBoardStyleBackground(
        drawScope: DrawScope,
        boardSizePx: Float,
        cellPx: Float,
        theme: BoardTheme,
        ambientPhase: Float // 0..1 animated sweep
    ) {
        with(drawScope) {
            when (theme.boardStyle) {
                BoardStyle.ROYAL_NAVY -> drawRoyalNavy(boardSizePx, cellPx, theme, ambientPhase)
                BoardStyle.ROYAL_GOLD -> drawRoyalGold(boardSizePx, cellPx, theme, ambientPhase)
                BoardStyle.MIDNIGHT_OBSIDIAN -> drawMidnightObsidian(boardSizePx, cellPx, theme, ambientPhase)
                BoardStyle.CYBERPUNK_NEON -> drawCyberpunkNeon(boardSizePx, cellPx, theme, ambientPhase)
                BoardStyle.CLASSIC_WOOD -> drawClassicWood(boardSizePx, cellPx, theme, ambientPhase)
                BoardStyle.NEBULA_GALAXY -> drawNebulaGalaxy(boardSizePx, cellPx, theme, ambientPhase)
            }
        }
    }

    // 1. ROYAL NAVY: Compass-rose lines, beveled rounded tiles, light sweep
    private fun DrawScope.drawRoyalNavy(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f

        // Diagonal compass-rose navigation vectors
        val navLineColor = Color(0xFF1E3A8A).copy(alpha = 0.25f)
        val stroke = 1.2.dp.toPx()
        drawLine(navLineColor, Offset(0f, 0f), Offset(size, size), strokeWidth = stroke)
        drawLine(navLineColor, Offset(size, 0f), Offset(0f, size), strokeWidth = stroke)
        drawLine(navLineColor, Offset(cx, 0f), Offset(cx, size), strokeWidth = stroke * 0.8f)
        drawLine(navLineColor, Offset(0f, cy), Offset(size, cy), strokeWidth = stroke * 0.8f)

        // Concentric nautical range rings
        drawCircle(navLineColor, size * 0.28f, Offset(cx, cy), style = Stroke(stroke * 0.8f))
        drawCircle(navLineColor, size * 0.44f, Offset(cx, cy), style = Stroke(stroke * 0.8f))

        // Ambient light sweep diagonal beam
        if (phase >= 0f) {
            val sweepX = size * (phase * 2f - 0.5f)
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xFF60A5FA).copy(alpha = 0.12f), Color.Transparent),
                    start = Offset(sweepX - 80f, 0f),
                    end = Offset(sweepX + 80f, size)
                ),
                start = Offset(sweepX - size * 0.4f, 0f),
                end = Offset(sweepX + size * 0.4f, size),
                strokeWidth = size * 0.35f
            )
        }
    }

    // 2. ROYAL GOLD: Diamond-lattice foil, diamond inlays, foil sweep, double gold frame
    private fun DrawScope.drawRoyalGold(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        val gridStep = cell * 1.5f
        val foilGold = Color(0xFFF59E0B).copy(alpha = 0.10f)
        val stroke = 1.0.dp.toPx()

        var offset = -size
        while (offset < size * 2) {
            drawLine(foilGold, Offset(offset, 0f), Offset(offset + size, size), strokeWidth = stroke)
            drawLine(foilGold, Offset(offset + size, 0f), Offset(offset, size), strokeWidth = stroke)
            offset += gridStep
        }

        // Animated gold foil luster sweep
        if (phase >= 0f) {
            val sweepPos = size * (phase * 1.8f - 0.4f)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFEF3C7).copy(alpha = 0.18f),
                        Color(0xFFFBBF24).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    start = Offset(sweepPos - 120f, sweepPos - 120f),
                    end = Offset(sweepPos + 120f, sweepPos + 120f)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(size, size)
            )
        }
    }

    // 3. MIDNIGHT OBSIDIAN: Cut-glass facets, shine on tiles, silver corner brackets
    private fun DrawScope.drawMidnightObsidian(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val stroke = 1.0.dp.toPx()
        val facetSilver = Color(0xFF71717A).copy(alpha = 0.22f)

        // Geometric polygonal facet angles
        val facetOffsets = floatArrayOf(0.15f, 0.35f, 0.65f, 0.85f)
        for (f in facetOffsets) {
            drawLine(facetSilver, Offset(size * f, 0f), Offset(size, size * (1f - f)), strokeWidth = stroke)
            drawLine(facetSilver, Offset(0f, size * f), Offset(size * (1f - f), size), strokeWidth = stroke)
        }

        // Silver corner brackets (16dp L-shapes)
        val bracketLen = cell * 0.7f
        val bracketStroke = 2.0.dp.toPx()
        val bracketColor = Color(0xFFE4E4E7).copy(alpha = 0.65f)

        // Top-left
        drawLine(bracketColor, Offset(4f, 4f), Offset(4f + bracketLen, 4f), strokeWidth = bracketStroke)
        drawLine(bracketColor, Offset(4f, 4f), Offset(4f, 4f + bracketLen), strokeWidth = bracketStroke)
        // Top-right
        drawLine(bracketColor, Offset(size - 4f, 4f), Offset(size - 4f - bracketLen, 4f), strokeWidth = bracketStroke)
        drawLine(bracketColor, Offset(size - 4f, 4f), Offset(size - 4f, 4f + bracketLen), strokeWidth = bracketStroke)
        // Bottom-left
        drawLine(bracketColor, Offset(4f, size - 4f), Offset(4f + bracketLen, size - 4f), strokeWidth = bracketStroke)
        drawLine(bracketColor, Offset(4f, size - 4f), Offset(4f, size - 4f - bracketLen), strokeWidth = bracketStroke)
        // Bottom-right
        drawLine(bracketColor, Offset(size - 4f, size - 4f), Offset(size - 4f - bracketLen, size - 4f), strokeWidth = bracketStroke)
        drawLine(bracketColor, Offset(size - 4f, size - 4f), Offset(size - 4f, size - 4f - bracketLen), strokeWidth = bracketStroke)

        // Shimmer shine reflection sweep
        if (phase >= 0f) {
            val glintX = size * phase
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.Transparent),
                    start = Offset(glintX - 50f, 0f),
                    end = Offset(glintX + 50f, size)
                ),
                start = Offset(glintX, 0f),
                end = Offset(glintX, size),
                strokeWidth = 30f
            )
        }
    }

    // 4. CYBERPUNK NEON: Circuit traces, neon-outlined tiles, moving scanline, double neon frame
    private fun DrawScope.drawCyberpunkNeon(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        val cyanTraces = Color(0xFF06B6D4).copy(alpha = 0.22f)
        val magentaTraces = Color(0xFFEC4899).copy(alpha = 0.18f)
        val stroke = 1.2.dp.toPx()

        // Circuit PCB traces with 90° bends
        val p1 = Path().apply {
            moveTo(cell * 2, 0f)
            lineTo(cell * 2, cell * 3)
            lineTo(cell * 4, cell * 5)
            lineTo(cell * 4, cell * 7)
        }
        drawPath(p1, cyanTraces, style = Stroke(stroke))
        drawCircle(cyanTraces, 3.dp.toPx(), Offset(cell * 4, cell * 7))

        val p2 = Path().apply {
            moveTo(size, cell * 2)
            lineTo(size - cell * 3, cell * 2)
            lineTo(size - cell * 5, cell * 4)
            lineTo(size - cell * 7, cell * 4)
        }
        drawPath(p2, magentaTraces, style = Stroke(stroke))
        drawCircle(magentaTraces, 3.dp.toPx(), Offset(size - cell * 7, cell * 4))

        val p3 = Path().apply {
            moveTo(0f, size - cell * 2)
            lineTo(cell * 3, size - cell * 2)
            lineTo(cell * 5, size - cell * 4)
        }
        drawPath(p3, cyanTraces, style = Stroke(stroke))

        // Moving vertical scanline
        if (phase >= 0f) {
            val scanY = size * phase
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF06B6D4).copy(alpha = 0.18f), Color.Transparent),
                    startY = scanY - 24f,
                    endY = scanY + 24f
                ),
                topLeft = Offset(0f, scanY - 24f),
                size = Size(size, 48f)
            )
        }
    }

    // 5. CLASSIC WOOD: Planks with grain, brass studs, warm lamp glow, studded frame
    private fun DrawScope.drawClassicWood(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        val plankH = cell * 2.5f
        val grainColor = Color(0xFF382215).copy(alpha = 0.45f)
        val stroke = 1.0.dp.toPx()

        var y = 0f
        while (y < size) {
            drawLine(grainColor, Offset(0f, y), Offset(size, y), strokeWidth = stroke * 1.5f)
            // Subtle wood grain wave
            val p = Path().apply {
                moveTo(0f, y + plankH * 0.35f)
                cubicTo(size * 0.3f, y + plankH * 0.25f, size * 0.7f, y + plankH * 0.45f, size, y + plankH * 0.35f)
            }
            drawPath(p, Color(0xFF4A301D).copy(alpha = 0.25f), style = Stroke(stroke))
            y += plankH
        }

        // Warm radial lamp glow at top-center
        val glowRadius = size * 0.7f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDE68A).copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size * 0.5f, size * 0.2f),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(size * 0.5f, size * 0.2f)
        )

        // Brass studs around the border
        val studColor = Color(0xFFF59E0B)
        val studRadius = 2.5.dp.toPx()
        val studMargin = cell * 0.35f
        val studs = listOf(
            Offset(studMargin, studMargin),
            Offset(size - studMargin, studMargin),
            Offset(studMargin, size - studMargin),
            Offset(size - studMargin, size - studMargin),
            Offset(size * 0.5f, studMargin),
            Offset(size * 0.5f, size - studMargin),
            Offset(studMargin, size * 0.5f),
            Offset(size - studMargin, size * 0.5f)
        )
        for (s in studs) {
            drawCircle(Color.Black.copy(alpha = 0.6f), studRadius + 1.dp.toPx(), s)
            drawCircle(studColor, studRadius, s)
            drawCircle(Color.White.copy(alpha = 0.7f), studRadius * 0.35f, Offset(s.x - 0.7f, s.y - 0.7f))
        }
    }

    // 6. NEBULA GALAXY: Nebula clouds, round glowing tiles, twinkling stars
    private fun DrawScope.drawNebulaGalaxy(size: Float, cell: Float, theme: BoardTheme, phase: Float) {
        // Cosmic nebula dust clouds
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size * 0.3f, size * 0.35f),
                radius = size * 0.45f
            ),
            radius = size * 0.45f,
            center = Offset(size * 0.3f, size * 0.35f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEC4899).copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size * 0.75f, size * 0.7f),
                radius = size * 0.40f
            ),
            radius = size * 0.40f,
            center = Offset(size * 0.75f, size * 0.7f)
        )

        // Twinkling star field
        val starCoords = listOf(
            Offset(size * 0.12f, size * 0.25f),
            Offset(size * 0.88f, size * 0.18f),
            Offset(size * 0.22f, size * 0.78f),
            Offset(size * 0.82f, size * 0.82f),
            Offset(size * 0.48f, size * 0.15f),
            Offset(size * 0.52f, size * 0.85f),
            Offset(size * 0.15f, size * 0.52f),
            Offset(size * 0.85f, size * 0.48f)
        )

        starCoords.forEachIndexed { idx, pt ->
            val starPhase = ((phase + idx * 0.125f) % 1.0f)
            val starAlpha = (0.3f + 0.65f * sin(starPhase * 2 * PI.toFloat())).coerceIn(0.15f, 0.95f)
            drawCircle(Color.White.copy(alpha = starAlpha), 1.8.dp.toPx(), pt)
            drawLine(
                Color(0xFFA855F7).copy(alpha = starAlpha * 0.7f),
                Offset(pt.x - 4f, pt.y),
                Offset(pt.x + 4f, pt.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                Color(0xFFA855F7).copy(alpha = starAlpha * 0.7f),
                Offset(pt.x, pt.y - 4f),
                Offset(pt.x, pt.y + 4f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }

    /**
     * Draws style-specific border around the entire board.
     */
    fun drawBoardFrame(drawScope: DrawScope, sizePx: Float, theme: BoardTheme) {
        with(drawScope) {
            when (theme.boardStyle) {
                BoardStyle.ROYAL_GOLD -> {
                    // Double gold frame
                    drawRoundRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(sizePx - 3f, sizePx - 3f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                        topLeft = Offset(4.5f, 4.5f),
                        size = Size(sizePx - 9f, sizePx - 9f),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                BoardStyle.CYBERPUNK_NEON -> {
                    // Double neon cyan and magenta frame
                    drawRoundRect(
                        color = Color(0xFF06B6D4),
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(sizePx - 3f, sizePx - 3f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFEC4899),
                        topLeft = Offset(4.5f, 4.5f),
                        size = Size(sizePx - 9f, sizePx - 9f),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
                BoardStyle.CLASSIC_WOOD -> {
                    // Studded border
                    drawRoundRect(
                        color = Color(0xFF5E3920),
                        topLeft = Offset(2f, 2f),
                        size = Size(sizePx - 4f, sizePx - 4f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
                BoardStyle.MIDNIGHT_OBSIDIAN -> {
                    drawRoundRect(
                        color = Color(0xFF3F3F46),
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(sizePx - 3f, sizePx - 3f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                BoardStyle.NEBULA_GALAXY -> {
                    drawRoundRect(
                        color = Color(0xFFA855F7),
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(sizePx - 3f, sizePx - 3f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                BoardStyle.ROYAL_NAVY -> {
                    drawRoundRect(
                        color = theme.gridBorder,
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(sizePx - 3f, sizePx - 3f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
