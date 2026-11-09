package com.example.ludo.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.SatoshiFontFamily
import com.example.ui.theme.TextMuted

/**
 * Premium Developer Credits Footer.
 * Displayed at the bottom of Quick Rules and Settings screens.
 */
@Composable
fun AppFooter(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val instagramUrl = "https://www.instagram.com/adarshchaturvedi9_/"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 24.dp)
            .testTag("developer_credits_footer"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "All Rights Reserved By Virgees Dev.",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.6.sp,
            color = AccentPrimary.copy(alpha = 0.95f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Game By Adarsh Chaturvedi (Lucky)",
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.3.sp,
            color = TextMuted.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 18.dp),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback gracefully if no browser/activity available
                        }
                    }
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("instagram_profile_link"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            InstagramIcon(
                modifier = Modifier.size(14.dp),
                color = AccentPrimary
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "@adarshchaturvedi9_",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.4.sp,
                color = AccentPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Crisp vector-rendered Instagram glyph using gold accent stroke.
 */
@Composable
private fun InstagramIcon(
    modifier: Modifier = Modifier,
    color: Color = AccentPrimary
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

        // Outer rounded rect
        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeWidth)
        )

        // Center lens circle
        val center = Offset(size.width / 2, size.height / 2)
        val lensRadius = (size.width / 2) * 0.42f
        drawCircle(
            color = color,
            radius = lensRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Top-right flash dot
        val dotRadius = 0.9.dp.toPx()
        val dotCenter = Offset(size.width * 0.76f, size.height * 0.24f)
        drawCircle(
            color = color,
            radius = dotRadius,
            center = dotCenter
        )
    }
}

