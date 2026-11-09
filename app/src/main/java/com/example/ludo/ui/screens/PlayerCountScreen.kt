package com.example.ludo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.GlassCard
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.TextMuted
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

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("player_count_screen"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                GameTopBar(
                    title = "Player Count",
                    subtitle = "Pass & Play",
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SELECT PLAYERS",
                    style = Typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose how many contenders will participate locally.",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { option ->
                        val isSelected = selectedCount == option.count

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            isActive = isSelected,
                            activeBorderColor = AccentPrimary,
                            onClick = { selectedCount = option.count }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                            style = Typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                            color = if (isSelected) AccentPrimary else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else Color(0xFF080F22))
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
            }

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                GameButton(
                    text = "Continue: Names & Colors",
                    onClick = { onContinue(selectedCount) },
                    style = GameButtonStyle.PRIMARY_GOLD,
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    testTag = "continue_to_customization_button"
                )
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ludo.ads.BannerAdView(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_LOBBY,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = Color(0xFF0D1D38),
                    containerBorderColor = Color(0xFF293B5B)
                )
            }
        }
    }
}

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

        drawRoundRect(
            color = if (isSelected) Color(0xFF152441) else Color(0xFF080F22),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(cornerRadius * 1.5f, cornerRadius * 1.5f)
        )

        val tlActive = activeQuadrants.contains(PlayerQuadrant.TOP_LEFT)
        drawRoundRect(
            color = if (tlActive) PlayerQuadrant.TOP_LEFT.color else Color(0xFF1D2C43),
            topLeft = Offset(0f, 0f),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val trActive = activeQuadrants.contains(PlayerQuadrant.TOP_RIGHT)
        drawRoundRect(
            color = if (trActive) PlayerQuadrant.TOP_RIGHT.color else Color(0xFF1D2C43),
            topLeft = Offset(quadW + gap, 0f),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val blActive = activeQuadrants.contains(PlayerQuadrant.BOTTOM_LEFT)
        drawRoundRect(
            color = if (blActive) PlayerQuadrant.BOTTOM_LEFT.color else Color(0xFF1D2C43),
            topLeft = Offset(0f, quadH + gap),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val brActive = activeQuadrants.contains(PlayerQuadrant.BOTTOM_RIGHT)
        drawRoundRect(
            color = if (brActive) PlayerQuadrant.BOTTOM_RIGHT.color else Color(0xFF1D2C43),
            topLeft = Offset(quadW + gap, quadH + gap),
            size = Size(quadW, quadH),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        drawCircle(
            color = if (isSelected) AccentPrimary else Color(0xFF34435A),
            radius = 3.dp.toPx(),
            center = Offset(w / 2f, h / 2f)
        )
    }
}
