package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Unified Motion Spec for Ludo Legends:
 * Fast 120ms, Standard 260ms, Emphasized 420ms
 * Smooth spring bounces and tactile easing curves.
 */
object MotionSpec {
    const val DurationFast = 120
    const val DurationStandard = 260
    const val DurationEmphasized = 420
    const val DurationRollPhysics = 540
    const val DurationHopStep = 140

    val EasingFastOutSlow: Easing = FastOutSlowInEasing
    val EasingSmoothDecel: Easing = LinearOutSlowInEasing
    val EasingBackOut: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

    fun <T> springBouncy() = spring<T>(
        dampingRatio = 0.70f,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> springSnappy() = spring<T>(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMediumLow
    )
}
