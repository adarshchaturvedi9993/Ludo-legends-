package com.example.ludo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.BoardCoordinates
import com.example.ludo.model.GridCoord
import com.example.ludo.model.PlayerColor
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.Typography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Data holder for active capture visual animation.
 * Total duration: ~1.2s
 */
data class ActiveCaptureAnimation(
    val attackerColor: PlayerColor,
    val victimColor: PlayerColor,
    val victimTokenId: Int,
    val startCoord: GridCoord,
    val yardSlotCoord: GridCoord,
    val progress: Float = 0f, // 0..1
    val isFinished: Boolean = false
)

/**
 * Data holder for winning pawn finish celebration.
 */
data class ActiveFinishAnimation(
    val playerColor: PlayerColor,
    val tokenId: Int,
    val targetSlotCoord: GridCoord,
    val finishOrder: Int,
    val isFourthPawn: Boolean = false,
    val progress: Float = 0f, // 0..1
    val isFinished: Boolean = false
)

/**
 * Floating toast notification event during match.
 */
data class GameToastNotification(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val subtitle: String? = null,
    val accentColor: Color = StarGold,
    val icon: ImageVector? = null
)

/**
 * Renders the ~1.2s capture effects:
 * - White hit flash
 * - Double shockwave in attacker's color
 * - Radial sparks and victim-colored shards
 * - Victim pawn squash & parabolic spin arc to yard slot
 * - Landing ripple at yard slot
 */
@Composable
fun CaptureAnimationOverlay(
    capture: ActiveCaptureAnimation?,
    cellSizePx: Float,
    modifier: Modifier = Modifier
) {
    if (capture == null || capture.isFinished) return
    val progress = capture.progress.coerceIn(0f, 1f)
    val density = LocalDensity.current

    // Phase 1: 0.0 -> 0.25: Hit impact flash, shockwaves, sparks, squash
    // Phase 2: 0.25 -> 0.85: Parabolic arc flight to yard with 3D spin
    // Phase 3: 0.85 -> 1.00: Landing ripple at yard slot
    val startX = (capture.startCoord.col + 0.5f) * cellSizePx
    val startY = (capture.startCoord.row + 0.5f) * cellSizePx
    val targetX = (capture.yardSlotCoord.col + 0.5f) * cellSizePx
    val targetY = (capture.yardSlotCoord.row + 0.5f) * cellSizePx

    val attackerPrimary = capture.attackerColor.primaryColor
    val victimPrimary = capture.victimColor.primaryColor

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width

        // 1. White impact flash (0.00 -> 0.18)
        if (progress < 0.18f) {
            val flashAlpha = (1f - (progress / 0.18f)) * 0.75f
            drawCircle(
                color = Color.White.copy(alpha = flashAlpha),
                radius = cellSizePx * 1.4f,
                center = Offset(startX, startY)
            )
        }

        // 2. Double shockwave in attacker's color (0.05 -> 0.40)
        if (progress in 0.05f..0.45f) {
            val waveProgress = ((progress - 0.05f) / 0.40f)
            val waveRadius1 = waveProgress * cellSizePx * 2.2f
            val waveRadius2 = (waveProgress * 0.8f) * cellSizePx * 2.0f
            val waveAlpha = (1f - waveProgress) * 0.85f

            drawCircle(
                color = attackerPrimary.copy(alpha = waveAlpha),
                radius = waveRadius1,
                center = Offset(startX, startY),
                style = Stroke(width = 3.dp.toPx() * (1f - waveProgress))
            )
            drawCircle(
                color = StarGold.copy(alpha = waveAlpha * 0.7f),
                radius = waveRadius2,
                center = Offset(startX, startY),
                style = Stroke(width = 2.dp.toPx() * (1f - waveProgress))
            )
        }

        // 3. Radial sparks and victim-colored shards (0.05 -> 0.45)
        if (progress in 0.05f..0.45f) {
            val sparkProgress = ((progress - 0.05f) / 0.40f)
            val numSparks = 10
            for (i in 0 until numSparks) {
                val angle = (2 * PI * i / numSparks).toFloat()
                val dist = sparkProgress * cellSizePx * 1.6f
                val sx = startX + cos(angle) * dist
                val sy = startY + sin(angle) * dist
                val sparkColor = if (i % 2 == 0) victimPrimary else Color.White
                val sparkAlpha = (1f - sparkProgress) * 0.9f
                drawCircle(
                    color = sparkColor.copy(alpha = sparkAlpha),
                    radius = 2.5.dp.toPx() * (1f - sparkProgress),
                    center = Offset(sx, sy)
                )
            }
        }

        // 4. Landing ripple at yard slot (0.80 -> 1.00)
        if (progress >= 0.80f) {
            val landProgress = (progress - 0.80f) / 0.20f
            val rippleRadius = landProgress * cellSizePx * 1.5f
            val rippleAlpha = (1f - landProgress) * 0.8f
            drawCircle(
                color = victimPrimary.copy(alpha = rippleAlpha),
                radius = rippleRadius,
                center = Offset(targetX, targetY),
                style = Stroke(width = 2.5.dp.toPx() * (1f - landProgress))
            )
        }
    }

    // 5. Flying victim pawn in parabolic arc with squash and spin
    if (progress in 0.05f..0.98f) {
        val flightProgress = ((progress - 0.05f) / 0.93f).coerceIn(0f, 1f)

        // Parabolic arc interpolation
        val currentX = startX + (targetX - startX) * flightProgress
        val currentY = startY + (targetY - startY) * flightProgress
        // Peak elevation in center of flight (60px high)
        val arcElevation = sin(PI.toFloat() * flightProgress) * 85f

        // Squash at takeoff (flightProgress < 0.15), stretch during arc, spin 720°
        val squashX = if (flightProgress < 0.15f) 1.25f else 1.0f - (sin(PI.toFloat() * flightProgress) * 0.15f)
        val squashY = if (flightProgress < 0.15f) 0.70f else 1.0f + (sin(PI.toFloat() * flightProgress) * 0.20f)
        val spinDegrees = flightProgress * 720f

        val pawnSizePx = cellSizePx * 0.85f
        val pawnSizeDp = with(density) { pawnSizePx.toDp() }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (currentX - pawnSizePx / 2f).roundToInt(),
                        y = (currentY - pawnSizePx / 2f - arcElevation).roundToInt()
                    )
                }
                .size(pawnSizeDp)
                .rotate(spinDegrees)
                .scale(scaleX = squashX, scaleY = squashY),
            contentAlignment = Alignment.Center
        ) {
            TurnedPawn(
                color = capture.victimColor,
                isMovable = false,
                isFinished = false,
                size = pawnSizeDp,
                hopElevationPx = arcElevation
            )
        }
    }
}

/**
 * Renders the winning pawn finish celebration:
 * - Rotating golden rays and sparkles
 * - 3D spin into slot with bounce and glint
 * - Aura scale expansion
 */
@Composable
fun FinishAnimationOverlay(
    finishAnim: ActiveFinishAnimation?,
    cellSizePx: Float,
    modifier: Modifier = Modifier
) {
    if (finishAnim == null || finishAnim.isFinished) return
    val progress = finishAnim.progress.coerceIn(0f, 1f)
    val density = LocalDensity.current

    val slotCoord = finishAnim.targetSlotCoord
    val targetX = (slotCoord.col + 0.5f) * cellSizePx
    val targetY = (slotCoord.row + 0.5f) * cellSizePx

    val playerColor = finishAnim.playerColor

    Canvas(modifier = modifier.fillMaxSize()) {
        val auraRadius = cellSizePx * (0.8f + 0.5f * sin(progress * PI.toFloat()))
        val auraAlpha = (1f - (progress * 0.5f)).coerceIn(0.1f, 0.85f)

        // 1. Golden Aura Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    StarGold.copy(alpha = auraAlpha * 0.7f),
                    playerColor.primaryColor.copy(alpha = auraAlpha * 0.4f),
                    Color.Transparent
                ),
                center = Offset(targetX, targetY),
                radius = auraRadius
            ),
            radius = auraRadius,
            center = Offset(targetX, targetY)
        )

        // 2. Rotating Golden Rays (8 rays)
        val rayAngle = progress * 360f
        val numRays = 8
        val rayLen = cellSizePx * 1.2f
        for (i in 0 until numRays) {
            val angle = ((i * 360f / numRays) + rayAngle) * (PI.toFloat() / 180f)
            val rx = targetX + cos(angle) * rayLen
            val ry = targetY + sin(angle) * rayLen
            drawLine(
                color = StarGold.copy(alpha = (1f - progress) * 0.6f),
                start = Offset(targetX, targetY),
                end = Offset(rx, ry),
                strokeWidth = 2.dp.toPx() * (1f - progress)
            )
        }

        // 3. Starlight Sparkles (6 points)
        val numSparkles = 6
        for (i in 0 until numSparkles) {
            val sparkAngle = (i * 60f + progress * 180f) * (PI.toFloat() / 180f)
            val sparkDist = cellSizePx * (0.4f + progress * 0.6f)
            val sx = targetX + cos(sparkAngle) * sparkDist
            val sy = targetY + sin(sparkAngle) * sparkDist
            drawCircle(
                color = Color.White.copy(alpha = (1f - progress) * 0.9f),
                radius = 2.5.dp.toPx() * (1f - progress),
                center = Offset(sx, sy)
            )
        }
    }

    // 4. Pawn 3D spin into slot with bounce glint
    val spinDegrees = (1f - progress) * 360f
    val bounceScale = 1.0f + 0.35f * sin(progress * PI.toFloat())
    val pawnSizePx = cellSizePx * 0.55f
    val pawnSizeDp = with(density) { pawnSizePx.toDp() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (targetX - pawnSizePx / 2f).roundToInt(),
                    y = (targetY - pawnSizePx / 2f).roundToInt()
                )
            }
            .size(pawnSizeDp)
            .rotate(spinDegrees)
            .scale(bounceScale),
        contentAlignment = Alignment.Center
    ) {
        TurnedPawn(
            color = finishAnim.playerColor,
            isMovable = false,
            isFinished = true,
            size = pawnSizeDp
        )
    }
}

/**
 * Clean, modern floating toast component for big match moments.
 */
@Composable
fun GameToastBanner(
    toast: GameToastNotification?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier
    ) {
        if (toast != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.2.dp, toast.accentColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("game_toast_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (toast.icon != null) {
                        Icon(
                            imageVector = toast.icon,
                            contentDescription = null,
                            tint = toast.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = toast.title,
                            style = Typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = toast.accentColor
                        )
                        if (toast.subtitle != null) {
                            Text(
                                text = toast.subtitle,
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
