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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ludo.model.BotDifficulty
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Play With Computer setup screen:
 * Allows configuring total player count (1-3 bots), bot difficulty (Easy/Medium/Hard),
 * player name, and user color selection with live 2.5D TurnedPawn preview.
 */
@Composable
fun BotSetupScreen(
    initialUserName: String,
    onBackClick: () -> Unit,
    onStartGame: (List<Player>) -> Unit
) {
    var totalPlayers by remember { mutableIntStateOf(4) }
    var selectedDifficulty by remember { mutableStateOf(BotDifficulty.MEDIUM) }
    var userName by remember { mutableStateOf(initialUserName) }
    var userColor by remember { mutableStateOf(PlayerColor.RED) }

    val scrollState = rememberScrollState()
    val allColors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 20.dp)
            .testTag("bot_setup_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Bar
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
                        .testTag("bot_setup_back_button")
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
                        text = "AI Match Configuration",
                        style = Typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Solo Challenge • Offline Neural Bots",
                        style = Typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Scrollable Options Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // 1. Total Opponents / Bot Count
            Text(
                text = "OPPONENT CONFIGURATION",
                style = Typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfacePanel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(2, "vs 1 Bot"),
                    Pair(3, "vs 2 Bots"),
                    Pair(4, "vs 3 Bots")
                ).forEach { (count, label) ->
                    val isSelected = totalPlayers == count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SurfaceElevated else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) AccentPrimary else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { totalPlayers = count }
                            )
                            .testTag("bot_count_$count"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = Typography.labelLarge.copy(fontSize = 13.sp),
                            color = if (isSelected) AccentPrimary else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Bot Difficulty Selection (Easy / Medium / Hard)
            Text(
                text = "BOT INTELLIGENCE TIER",
                style = Typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotDifficulty.values().forEach { difficulty ->
                    val isSelected = selectedDifficulty == difficulty
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceElevated else SurfacePanel)
                            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentPrimary else BorderHairline, RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { selectedDifficulty = difficulty }
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("bot_difficulty_${difficulty.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color(0xFF080F22))
                                    .border(1.dp, if (isSelected) AccentPrimary.copy(alpha = 0.5f) else BorderHairline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentPrimary else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = difficulty.displayName,
                                        style = Typography.bodyLarge.copy(fontSize = 15.sp),
                                        color = if (isSelected) AccentPrimary else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color(0xFF080F22))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = when (difficulty) {
                                                BotDifficulty.EASY -> "CASUAL"
                                                BotDifficulty.MEDIUM -> "STANDARD"
                                                BotDifficulty.HARD -> "PRO"
                                            },
                                            style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = if (isSelected) AccentPrimary else TextMuted
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = difficulty.description,
                                    style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(22.dp)
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
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. User Name & Color Configuration
            Text(
                text = "YOUR PROFILE",
                style = Typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfacePanel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                // Row with TurnedPawn preview and player name input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TurnedPawn(
                        color = userColor,
                        isMovable = false,
                        isFinished = false,
                        size = 36.dp
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
                            text = "PLAYER NAME",
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextMuted
                        )
                        BasicTextField(
                            value = userName,
                            onValueChange = { userName = it },
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
                                .testTag("bot_user_name_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CHOOSE YOUR TOKEN COLOR",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allColors.forEach { color ->
                        val isChosen = userColor == color
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) SurfaceElevated else SurfacePanel)
                                .border(
                                    width = if (isChosen) 1.5.dp else 1.dp,
                                    color = if (isChosen) color.primaryColor else BorderHairline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { userColor = color }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color.primaryColor)
                                        .border(1.dp, color.accentColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = color.displayName,
                                    style = Typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isChosen) TextPrimary else TextSecondary,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Start Game Button
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Button(
                onClick = {
                    val cleanUserName = userName.trim().ifEmpty { "Player 1" }
                    val botNames = listOf("Titan", "Vortex", "Orion", "Nova", "Atlas")

                    val configuredPlayers = mutableListOf<Player>()
                    // Add Human player
                    configuredPlayers.add(
                        Player(
                            id = 0,
                            name = cleanUserName,
                            color = userColor,
                            isBot = false
                        )
                    )

                    // Assign remaining unique colors to bots
                    val remainingColors = allColors.filter { it != userColor }.toMutableList()
                    for (b in 1 until totalPlayers) {
                        val botColor = remainingColors.removeAt(0)
                        val botName = "Bot " + botNames[(b - 1) % botNames.size]
                        configuredPlayers.add(
                            Player(
                                id = b,
                                name = botName,
                                color = botColor,
                                isBot = true,
                                botDifficulty = selectedDifficulty
                            )
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
                    .testTag("start_bot_match_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start AI Battle",
                    style = Typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

