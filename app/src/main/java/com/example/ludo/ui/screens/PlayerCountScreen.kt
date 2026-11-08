package com.example.ludo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

data class PlayerCountOption(
    val count: Int,
    val title: String,
    val subtitle: String,
    val activeQuadrants: List<PlayerQuadrant>
)

enum class PlayerQuadrant(val color: Color) {
    TOP_LEFT(LudoRed),
    TOP_RIGHT(LudoGreen),
    BOTTOM_RIGHT(LudoYellow),
    BOTTOM_LEFT(LudoBlue)
}

@Composable
fun PlayerCountScreen(
    initialCount: Int = 4,
    onBackClick: () -> Unit,
    onContinue: (Int) -> Unit
) {
    var selectedCount by remember { mutableIntStateOf(initialCount) }

    val options = listOf(
        PlayerCountOption(
            count = 2,
            title = "2 Players",
            subtitle = "Head-to-head duel across opposing corners",
            activeQuadrants = listOf(PlayerQuadrant.TOP_LEFT, PlayerQuadrant.BOTTOM_RIGHT)
        ),
        PlayerCountOption(
            count = 3,
            title = "3 Players",
            subtitle = "Triangular skirmish across three quadrants",
            activeQuadrants = listOf(PlayerQuadrant.TOP_LEFT, PlayerQuadrant.TOP_RIGHT, PlayerQuadrant.BOTTOM_RIGHT)
        ),
        PlayerCountOption(
            count = 4,
            title = "4 Players",
            subtitle = "Classic tournament battle across all four zones",
            activeQuadrants = listOf(
                PlayerQuadrant.TOP_LEFT,
                PlayerQuadrant.TOP_RIGHT,
                PlayerQuadrant.BOTTOM_RIGHT,
                PlayerQuadrant.BOTTOM_LEFT
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 20.dp)
            .testTag("player_count_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top App Bar
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfacePanel)
                        .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                        .testTag("player_count_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Match Setup",
                        style = Typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Pass & Play • Local Match",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Player Count",
                style = Typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose the number of contenders participating in this match.",
                style = Typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Player count cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    val isSelected = selectedCount == option.count
                    val borderColor = if (isSelected) AccentPrimary else BorderHairline
                    val bgColor = if (isSelected) SurfaceElevated else SurfacePanel

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { selectedCount = option.count }
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("option_count_${option.count}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Miniature Board Layout Diagram
                        MiniBoardQuadrantDiagram(
                            activeQuadrants = option.activeQuadrants,
                            isSelected = isSelected,
                            modifier = Modifier.size(46.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = option.title,
                                    style = Typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) AccentPrimary else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Subtle badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color(0xFF080F22))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${option.count}P",
                                        style = Typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) AccentPrimary else TextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = option.subtitle,
                                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Radio check indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentPrimary else Color(0xFF080F22))
                            .border(1.dp, if (isSelected) AccentPrimary else BorderHairline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF080F22),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Continue button
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Button(
                onClick = { onContinue(selectedCount) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = Color(0xFF080F22)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_to_customization_button")
            ) {
                Text(
                    text = "Continue: Names & Colors",
                    style = Typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Miniature 4-quadrant board schematic showing which corners are in play.
 */
@Composable
private fun MiniBoardQuadrantDiagram(
    activeQuadrants: List<PlayerQuadrant>,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val gap = 3.dp.toPx()
        val cornerRadius = 3.dp.toPx()
        val quadW = (w - gap) / 2f
        val quadH = (h - gap) / 2f

        // Board background outline
        drawRoundRect(
            color = if (isSelected) Color(0xFF152441) else Color(0xFF080F22),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cornerRadius * 1.5f, cornerRadius * 1.5f)
        )

        // 4 Quadrants
        // Top Left
        val tlActive = activeQuadrants.contains(PlayerQuadrant.TOP_LEFT)
        drawRoundRect(
            color = if (tlActive) PlayerQuadrant.TOP_LEFT.color else Color(0xFF1D2C43),
            topLeft = Offset(0f, 0f),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Top Right
        val trActive = activeQuadrants.contains(PlayerQuadrant.TOP_RIGHT)
        drawRoundRect(
            color = if (trActive) PlayerQuadrant.TOP_RIGHT.color else Color(0xFF1D2C43),
            topLeft = Offset(quadW + gap, 0f),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Bottom Left
        val blActive = activeQuadrants.contains(PlayerQuadrant.BOTTOM_LEFT)
        drawRoundRect(
            color = if (blActive) PlayerQuadrant.BOTTOM_LEFT.color else Color(0xFF1D2C43),
            topLeft = Offset(0f, quadH + gap),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Bottom Right
        val brActive = activeQuadrants.contains(PlayerQuadrant.BOTTOM_RIGHT)
        drawRoundRect(
            color = if (brActive) PlayerQuadrant.BOTTOM_RIGHT.color else Color(0xFF1D2C43),
            topLeft = Offset(quadW + gap, quadH + gap),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Center cross indicator
        drawCircle(
            color = if (isSelected) AccentPrimary else Color(0xFF34435A),
            radius = 3.dp.toPx(),
            center = Offset(w / 2f, h / 2f)
        )
    }
}

