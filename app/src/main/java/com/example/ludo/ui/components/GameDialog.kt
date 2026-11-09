package com.example.ludo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldRimGradientColors
import com.example.ui.theme.MotionSpec
import com.example.ui.theme.Typography

/**
 * Universal game dialog replacing standard Android AlertDialogs with the
 * dark navy (#101B33 / #152441) and royal gold styling.
 */
@Composable
fun GameDialog(
    visible: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    crownPrefix: Boolean = true,
    buttons: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(MotionSpec.springBouncy()) + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + scaleOut(targetScale = 0.92f)
        ) {
            Box(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(max = 420.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D172E))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(GoldRimGradientColors),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(22.dp)
                    .testTag("game_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Pill / Title
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF091122))
                            .border(1.dp, AccentPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (crownPrefix) {
                            Text("👑", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = title.uppercase(),
                            style = Typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            ),
                            color = AccentPrimary
                        )
                    }

                    // Dialog Body Content
                    content()

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons Row / Column
                    buttons()
                }
            }
        }
    }
}
