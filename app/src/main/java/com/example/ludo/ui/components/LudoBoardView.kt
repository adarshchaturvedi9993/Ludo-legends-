package com.example.ludo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.TokenState
import com.example.ui.theme.DarkAlternateCell
import com.example.ui.theme.DarkBoardBackground
import com.example.ui.theme.DarkGridBorder
import com.example.ui.theme.DarkGridCell
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoBlueHomeSurface
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoGreenHomeSurface
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoRedHomeSurface
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.LudoYellowHomeSurface
import com.example.ui.theme.StarGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Premium Commercial Dark Ludo Board:
 *
 * Exact User Palette:
 * - Board background: #101A2C
 * - Grid cells: #18253B
 * - Alternate cells: #1D2C43
 * - Grid borders: #34435A
 * - Red: #F04444 (dark red home surface #24141B)
 * - Green: #19B96B (dark green home surface #12261E)
 * - Yellow: #FFC928 (dark yellow home surface #272214)
 * - Blue: #398BFF (dark blue home surface #132034)
 *
 * Geometry & Features:
 * - Perfectly square (1:1 aspect ratio), 15x15 equal cells.
 * - 4 distinct dark-tinted home areas with recessed circular pedestals (zero pure white).
 * - 52 main-path cells alternating between #18253B and #1D2C43.
 * - Starting cells, 8 safe-zone stars, and 4 home corridors accurately rendered.
 * - Central 4-color triangle meeting precisely at (7.5, 7.5) with brass star hub.
 * - Perfectly aligned 2.5D pawns with hop animation and multi-token clustering.
 */
@Composable
fun LudoBoardView(
    players: List<Player>,
    currentPlayer: Player,
    movableTokenIds: Set<Int>,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    animatingTokenId: Int? = null,
    animatingTokenCoord: GridCoord? = null,
    animatingHopElevationPx: Float = 0f
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBoardBackground)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFE078),
                        Color(0xFFF5C928),
                        Color(0xFF8A6A12),
                        Color(0xFFF5C928),
                        Color(0xFFFFE078)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("ludo_board_view")
    ) {
        val boardSizePx = constraints.maxWidth.toFloat()
        val cellSizePx = boardSizePx / BoardCoordinates.GRID_SIZE

        // 1. Draw static premium dark board geometry and markings
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDarkLudoBoard(cellSizePx)
        }

        // 2. Render authentic 2.5D pawns on top
        val allTokens = players.flatMap { it.tokens }
        val tokensByCoord = allTokens.groupBy { token ->
            val coord = if (token.id == animatingTokenId && animatingTokenCoord != null) {
                animatingTokenCoord
            } else {
                BoardCoordinates.getCoordinate(token)
            }
            Pair((coord.row * 10).roundToInt(), (coord.col * 10).roundToInt())
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale = infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tokenPulse"
        )

        val density = LocalDensity.current

        for (player in players) {
            val isCurrentPlayer = player.id == currentPlayer.id
            for (token in player.tokens) {
                val isAnimatingThisToken = token.id == animatingTokenId && player.id == currentPlayer.id
                val coord = if (isAnimatingThisToken && animatingTokenCoord != null) {
                    animatingTokenCoord
                } else {
                    BoardCoordinates.getCoordinate(token)
                }

                val key = Pair((coord.row * 10).roundToInt(), (coord.col * 10).roundToInt())
                val cluster = tokensByCoord[key] ?: listOf(token)
                val indexInCluster = cluster.indexOf(token)
                val clusterSize = cluster.size

                val isMovable = isCurrentPlayer && movableTokenIds.contains(token.id)

                // Cluster offset when multiple tokens occupy the same tile
                val (offsetX, offsetY) = if (clusterSize > 1 && token.state != TokenState.YARD && !isAnimatingThisToken) {
                    val angle = (2.0 * PI * indexInCluster / clusterSize).toFloat()
                    val radius = cellSizePx * 0.22f
                    Pair(cos(angle) * radius, sin(angle) * radius)
                } else {
                    Pair(0f, 0f)
                }

                val centerX = (coord.col + 0.5f) * cellSizePx + offsetX
                val centerY = (coord.row + 0.5f) * cellSizePx + offsetY

                // Calibrated pawn sizing for crisp visual weight and touch targets
                val pawnSizePx = if (token.state == TokenState.YARD) cellSizePx * 0.94f else cellSizePx * 0.92f
                val pawnSizeDp = with(density) { pawnSizePx.toDp() }

                val hopElevation = if (isAnimatingThisToken) animatingHopElevationPx else 0f

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (centerX - pawnSizePx / 2f).roundToInt(),
                                y = (centerY - pawnSizePx / 2f).roundToInt()
                            )
                        }
                        .size(pawnSizeDp)
                        .scale(if (isMovable) pulseScale.value else 1.0f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = pawnSizeDp / 2),
                            enabled = isMovable,
                            onClick = { onTokenClick(token.id) }
                        )
                        .testTag("pawn_${player.color.name}_${token.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMovable) {
                        // Animated glowing selection outline around eligible pawn
                        Box(
                            modifier = Modifier
                                .size(pawnSizeDp * 1.18f)
                                .border(1.6.dp, StarGold.copy(alpha = 0.90f), CircleShape)
                        )
                    }

                    TurnedPawn(
                        color = player.color,
                        isMovable = isMovable,
                        isFinished = token.isFinished,
                        size = pawnSizeDp,
                        hopElevationPx = hopElevation
                    )
                }
            }
        }
    }
}

/**
 * Draws the complete, authentic dark Ludo board geometry:
 * - 4 Corner Yards (6x6 cells) with dark-tinted surfaces and dark recessed pedestals.
 * - 52 Perimeter track cells with alternating #18253B and #1D2C43 tones and #34435A grid borders.
 * - 4 Solid starting cells with white/gold safe stars.
 * - 4 Perimeter safe star cells (indices 8, 21, 34, 47).
 * - 4 Solid colored home corridors (5 cells each).
 * - Central 4-color triangle confluence meeting at (7.5, 7.5) with brass star emblem.
 */
private fun DrawScope.drawDarkLudoBoard(cell: Float) {
    val hairline = 1.dp.toPx()
    val gridBorderColor = DarkGridBorder

    // 1. Draw Corner Yards (6x6 cells each, completely removing pure white)
    // Top-Left: Red Yard (Rows 0..5, Cols 0..5)
    drawDarkYard(
        left = 0f,
        top = 0f,
        size = cell * 6,
        cell = cell,
        playerColor = LudoRed,
        surfaceTint = LudoRedHomeSurface,
        slots = BoardCoordinates.RED_YARD_SLOTS
    )

    // Top-Right: Green Yard (Rows 0..5, Cols 9..14)
    drawDarkYard(
        left = cell * 9,
        top = 0f,
        size = cell * 6,
        cell = cell,
        playerColor = LudoGreen,
        surfaceTint = LudoGreenHomeSurface,
        slots = BoardCoordinates.GREEN_YARD_SLOTS
    )

    // Bottom-Right: Yellow Yard (Rows 9..14, Cols 9..14)
    drawDarkYard(
        left = cell * 9,
        top = cell * 9,
        size = cell * 6,
        cell = cell,
        playerColor = LudoYellow,
        surfaceTint = LudoYellowHomeSurface,
        slots = BoardCoordinates.YELLOW_YARD_SLOTS
    )

    // Bottom-Left: Blue Yard (Rows 9..14, Cols 0..5)
    drawDarkYard(
        left = 0f,
        top = cell * 9,
        size = cell * 6,
        cell = cell,
        playerColor = LudoBlue,
        surfaceTint = LudoBlueHomeSurface,
        slots = BoardCoordinates.BLUE_YARD_SLOTS
    )

    // 2. Draw 52 Perimeter Track Cells with alternating dark tones (#18253B and #1D2C43)
    val trackIndices = BoardCoordinates.TRACK_TILES
    for (i in trackIndices.indices) {
        val coord = trackIndices[i]
        val x = coord.col * cell
        val y = coord.row * cell

        // Alternating dark cell colors
        val cellColor = if (i % 2 == 0) DarkGridCell else DarkAlternateCell

        drawRect(
            color = cellColor,
            topLeft = Offset(x, y),
            size = Size(cell, cell)
        )
        // #34435A Grid border
        drawRect(
            color = gridBorderColor,
            topLeft = Offset(x, y),
            size = Size(cell, cell),
            style = Stroke(width = hairline)
        )
    }

    // 3. Draw Starting Cells (Solid player colors with safe stars)
    fun drawStartCell(index: Int, color: Color) {
        val coord = trackIndices[index]
        val x = coord.col * cell
        val y = coord.row * cell

        // Solid player color cell
        drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(cell, cell)
        )
        // Grid border
        drawRect(
            color = gridBorderColor,
            topLeft = Offset(x, y),
            size = Size(cell, cell),
            style = Stroke(width = hairline)
        )
        // Star on starting cell (bright white/cream star with dark outline)
        drawBoardStar(
            centerX = (coord.col + 0.5f) * cell,
            centerY = (coord.row + 0.5f) * cell,
            radius = cell * 0.34f,
            color = Color.White
        )
    }

    drawStartCell(0, LudoRed)
    drawStartCell(13, LudoGreen)
    drawStartCell(26, LudoYellow)
    drawStartCell(39, LudoBlue)

    // 4. Draw Safe Stars on the 4 track safe tiles (Indices: 8, 21, 34, 47)
    val trackSafeIndices = listOf(8, 21, 34, 47)
    for (safeIdx in trackSafeIndices) {
        val coord = trackIndices[safeIdx]
        val x = coord.col * cell
        val y = coord.row * cell

        // Subtle highlight tile backing for safe star
        drawRect(
            color = DarkAlternateCell,
            topLeft = Offset(x, y),
            size = Size(cell, cell)
        )
        drawRect(
            color = gridBorderColor,
            topLeft = Offset(x, y),
            size = Size(cell, cell),
            style = Stroke(width = hairline)
        )

        // Safe star in rich gold #FFC928
        drawBoardStar(
            centerX = (coord.col + 0.5f) * cell,
            centerY = (coord.row + 0.5f) * cell,
            radius = cell * 0.34f,
            color = StarGold
        )
    }

    // 5. Draw Solid Colored Home Run Corridors (5 cells each)
    fun drawHomeLane(coords: List<GridCoord>, color: Color) {
        for (coord in coords) {
            val x = coord.col * cell
            val y = coord.row * cell
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(cell, cell)
            )
            drawRect(
                color = gridBorderColor,
                topLeft = Offset(x, y),
                size = Size(cell, cell),
                style = Stroke(width = hairline)
            )
        }
    }

    drawHomeLane(BoardCoordinates.RED_HOME, LudoRed)
    drawHomeLane(BoardCoordinates.GREEN_HOME, LudoGreen)
    drawHomeLane(BoardCoordinates.YELLOW_HOME, LudoYellow)
    drawHomeLane(BoardCoordinates.BLUE_HOME, LudoBlue)

    // 6. Draw Central Home Area with 4 Accurately Aligned Coloured Triangles
    val cx = 7.5f * cell
    val cy = 7.5f * cell
    val goalLeft = 6f * cell
    val goalRight = 9f * cell
    val goalTop = 6f * cell
    val goalBottom = 9f * cell

    // Center base surface (#101A2C)
    drawRect(
        color = DarkBoardBackground,
        topLeft = Offset(goalLeft, goalTop),
        size = Size(cell * 3, cell * 3)
    )

    // Red Triangle (Left pointing to center)
    val redPath = Path().apply {
        moveTo(goalLeft, goalTop)
        lineTo(cx, cy)
        lineTo(goalLeft, goalBottom)
        close()
    }
    drawPath(redPath, LudoRed, style = Fill)

    // Green Triangle (Top pointing to center)
    val greenPath = Path().apply {
        moveTo(goalLeft, goalTop)
        lineTo(goalRight, goalTop)
        lineTo(cx, cy)
        close()
    }
    drawPath(greenPath, LudoGreen, style = Fill)

    // Yellow Triangle (Right pointing to center)
    val yellowPath = Path().apply {
        moveTo(goalRight, goalTop)
        lineTo(goalRight, goalBottom)
        lineTo(cx, cy)
        close()
    }
    drawPath(yellowPath, LudoYellow, style = Fill)

    // Blue Triangle (Bottom pointing to center)
    val bluePath = Path().apply {
        moveTo(goalLeft, goalBottom)
        lineTo(cx, cy)
        lineTo(goalRight, goalBottom)
        close()
    }
    drawPath(bluePath, LudoBlue, style = Fill)

    // Clean boundary and diagonal grid lines through center
    val centerStroke = 1.5.dp.toPx()
    drawLine(gridBorderColor, Offset(goalLeft, goalTop), Offset(goalRight, goalBottom), strokeWidth = centerStroke)
    drawLine(gridBorderColor, Offset(goalLeft, goalBottom), Offset(goalRight, goalTop), strokeWidth = centerStroke)

    // Center box outline
    drawRect(
        color = gridBorderColor,
        topLeft = Offset(goalLeft, goalTop),
        size = Size(cell * 3, cell * 3),
        style = Stroke(width = centerStroke)
    )

    // Center Brass Star / Compass Junction Emblem
    drawCenterVictoryEmblem(cx, cy, cell * 0.48f)
}

/**
 * Renders an authentic dark Ludo home yard:
 * - Solid colored frame in player's traditional color (#F04444, #19B96B, #FFC928, #398BFF).
 * - Dark tinted inner background surface (#24141B, #12261E, #272214, #132034).
 * - Dark recessed panel card (#141F33 with subtle tint).
 * - 4 circular recessed pedestals with dark socket discs (zero pure white).
 */
private fun DrawScope.drawDarkYard(
    left: Float,
    top: Float,
    size: Float,
    cell: Float,
    playerColor: Color,
    surfaceTint: Color,
    slots: List<GridCoord>
) {
    val hairline = 1.dp.toPx()

    // 1. Yard outer tinted base
    drawRoundRect(
        color = surfaceTint,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )

    // Yard solid colored outer boundary stroke
    drawRoundRect(
        color = playerColor,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    // 2. Dark recessed inner card (replacing pure white card)
    val innerMargin = cell * 0.85f
    val innerSize = size - (innerMargin * 2)
    val innerCardLeft = left + innerMargin
    val innerCardTop = top + innerMargin

    // Subtle dark gradient panel
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(DarkGridCell, Color(0xFF141F33)),
            startY = innerCardTop,
            endY = innerCardTop + innerSize
        ),
        topLeft = Offset(innerCardLeft, innerCardTop),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = playerColor.copy(alpha = 0.4f),
        topLeft = Offset(innerCardLeft, innerCardTop),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = hairline)
    )

    // 3. Four recessed circular pedestals located at token slots
    for (slot in slots) {
        val pedestalCx = (slot.col + 0.5f) * cell
        val pedestalCy = (slot.row + 0.5f) * cell

        // Outer collar ring in player color
        drawCircle(
            color = playerColor,
            radius = cell * 0.44f,
            center = Offset(pedestalCx, pedestalCy)
        )
        // Dark recessed socket disc (#101A2C with subtle inner depth)
        drawCircle(
            color = DarkBoardBackground,
            radius = cell * 0.38f,
            center = Offset(pedestalCx, pedestalCy)
        )
        // Concentric target ring in player color
        drawCircle(
            color = playerColor.copy(alpha = 0.5f),
            radius = cell * 0.22f,
            center = Offset(pedestalCx, pedestalCy),
            style = Stroke(width = 1.dp.toPx())
        )
        // Center jewel pin
        drawCircle(
            color = playerColor,
            radius = cell * 0.08f,
            center = Offset(pedestalCx, pedestalCy)
        )
    }
}

/**
 * Draws a sharp 5-pointed star in pure vector mathematics.
 */
private fun DrawScope.drawBoardStar(
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: Color
) {
    val path = Path()
    val innerRadius = radius * 0.40f
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
    // Dark hairline border around star for crisp contrast against any background
    drawPath(path, Color.Black.copy(alpha = 0.35f), style = Stroke(width = 0.8.dp.toPx()))
}

/**
 * Draws the central brass victory hub emblem at the confluence of the 4 home triangles.
 */
private fun DrawScope.drawCenterVictoryEmblem(
    cx: Float,
    cy: Float,
    radius: Float
) {
    // Outer brass disc
    drawCircle(
        color = Color(0xFF101A2C),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = StarGold,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 8-point gold compass star inside
    val path = Path()
    val points = 8
    val innerRadius = radius * 0.42f
    for (i in 0 until points * 2) {
        val angle = (i * PI / points).toFloat() - (PI / 2).toFloat()
        val r = if (i % 2 == 0) radius * 0.82f else innerRadius
        val px = cx + cos(angle) * r
        val py = cy + sin(angle) * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, StarGold, style = Fill)
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius * 0.16f,
        center = Offset(cx, cy)
    )
}
