package com.example.ludo.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludo.data.GamePreferences
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.GoldRimGradientColors
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Production Home Screen:
 * - Unified atmospheric navy canvas background.
 * - Top header with user profile & quick action icons.
 * - Hero 3D Ludo illustration banner.
 * - Dominant Play Now (pass & play) and Play With AI buttons with tactile press-scale.
 * - Resume Match summary card (when match in progress).
 * - Compact utility cards: Quick Rules, Game Stats, Settings.
 */
@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    gamePreferences: GamePreferences? = null,
    onPassAndPlayClick: () -> Unit,
    onPlayWithComputerClick: () -> Unit,
    onResumeMatchClick: () -> Unit,
    onRulesClick: () -> Unit,
    onStatsClick: () -> Unit = onRulesClick,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val savedSummary = remember(hasSavedGame) {
        if (hasSavedGame) gamePreferences?.getSavedGameSummary() else null
    }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Bar: Profile avatar + Title/Subtitle & Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1D38))
                                .border(2.dp, Brush.linearGradient(GoldRimGradientColors), CircleShape)
                                .clickable { onStatsClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "LUDO LEGENDS",
                                style = Typography.titleLarge.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Play • Challenge • Win",
                                style = Typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFC7D3E8)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1D38))
                                .border(1.5.dp, Brush.linearGradient(GoldRimGradientColors), CircleShape)
                                .clickable { onRulesClick() }
                                .testTag("rules_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = "Rules",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1D38))
                                .border(1.5.dp, Brush.linearGradient(GoldRimGradientColors), CircleShape)
                                .clickable(onClick = onSettingsClick)
                                .testTag("settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 2. Hero Section: 3D Artwork card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, BorderHairline, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.home_hero_card),
                        contentDescription = "Ludo Legends Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }

                // 3. Conditional Resume Match Card
                if (hasSavedGame) {
                    ResumeMatchCard(
                        summary = savedSummary,
                        onResumeClick = onResumeMatchClick
                    )
                }

                // 4. Dominant Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameButton(
                        text = "Play Now",
                        subtitle = "Pass & Play with 2, 3 or 4 Players",
                        onClick = onPassAndPlayClick,
                        style = GameButtonStyle.PRIMARY_GOLD,
                        icon = Icons.Default.PlayArrow,
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        height = 68.dp,
                        testTag = "play_now_button"
                    )

                    GameButton(
                        text = "Play With AI",
                        subtitle = "Challenge Intelligent Computer Bots",
                        onClick = onPlayWithComputerClick,
                        style = GameButtonStyle.SECONDARY_GLASS,
                        icon = Icons.Default.SmartToy,
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        height = 64.dp,
                        testTag = "play_with_ai_button"
                    )
                }

                // 5. Compact 4-Card Quick Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactUtilityCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.EmojiEvents,
                        title = "Resume\nMatch",
                        hasActiveState = hasSavedGame,
                        testTag = "home_resume_card",
                        onClick = {
                            if (hasSavedGame) onResumeMatchClick() else onStatsClick()
                        }
                    )

                    CompactUtilityCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.MenuBook,
                        title = "Quick\nRules",
                        testTag = "home_rules_card",
                        onClick = onRulesClick
                    )

                    CompactUtilityCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.BarChart,
                        title = "Game\nStats",
                        testTag = "home_stats_card",
                        onClick = onStatsClick
                    )

                    CompactUtilityCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        testTag = "home_settings_card",
                        onClick = onSettingsClick
                    )
                }

                // 6. Bottom Footer with Rights & Credits
                AppFooter(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ResumeMatchCard(
    summary: GamePreferences.SavedGameSummary?,
    onResumeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePanel)
            .border(1.5.dp, StarGold.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onResumeClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("resume_match_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(StarGold)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RESUME MATCH",
                        style = Typography.labelLarge.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = StarGold
                    )
                    if (summary?.hasBots == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "VS BOT",
                                style = Typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val subtitleText = if (summary != null) {
                    "${summary.playerCount} Players • Turn ${summary.turnCount} • ${summary.currentPlayerName}'s turn"
                } else {
                    "Continue preserved session"
                }

                Text(
                    text = subtitleText,
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPrimary)
                .clickable { onResumeClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF080F22),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Resume",
                    style = Typography.labelLarge.copy(fontSize = 13.sp),
                    color = Color(0xFF080F22)
                )
            }
        }
    }
}

@Composable
private fun CompactUtilityCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    hasActiveState: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D1D38))
            .border(
                width = if (hasActiveState) 1.5.dp else 1.dp,
                color = if (hasActiveState) AccentPrimary else Color(0xFF293B5B),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 10.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(24.dp)
            )
            if (hasActiveState) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AccentPrimary)
                )
            }
        }

        Text(
            text = title,
            style = Typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 13.sp
            ),
            color = Color(0xFFF5F7FC),
            textAlign = TextAlign.Center
        )
    }
}
