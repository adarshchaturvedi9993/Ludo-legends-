package com.example.ludo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SurfaceBackground

/**
 * Premium tournament game table canvas:
 * Elegant ambient radial lighting centered on the play area,
 * creating a focused, distraction-free environment for tournament play.
 */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    enableTwinkle: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Smooth radial ambient lighting focused softly behind the center table
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF142038), // Soft center ambient glow
                        Color(0xFF0D1526), // Mid transition
                        SurfaceBackground  // Deep edge table base
                    ),
                    center = Offset(w * 0.5f, h * 0.40f),
                    radius = maxOf(w, h) * 0.85f
                )
            )
        }

        content()
    }
}
