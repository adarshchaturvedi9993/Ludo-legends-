package com.example.ludo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.SurfaceBackground
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val alpha: Float,
    val phaseOffset: Float,
    val isGold: Boolean
)

/**
 * Atmospheric dark navy background canvas with subtle twinkling particle field
 * and peripheral vignette. Rendered consistently across all game screens.
 */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    enableTwinkle: Boolean = true,
    content: @Composable () -> Unit
) {
    val particles = remember {
        val rand = Random(42)
        List(40) {
            StarParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 1.6f + 0.8f,
                alpha = rand.nextFloat() * 0.45f + 0.15f,
                phaseOffset = rand.nextFloat() * 6.28f,
                isGold = rand.nextFloat() < 0.35f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val time = if (enableTwinkle) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        ).value
    } else 0f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Deep navy radial ambient gradient with dark peripheral vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1B38),
                        Color(0xFF0A1328),
                        SurfaceBackground
                    ),
                    center = Offset(w * 0.5f, h * 0.35f),
                    radius = maxOf(w, h) * 0.85f
                )
            )

            // 2. Subtle twinkling dust & stars
            particles.forEach { p ->
                val px = p.xRatio * w
                val py = p.yRatio * h
                val twinkle = if (enableTwinkle) {
                    0.6f + 0.4f * sin(time + p.phaseOffset)
                } else 1.0f
                val effectiveAlpha = (p.alpha * twinkle).coerceIn(0.05f, 0.90f)
                val dotColor = if (p.isGold) {
                    AccentPrimary.copy(alpha = effectiveAlpha)
                } else {
                    Color.White.copy(alpha = effectiveAlpha * 0.75f)
                }
                drawCircle(
                    color = dotColor,
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }
        }

        content()
    }
}
