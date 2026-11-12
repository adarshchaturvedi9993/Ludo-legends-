package com.example.ludo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GridCoord
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Miniature High-Fidelity Ludo Board Preview:
 * Renders an exact scaled visual preview of a Ludo board for a given theme:
 * - 4 corner yards with authentic theme-tinted surfaces.
 * - 52 track cells with theme background, grid cells, and borders.
 * - Safe star tiles rendered with theme.starColor.
 * - Solid colored 5-tile home corridors.
 * - 4-color victory triangles and center hub star gradient.
 */
@Composable
fun MiniBoardPreview(
    theme: BoardTheme,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(theme.boardBackground)
            .border(1.dp, theme.gridBorder, RoundedCornerShape(cornerRadius))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val totalSize = size.width.coerceAtMost(size.height)
            val cell = totalSize / 15f

            drawMiniLudoBoard(cell = cell, theme = theme)
        }
    }
}

private fun DrawScope.drawMiniLudoBoard(cell: Float, theme: BoardTheme) {
    val hairline = (cell * 0.08f).coerceAtLeast(0.6f)
    val gridBorder = theme.gridBorder

    // 1. Draw 4 Corner Yards (6x6 cells each)
    drawMiniYard(0f, 0f, cell * 6, cell, LudoRed, theme.yardSurfaceRed, theme)
    drawMiniYard(cell * 9, 0f, cell * 6, cell, LudoGreen, theme.yardSurfaceGreen, theme)
    drawMiniYard(cell * 9, cell * 9, cell * 6, cell, LudoYellow, theme.yardSurfaceYellow, theme)
    drawMiniYard(0f, cell * 9, cell * 6, cell, LudoBlue, theme.yardSurfaceBlue, theme)

    // 2. Draw 52 Perimeter Track Cells
    val trackIndices = BoardCoordinates.TRACK_TILES
    for (i in trackIndices.indices) {
        val coord = trackIndices[i]
        val x = coord.col * cell
        val y = coord.row * cell
        val cellColor = if (i % 2 == 0) theme.gridCell else theme.alternateCell

        drawRect(cellColor, Offset(x, y), Size(cell, cell))
        drawRect(gridBorder, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
    }

    // 3. Draw Starting Cells (Red 0, Green 13, Yellow 26, Blue 39)
    fun drawStartTile(index: Int, color: Color) {
        val coord = trackIndices[index]
        val x = coord.col * cell
        val y = coord.row * cell
        drawRect(color, Offset(x, y), Size(cell, cell))
        drawRect(gridBorder, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        drawMiniStar((coord.col + 0.5f) * cell, (coord.row + 0.5f) * cell, cell * 0.35f, Color.White)
    }

    drawStartTile(0, LudoRed)
    drawStartTile(13, LudoGreen)
    drawStartTile(26, LudoYellow)
    drawStartTile(39, LudoBlue)

    // 4. Safe Star Cells (8, 21, 34, 47)
    listOf(8, 21, 34, 47).forEach { safeIdx ->
        val coord = trackIndices[safeIdx]
        val x = coord.col * cell
        val y = coord.row * cell
        drawRect(theme.alternateCell, Offset(x, y), Size(cell, cell))
        drawRect(gridBorder, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        drawMiniStar((coord.col + 0.5f) * cell, (coord.row + 0.5f) * cell, cell * 0.35f, theme.starColor)
    }

    // 5. Home Run Corridors (5 cells each)
    fun drawHomeStrip(coords: List<GridCoord>, color: Color) {
        for (coord in coords) {
            val x = coord.col * cell
            val y = coord.row * cell
            drawRect(color, Offset(x, y), Size(cell, cell))
            drawRect(gridBorder, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        }
    }

    drawHomeStrip(BoardCoordinates.RED_HOME, LudoRed)
    drawHomeStrip(BoardCoordinates.GREEN_HOME, LudoGreen)
    drawHomeStrip(BoardCoordinates.YELLOW_HOME, LudoYellow)
    drawHomeStrip(BoardCoordinates.BLUE_HOME, LudoBlue)

    // 6. Central 4-Color Goal Confluence
    val cx = 7.5f * cell
    val cy = 7.5f * cell
    val goalLeft = 6f * cell
    val goalRight = 9f * cell
    val goalTop = 6f * cell
    val goalBottom = 9f * cell

    // Red triangle
    val redPath = Path().apply {
        moveTo(goalLeft, goalTop); lineTo(cx, cy); lineTo(goalLeft, goalBottom); close()
    }
    drawPath(redPath, LudoRed, style = Fill)

    // Green triangle
    val greenPath = Path().apply {
        moveTo(goalLeft, goalTop); lineTo(goalRight, goalTop); lineTo(cx, cy); close()
    }
    drawPath(greenPath, LudoGreen, style = Fill)

    // Yellow triangle
    val yellowPath = Path().apply {
        moveTo(goalRight, goalTop); lineTo(goalRight, goalBottom); lineTo(cx, cy); close()
    }
    drawPath(yellowPath, LudoYellow, style = Fill)

    // Blue triangle
    val bluePath = Path().apply {
        moveTo(goalLeft, goalBottom); lineTo(cx, cy); lineTo(goalRight, goalBottom); close()
    }
    drawPath(bluePath, LudoBlue, style = Fill)

    // Center divider lines
    drawLine(gridBorder, Offset(goalLeft, goalTop), Offset(goalRight, goalBottom), strokeWidth = hairline * 1.5f)
    drawLine(gridBorder, Offset(goalLeft, goalBottom), Offset(goalRight, goalTop), strokeWidth = hairline * 1.5f)
    drawRect(gridBorder, Offset(goalLeft, goalTop), Size(cell * 3, cell * 3), style = Stroke(width = hairline * 1.5f))

    // Center Gold Star Emblem
    drawMiniCenterEmblem(cx, cy, cell * 0.45f, theme)
}

private fun DrawScope.drawMiniYard(
    left: Float,
    top: Float,
    size: Float,
    cell: Float,
    playerColor: Color,
    surfaceTint: Color,
    theme: BoardTheme
) {
    val hairline = (cell * 0.08f).coerceAtLeast(0.6f)

    // Yard background
    drawRoundRect(
        color = surfaceTint,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(cell * 0.5f, cell * 0.5f)
    )
    drawRoundRect(
        color = playerColor,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(cell * 0.5f, cell * 0.5f),
        style = Stroke(width = hairline * 1.5f)
    )

    // Inner Yard Island
    val innerMargin = cell * 0.85f
    val innerSize = size - (innerMargin * 2)
    val innerLeft = left + innerMargin
    val innerTop = top + innerMargin

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(theme.gridCell, theme.alternateCell),
            startY = innerTop,
            endY = innerTop + innerSize
        ),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(cell * 0.4f, cell * 0.4f)
    )
    drawRoundRect(
        color = playerColor.copy(alpha = 0.5f),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(cell * 0.4f, cell * 0.4f),
        style = Stroke(width = hairline)
    )

    // 4 Token Yard circles
    val slotOffsets = listOf(
        Pair(0.28f, 0.28f),
        Pair(0.72f, 0.28f),
        Pair(0.28f, 0.72f),
        Pair(0.72f, 0.72f)
    )
    for ((ox, oy) in slotOffsets) {
        val px = innerLeft + innerSize * ox
        val py = innerTop + innerSize * oy
        drawCircle(playerColor, cell * 0.38f, Offset(px, py))
        drawCircle(theme.boardBackground, cell * 0.30f, Offset(px, py))
        drawCircle(playerColor, cell * 0.16f, Offset(px, py))
    }
}

private fun DrawScope.drawMiniStar(centerX: Float, centerY: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.42f
    val numPoints = 5
    var angle = -PI / 2.0
    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (centerX + cos(angle) * r).toFloat()
        val y = (centerY + sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += PI / numPoints
    }
    path.close()
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawMiniCenterEmblem(cx: Float, cy: Float, radius: Float, theme: BoardTheme) {
    drawCircle(theme.boardBackground, radius, Offset(cx, cy))
    drawCircle(theme.starColor, radius, Offset(cx, cy), style = Stroke(width = 0.8f))

    val path = Path()
    val points = 8
    val innerRadius = radius * 0.40f
    for (i in 0 until points * 2) {
        val angle = (i * PI / points).toFloat() - (PI / 2).toFloat()
        val r = if (i % 2 == 0) radius * 0.82f else innerRadius
        val px = cx + cos(angle) * r
        val py = cy + sin(angle) * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    val starBrush = Brush.radialGradient(
        colors = theme.centerStarGradient,
        center = Offset(cx, cy),
        radius = radius
    )
    drawPath(path, starBrush, style = Fill)
    drawCircle(Color.White.copy(alpha = 0.9f), radius * 0.18f, Offset(cx, cy))
}
