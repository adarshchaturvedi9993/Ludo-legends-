package com.example.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldRimGradientColors
import com.example.ui.theme.Typography

/**
 * Standard top navigation bar matching the Veeru dark navy & gold rim aesthetic.
 */
@Composable
fun GameTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    subtitle: String? = null,
    crownPrefix: Boolean = true,
    actions: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Slot: Back Button (or empty placeholder for balance)
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1D38))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(GoldRimGradientColors),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = AccentPrimary),
                        onClick = onBackClick
                    )
                    .testTag("top_bar_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }

        // Center Slot: Crown pill with title
        Row(
            modifier = Modifier
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF0A1428))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)),
                    shape = RoundedCornerShape(21.dp)
                )
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (crownPrefix) {
                Text(text = "👑", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title.uppercase(),
                style = Typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = AccentPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $subtitle",
                    style = Typography.bodyMedium.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Right Slot: Optional action or spacer
        if (actions != null) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                actions()
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}
