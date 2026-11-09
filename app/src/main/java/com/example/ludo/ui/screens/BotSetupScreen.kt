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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
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
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.GlassCard
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

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

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("bot_setup_screen")
        ) {
            GameTopBar(
                title = "AI Match Setup",
                subtitle = "Solo vs Bots",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Total Opponents
                Text(
                    text = "OPPONENT BOT COUNT",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair(2, "vs 1 Bot"),
                        Pair(3, "vs 2 Bots"),
                        Pair(4, "vs 3 Bots")
                    ).forEach { (count, label) ->
                        val isSelected = totalPlayers == count
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            isActive = isSelected,
                            activeBorderColor = AccentPrimary,
                            onClick = { totalPlayers = count }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
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
                }

                // 2. Bot Difficulty Selection
                Text(
                    text = "BOT INTELLIGENCE TIER",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotDifficulty.values().forEach { difficulty ->
                        val isSelected = selectedDifficulty == difficulty
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            isActive = isSelected,
                            activeBorderColor = AccentPrimary,
                            onClick = { selectedDifficulty = difficulty }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else Color(0xFF080F22))
                                            .border(1.dp, if (isSelected) AccentPrimary else BorderHairline, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = if (isSelected) AccentPrimary else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = difficulty.displayName.uppercase(),
                                            style = Typography.titleLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                            color = if (isSelected) AccentPrimary else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = difficulty.description,
                                            style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(AccentPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
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

                // 3. User Name & Color Configuration
                Text(
                    text = "YOUR PROFILE & COLOR",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "PLAYER NAME",
                                    style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                                    color = TextMuted
                                )
                                BasicTextField(
                                    value = userName,
                                    onValueChange = { userName = it },
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
                                        .testTag("bot_user_name_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allColors.forEach { color ->
                                val isChosen = userColor == color
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) SurfaceElevated else SurfacePanel)
                                        .border(
                                            width = if (isChosen) 1.5.dp else 1.dp,
                                            color = if (isChosen) color.primaryColor else BorderHairline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { userColor = color },
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
                                            color = if (isChosen) Color.White else TextSecondary,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                GameButton(
                    text = "Start AI Battle",
                    onClick = {
                        val cleanUserName = userName.trim().ifEmpty { "Player 1" }
                        val botNames = listOf("Titan", "Vortex", "Orion", "Nova", "Atlas")

                        val configuredPlayers = mutableListOf<Player>()
                        configuredPlayers.add(
                            Player(
                                id = 0,
                                name = cleanUserName,
                                color = userColor,
                                isBot = false
                            )
                        )

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
                    style = GameButtonStyle.PRIMARY_GOLD,
                    icon = Icons.Default.PlayArrow,
                    testTag = "start_bot_match_button"
                )
            }
        }
    }
}
