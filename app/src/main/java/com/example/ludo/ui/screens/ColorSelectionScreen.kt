package com.example.ludo.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Player Name and Color Selection Screen:
 * Ensures 100% color uniqueness with instant swap mechanics.
 */
@Composable
fun ColorSelectionScreen(
    playerCount: Int,
    initialNames: List<String>,
    onBackClick: () -> Unit,
    onStartGame: (List<Player>) -> Unit
) {
    val names = remember {
        mutableStateListOf(
            initialNames.getOrElse(0) { "Player 1" },
            initialNames.getOrElse(1) { "Player 2" },
            initialNames.getOrElse(2) { "Player 3" },
            initialNames.getOrElse(3) { "Player 4" }
        )
    }

    val defaultColors = when (playerCount) {
        2 -> listOf(PlayerColor.RED, PlayerColor.YELLOW)
        3 -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW)
        else -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
    }

    val selectedColors = remember {
        mutableStateListOf<PlayerColor>().apply {
            addAll(defaultColors.take(playerCount))
        }
    }

    val scrollState = rememberScrollState()
    val allPaletteColors = listOf(
        PlayerColor.RED,
        PlayerColor.GREEN,
        PlayerColor.YELLOW,
        PlayerColor.BLUE
    )

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("color_selection_screen")
        ) {
            GameTopBar(
                title = "Names & Colors",
                subtitle = "$playerCount Players",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CUSTOMIZE CONTENDERS",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                for (i in 0 until playerCount) {
                    val currentColor = selectedColors[i]

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(currentColor.primaryColor.copy(alpha = 0.2f))
                                        .border(1.dp, currentColor.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P${i + 1}",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = currentColor.primaryColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                TurnedPawn(
                                    color = currentColor,
                                    isMovable = false,
                                    isFinished = false,
                                    size = 32.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "CONTENDER NAME",
                                        style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                                        color = TextMuted
                                    )
                                    BasicTextField(
                                        value = names[i],
                                        onValueChange = { newName -> names[i] = newName },
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        cursorBrush = SolidColor(AccentPrimary),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 1.dp)
                                            .testTag("player_name_field_$i")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allPaletteColors.forEach { paletteColor ->
                                    val isSelectedByThis = currentColor == paletteColor
                                    val takenByOtherIndex = selectedColors.indexOfFirst { it == paletteColor }
                                    val isTakenByOther = takenByOtherIndex != -1 && takenByOtherIndex != i

                                    ColorPaletteChip(
                                        color = paletteColor,
                                        isSelected = isSelectedByThis,
                                        takenByLabel = if (isTakenByOther) "P${takenByOtherIndex + 1}" else null,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            if (isSelectedByThis) return@ColorPaletteChip
                                            if (isTakenByOther) {
                                                selectedColors[takenByOtherIndex] = currentColor
                                                selectedColors[i] = paletteColor
                                            } else {
                                                selectedColors[i] = paletteColor
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                GameButton(
                    text = "Start Match",
                    onClick = {
                        val configuredPlayers = (0 until playerCount).map { i ->
                            val cleanName = names[i].trim().ifEmpty { "Player ${i + 1}" }
                            Player(id = i, name = cleanName, color = selectedColors[i])
                        }
                        onStartGame(configuredPlayers)
                    },
                    style = GameButtonStyle.PRIMARY_GOLD,
                    icon = Icons.Default.PlayArrow,
                    testTag = "start_game_button"
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
private fun ColorPaletteChip(
    color: PlayerColor,
    isSelected: Boolean,
    takenByLabel: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) color.primaryColor else BorderHairline
    val bgColor = if (isSelected) SurfaceElevated else SurfacePanel

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.primaryColor)
                    .border(1.dp, color.accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF080F22),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = takenByLabel ?: color.displayName,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) TextPrimary else if (takenByLabel != null) TextMuted else TextSecondary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
