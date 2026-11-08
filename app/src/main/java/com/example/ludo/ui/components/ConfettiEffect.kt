package com.example.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.StarGold
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val wobbleSpeed: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val colors = listOf(LudoRed, LudoGreen, LudoYellow, LudoBlue, StarGold, Color.White)

    val particles = remember {
        List(50) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                speed = 0.6f + Random.nextFloat() * 0.7f,
                size = 6f + Random.nextFloat() * 8f,
                color = colors[Random.nextInt(colors.size)],
                wobbleSpeed = 2f + Random.nextFloat() * 4f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        for (p in particles) {
            val y = (t * p.speed * h * 1.2f) % (h + 40f) - 20f
            val wobble = kotlin.math.sin((t * p.wobbleSpeed * Math.PI).toFloat()) * 24f
            val x = (p.xRatio * w) + wobble

            drawRect(
                color = p.color.copy(alpha = (1f - (t * 0.3f)).coerceIn(0.2f, 1f)),
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 0.6f)
            )
        }
    }
}
