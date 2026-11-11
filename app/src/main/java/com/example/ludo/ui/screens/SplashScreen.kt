package com.example.ludo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.94f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        delay(200)
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )
        onSplashFinished()
    }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value)
            ) {
                // Game Icon Emblem
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(
                            width = 2.dp,
                            color = AccentPrimary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_icon),
                        contentDescription = "Ludo Legends",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "LUDO LEGENDS",
                    style = Typography.displayMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.0.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "CLASSIC BOARD TOURNAMENT",
                    style = Typography.labelSmall.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AccentPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Clean Loading Progress Bar
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(fraction = progressAnim.value)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "LOADING ARENA...",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.0.sp
                    ),
                    color = TextMuted
                )
            }

            // Bottom Attribution Footer
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(alphaAnim.value)
            ) {
                AppFooter()
            }
        }
    }
}
