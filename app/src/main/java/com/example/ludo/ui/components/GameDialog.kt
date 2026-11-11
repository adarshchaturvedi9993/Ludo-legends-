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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.MotionSpec
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.Typography

/**
 * Universal game modal dialog with crisp tournament styling:
 * Solid elevated background, clean typography, structured layout, and no emoji clutter.
 */
@Composable
fun GameDialog(
    visible: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    crownPrefix: Boolean = false, // Ignored: Crown emoji eliminated per UX quality directive
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
            enter = fadeIn(MotionSpec.springBouncy()) + scaleIn(initialScale = 0.94f),
            exit = fadeOut() + scaleOut(targetScale = 0.94f)
        ) {
            Box(
                modifier = modifier
                    .padding(20.dp)
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = BorderHairline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
                    .testTag("game_dialog"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Dialog Title
                    Text(
                        text = title,
                        style = Typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    // Dialog Body Content
                    content()

                    Spacer(modifier = Modifier.height(2.dp))

                    // Action Buttons
                    buttons()
                }
            }
        }
    }
}
