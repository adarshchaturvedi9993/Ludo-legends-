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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GridCoord
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
import com.example.ui.theme.DarkAlternateCell
import com.example.ui.theme.DarkBoardBackground
import com.example.ui.theme.DarkGridBorder
import com.example.ui.theme.DarkGridCell
import com.example.ui.theme.GoldRimGradientColors
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
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Premium Commercial Dark Ludo Board:
 *
 * Requirements:
 * - Direct board pawn selection (NO bottom sheet popup).
 * - Movable pawns get pulsing glow ring, scale pulse (1.0 -> 1.12), and bouncing chevron arrow above.
 * - Soft destination-tile preview: ghost pawn + highlighted target cell + dotted path.
 * - 48dp minimum touch target for accessibility and error-free pawn tapping.
 * - Layered gradients, safe stars, and central brass star victory hub.
 */
@Composable
fun LudoBoardView(
    players: List<Player>,
    currentPlayer: Player,
    movableTokenIds: Set<Int>,
    diceValue: Int,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    animatingTokenId: Int? = null,
    animatingTokenCoord: GridCoord? = null,
    animatingHopElevationPx: Float = 0f,
    showColorBlindSymbol: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkBoardBackground)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(GoldRimGradientColors),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("ludo_board_view")
    ) {
        val boardSizePx = constraints.maxWidth.toFloat()
        val cellSizePx = boardSizePx / BoardCoordinates.GRID_SIZE

        // 1. Draw Static Dark Board Geometry
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDarkLudoBoard(cellSizePx)
        }

        // 2. Compute destination tile previews and dotted paths for movable pawns
        val movableTokens = remember(currentPlayer, movableTokenIds, diceValue) {
            currentPlayer.tokens.filter { movableTokenIds.contains(it.id) }
        }

        val destinationPreviews = remember(movableTokens, diceValue) {
            movableTokens.mapNotNull { token ->
                val path = BoardCoordinates.computeHopPath(token, diceValue)
                if (path.size > 1) {
                    Triple(token, path.last(), path)
                } else null
            }
        }

        // Draw destination highlights & dotted paths on board
        if (animatingTokenId == null && movableTokenIds.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dottedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                val previewPlayerColor = currentPlayer.color.primaryColor

                destinationPreviews.forEach { (token, destCoord, pathCoords) ->
                    val destX = (destCoord.col + 0.5f) * cellSizePx
                    val destY = (destCoord.row + 0.5f) * cellSizePx

                    // Highlight target destination cell with soft glow
                    drawRoundRect(
                        color = previewPlayerColor.copy(alpha = 0.22f),
                        topLeft = Offset(destCoord.col * cellSizePx, destCoord.row * cellSizePx),
                        size = Size(cellSizePx, cellSizePx),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = StarGold.copy(alpha = 0.75f),
                        topLeft = Offset(destCoord.col * cellSizePx + 1.dp.toPx(), destCoord.row * cellSizePx + 1.dp.toPx()),
                        size = Size(cellSizePx - 2.dp.toPx(), cellSizePx - 2.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 1.6.dp.toPx(), pathEffect = dottedEffect)
                    )

                    // Dotted connection line from token to destination
                    if (pathCoords.size > 2) {
                        val path = Path()
                        pathCoords.forEachIndexed { idx, coord ->
                            val px = (coord.col + 0.5f) * cellSizePx
                            val py = (coord.row + 0.5f) * cellSizePx
                            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        drawPath(
                            path = path,
                            color = previewPlayerColor.copy(alpha = 0.55f),
                            style = Stroke(width = 2.dp.toPx(), pathEffect = dottedEffect)
                        )
                    }
                }
            }
        }

        // 3. Render Ghost Pawns at destination tiles
        val density = LocalDensity.current
        if (animatingTokenId == null && movableTokenIds.isNotEmpty()) {
            destinationPreviews.forEach { (token, destCoord, _) ->
                val destX = (destCoord.col + 0.5f) * cellSizePx
                val destY = (destCoord.row + 0.5f) * cellSizePx
                val pawnSizePx = cellSizePx * 0.88f
                val pawnSizeDp = with(density) { pawnSizePx.toDp() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (destX - pawnSizePx / 2f).roundToInt(),
                                y = (destY - pawnSizePx / 2f).roundToInt()
                            )
                        }
                        .size(pawnSizeDp),
                    contentAlignment = Alignment.Center
                ) {
                    TurnedPawn(
                        color = token.color,
                        isMovable = false,
                        isFinished = false,
                        size = pawnSizeDp,
                        isGhost = true
                    )
                }
            }
        }

        // 4. Render Pawns with direct touch hitboxes (minimum 48dp)
        val allTokens = players.flatMap { it.tokens }
        val tokensByCoord = allTokens.groupBy { token ->
            val coord = if (token.id == animatingTokenId && animatingTokenCoord != null) {
                animatingTokenCoord
            } else {
                BoardCoordinates.getCoordinate(token)
            }
            Pair((coord.row * 10).roundToInt(), (coord.col * 10).roundToInt())
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pawnPulse")
        val pulseScale = infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tokenPulse"
        )

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

                val (offsetX, offsetY) = if (clusterSize > 1 && token.state != TokenState.YARD && !isAnimatingThisToken) {
                    val angle = (2.0 * PI * indexInCluster / clusterSize).toFloat()
                    val radius = cellSizePx * 0.22f
                    Pair(cos(angle) * radius, sin(angle) * radius)
                } else {
                    Pair(0f, 0f)
                }

                val centerX = (coord.col + 0.5f) * cellSizePx + offsetX
                val centerY = (coord.row + 0.5f) * cellSizePx + offsetY

                val pawnSizePx = if (token.state == TokenState.YARD) cellSizePx * 0.94f else cellSizePx * 0.92f
                val pawnSizeDp = with(density) { pawnSizePx.toDp() }

                // Minimum 48dp touch hitbox
                val touchHitboxDp = max(48f, pawnSizeDp.value).dp
                val touchHitboxPx = with(density) { touchHitboxDp.toPx() }

                val hopElevation = if (isAnimatingThisToken) animatingHopElevationPx else 0f

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (centerX - touchHitboxPx / 2f).roundToInt(),
                                y = (centerY - touchHitboxPx / 2f).roundToInt()
                            )
                        }
                        .size(touchHitboxDp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = if (isMovable) ripple(bounded = false, radius = touchHitboxDp / 2) else null,
                            enabled = isMovable,
                            onClick = { onTokenClick(token.id) }
                        )
                        .testTag("pawn_${player.color.name}_${token.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(pawnSizeDp)
                            .scale(if (isMovable) pulseScale.value else 1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        TurnedPawn(
                            color = player.color,
                            isMovable = isMovable,
                            isFinished = token.isFinished,
                            size = pawnSizeDp,
                            hopElevationPx = hopElevation,
                            showColorBlindSymbol = showColorBlindSymbol
                        )
                    }

                    // Multi-token count badge if clustered and more than 1
                    if (clusterSize > 1 && indexInCluster == 0 && token.state != TokenState.YARD && !isAnimatingThisToken) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A1428))
                                .border(1.dp, StarGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = clusterSize.toString(),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = StarGold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the authentic dark Ludo board geometry with high visual fidelity.
 */
private fun DrawScope.drawDarkLudoBoard(cell: Float) {
    val hairline = 1.dp.toPx()
    val gridBorderColor = DarkGridBorder

    // 1. Draw Corner Yards (6x6 cells)
    drawDarkYard(0f, 0f, cell * 6, cell, LudoRed, LudoRedHomeSurface, BoardCoordinates.RED_YARD_SLOTS)
    drawDarkYard(cell * 9, 0f, cell * 6, cell, LudoGreen, LudoGreenHomeSurface, BoardCoordinates.GREEN_YARD_SLOTS)
    drawDarkYard(cell * 9, cell * 9, cell * 6, cell, LudoYellow, LudoYellowHomeSurface, BoardCoordinates.YELLOW_YARD_SLOTS)
    drawDarkYard(0f, cell * 9, cell * 6, cell, LudoBlue, LudoBlueHomeSurface, BoardCoordinates.BLUE_YARD_SLOTS)

    // 2. Draw 52 Perimeter Track Cells
    val trackIndices = BoardCoordinates.TRACK_TILES
    for (i in trackIndices.indices) {
        val coord = trackIndices[i]
        val x = coord.col * cell
        val y = coord.row * cell
        val cellColor = if (i % 2 == 0) DarkGridCell else DarkAlternateCell

        drawRect(cellColor, Offset(x, y), Size(cell, cell))
        drawRect(gridBorderColor, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
    }

    // 3. Draw Starting Cells
    fun drawStartCell(index: Int, color: Color) {
        val coord = trackIndices[index]
        val x = coord.col * cell
        val y = coord.row * cell

        drawRect(color, Offset(x, y), Size(cell, cell))
        drawRect(gridBorderColor, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        drawBoardStar((coord.col + 0.5f) * cell, (coord.row + 0.5f) * cell, cell * 0.34f, Color.White)
    }

    drawStartCell(0, LudoRed)
    drawStartCell(13, LudoGreen)
    drawStartCell(26, LudoYellow)
    drawStartCell(39, LudoBlue)

    // 4. Draw Safe Stars on track tiles (8, 21, 34, 47)
    listOf(8, 21, 34, 47).forEach { safeIdx ->
        val coord = trackIndices[safeIdx]
        val x = coord.col * cell
        val y = coord.row * cell

        drawRect(DarkAlternateCell, Offset(x, y), Size(cell, cell))
        drawRect(gridBorderColor, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        drawBoardStar((coord.col + 0.5f) * cell, (coord.row + 0.5f) * cell, cell * 0.34f, StarGold)
    }

    // 5. Draw Solid Colored Home Run Corridors (5 cells each)
    fun drawHomeLane(coords: List<GridCoord>, color: Color) {
        for (coord in coords) {
            val x = coord.col * cell
            val y = coord.row * cell
            drawRect(color, Offset(x, y), Size(cell, cell))
            drawRect(gridBorderColor, Offset(x, y), Size(cell, cell), style = Stroke(width = hairline))
        }
    }

    drawHomeLane(BoardCoordinates.RED_HOME, LudoRed)
    drawHomeLane(BoardCoordinates.GREEN_HOME, LudoGreen)
    drawHomeLane(BoardCoordinates.YELLOW_HOME, LudoYellow)
    drawHomeLane(BoardCoordinates.BLUE_HOME, LudoBlue)

    // 6. Central 4-Color Victory Confluence
    val cx = 7.5f * cell
    val cy = 7.5f * cell
    val goalLeft = 6f * cell
    val goalRight = 9f * cell
    val goalTop = 6f * cell
    val goalBottom = 9f * cell

    drawRect(DarkBoardBackground, Offset(goalLeft, goalTop), Size(cell * 3, cell * 3))

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

    val centerStroke = 1.5.dp.toPx()
    drawLine(gridBorderColor, Offset(goalLeft, goalTop), Offset(goalRight, goalBottom), strokeWidth = centerStroke)
    drawLine(gridBorderColor, Offset(goalLeft, goalBottom), Offset(goalRight, goalTop), strokeWidth = centerStroke)
    drawRect(gridBorderColor, Offset(goalLeft, goalTop), Size(cell * 3, cell * 3), style = Stroke(width = centerStroke))

    // Center Gold Star Hub Emblem
    drawCenterVictoryEmblem(cx, cy, cell * 0.48f)
}

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

    drawRoundRect(
        color = surfaceTint,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
    drawRoundRect(
        color = playerColor,
        topLeft = Offset(left, top),
        size = Size(size, size),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )

    val innerMargin = cell * 0.85f
    val innerSize = size - (innerMargin * 2)
    val innerCardLeft = left + innerMargin
    val innerCardTop = top + innerMargin

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

    for (slot in slots) {
        val pedestalCx = (slot.col + 0.5f) * cell
        val pedestalCy = (slot.row + 0.5f) * cell

        drawCircle(playerColor, cell * 0.44f, Offset(pedestalCx, pedestalCy))
        drawCircle(DarkBoardBackground, cell * 0.38f, Offset(pedestalCx, pedestalCy))
        drawCircle(playerColor.copy(alpha = 0.5f), cell * 0.22f, Offset(pedestalCx, pedestalCy), style = Stroke(width = 1.dp.toPx()))
        drawCircle(playerColor, cell * 0.08f, Offset(pedestalCx, pedestalCy))
    }
}

private fun DrawScope.drawBoardStar(centerX: Float, centerY: Float, radius: Float, color: Color) {
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
    drawPath(path, Color.Black.copy(alpha = 0.35f), style = Stroke(width = 0.8.dp.toPx()))
}

private fun DrawScope.drawCenterVictoryEmblem(cx: Float, cy: Float, radius: Float) {
    drawCircle(Color(0xFF101A2C), radius, Offset(cx, cy))
    drawCircle(StarGold, radius, Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))

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
    drawCircle(Color.White.copy(alpha = 0.9f), radius * 0.16f, Offset(cx, cy))
}
