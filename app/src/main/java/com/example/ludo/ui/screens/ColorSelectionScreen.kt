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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Dedicated Player Name and Color Selection Screen.
 * Enables each player to input their name and choose from the predefined palette
 * of distinct colors, with automatic swap mechanics guaranteeing 100% color uniqueness.
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

    // Default unique colors per player slot
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 20.dp)
            .testTag("color_selection_screen")
    ) {
        // App Bar
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
                    .testTag("color_selection_back_button")
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
                    text = "Player Configuration",
                    style = Typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "$playerCount Contenders • Color uniqueness enforced",
                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Player Cards
            for (i in 0 until playerCount) {
                val currentColor = selectedColors[i]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfacePanel)
                        .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                        .testTag("player_custom_card_$i")
                ) {
                    // Header: Player badge, Live 2.5D TurnedPawn, and Name Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Slot Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(currentColor.primaryColor.copy(alpha = 0.18f))
                                .border(1.dp, currentColor.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P${i + 1}",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                ),
                                color = currentColor.primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Live 2.5D TurnedPawn Preview
                        TurnedPawn(
                            color = currentColor,
                            isMovable = false,
                            isFinished = false,
                            size = 34.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "NAME",
                                style = Typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextMuted
                            )
                            BasicTextField(
                                value = names[i],
                                onValueChange = { newName -> names[i] = newName },
                                textStyle = TextStyle(
                                    color = TextPrimary,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color Palette Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ASSIGNED TOKEN COLOR",
                            style = Typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = "Tap to swap",
                            style = Typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allPaletteColors.forEach { paletteColor ->
                            val isSelectedByThis = currentColor == paletteColor
                            // Check if taken by another player
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
                                        // Seamless swap to guarantee 100% uniqueness without error!
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

            Spacer(modifier = Modifier.height(14.dp))

            // Uniqueness confirmation indicator
            val areAllUnique = selectedColors.distinct().size == playerCount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (areAllUnique) AccentPrimary else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (areAllUnique) "All $playerCount contenders have unique board quadrants" else "Color conflict detected",
                    style = Typography.labelSmall.copy(fontSize = 12.sp),
                    color = if (areAllUnique) TextPrimary else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Start Match Primary Action
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Button(
                onClick = {
                    val configuredPlayers = (0 until playerCount).map { i ->
                        val cleanName = names[i].trim().ifEmpty { "Player ${i + 1}" }
                        Player(
                            id = i,
                            name = cleanName,
                            color = selectedColors[i]
                        )
                    }
                    onStartGame(configuredPlayers)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = Color(0xFF080F22)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Match",
                    style = Typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            .height(54.dp)
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
            // Color dot with selection checkmark
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

