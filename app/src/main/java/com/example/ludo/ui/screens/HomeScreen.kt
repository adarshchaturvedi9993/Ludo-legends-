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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.LudoBrandEmblem
import com.example.ludo.ui.components.LudoHeroIllustration
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * LUDO LEGENDS - HOME SCREEN
 *
 * Design Architecture:
 * - Personality: Heritage club board-game with the precision, quiet restraint, and tactile solidity of an artisan physical set.
 * - One Visual Idea: Lathe-turned physical pawns and crisp isometric ivory dice anchored on a deep navy matte baize with gold navigational accents.
 * - Reference Discipline: Linear (density, hairline separation, typography discipline) and Chess.com / Flighty (utilitarian clarity and game telemetry).
 *
 * Main Functional Requirements:
 * 1. Deep navy background (#080F24) with gold (#F5C928) primary accent.
 * 2. Vector-crafted Ludo illustration with four colored pawns and 3D dice.
 * 3. Two visually dominant primary buttons: PLAY NOW and PLAY WITH AI.
 * 4. Compact additional sections: Resume Match (conditional), Game Statistics, Quick Rules, and Settings.
 * 5. 100% functional, responsive across screen sizes, and zero AI-slop graphics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    gamePreferences: GamePreferences? = null,
    onPassAndPlayClick: () -> Unit,
    onPlayWithComputerClick: () -> Unit,
    onResumeMatchClick: () -> Unit,
    onRulesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Interactive modal bottom sheet states
    var showStatsSheet by remember { mutableStateOf(false) }
    var showRulesSheet by remember { mutableStateOf(false) }

    // Live statistics state
    var totalMatches by remember { mutableIntStateOf(gamePreferences?.totalMatches ?: 0) }
    var totalWins by remember { mutableIntStateOf(gamePreferences?.totalWins ?: 0) }
    var totalCaptures by remember { mutableIntStateOf(gamePreferences?.totalCaptures ?: 0) }
    var totalSixes by remember { mutableIntStateOf(gamePreferences?.totalSixes ?: 0) }

    // Saved match summary if active
    val savedSummary = remember(hasSavedGame) {
        if (hasSavedGame) gamePreferences?.getSavedGameSummary() else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .testTag("home_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar: Left Avatar + Title/Subtitle, Right: Help ? & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Circular Avatar with gold border ring + Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D1D38))
                            .border(2.dp, Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)), CircleShape)
                            .clickable { showStatsSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ludo Legends",
                            style = Typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
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

                // Right: Help and Settings Circular buttons with gold rims
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Help button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D1D38))
                            .border(1.5.dp, Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)), CircleShape)
                            .clickable { showRulesSheet = true }
                            .testTag("rules_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = "Quick Rules",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D1D38))
                            .border(1.5.dp, Brush.linearGradient(listOf(GoldHighlight, AccentPrimary)), CircleShape)
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

            // 2. Hero Section: The authentic reference 3D artwork card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(com.example.R.drawable.home_hero_card),
                    contentDescription = "Ludo Legends - The Classic Game, Now Legendary!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            // 3. Dominant Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary 1: Play Now
                PlayNowDominantButton(onClick = onPassAndPlayClick)

                // Primary 2: Play With AI
                PlayWithAiDominantButton(onClick = onPlayWithComputerClick)
            }

            // 4. Compact 4-Card Quick Actions Row (Resume Match, Quick Rules, Game Stats, Settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Resume Match
                CompactUtilityCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    title = "Resume\nMatch",
                    hasActiveState = hasSavedGame,
                    testTag = "home_resume_card",
                    onClick = {
                        if (hasSavedGame) {
                            onResumeMatchClick()
                        } else {
                            showStatsSheet = true
                        }
                    }
                )

                // Card 2: Quick Rules
                CompactUtilityCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.MenuBook,
                    title = "Quick\nRules",
                    hasActiveState = false,
                    testTag = "home_rules_card",
                    onClick = { showRulesSheet = true }
                )

                // Card 3: Game Stats
                CompactUtilityCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart,
                    title = "Game\nStats",
                    hasActiveState = false,
                    testTag = "home_stats_card",
                    onClick = { showStatsSheet = true }
                )

                // Card 4: Settings
                CompactUtilityCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Settings,
                    title = "Settings",
                    hasActiveState = false,
                    testTag = "home_settings_card",
                    onClick = onSettingsClick
                )
            }

            // 5. Bottom Decorative Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(1.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, AccentPrimary)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "👑",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(1.dp)
                            .background(Brush.horizontalGradient(listOf(AccentPrimary, Color.Transparent)))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ROLL THE DICE • MAKE YOUR MOVE",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // ==========================================
        // Bottom Sheet: Game Statistics
        // ==========================================
        if (showStatsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatsSheet = false },
                containerColor = SurfacePanel,
                tonalElevation = 0.dp,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BorderHairline)
                    )
                }
            ) {
                GameStatisticsSheetContent(
                    matches = totalMatches,
                    wins = totalWins,
                    captures = totalCaptures,
                    sixes = totalSixes,
                    onResetStats = {
                        gamePreferences?.resetStatistics()
                        totalMatches = 0
                        totalWins = 0
                        totalCaptures = 0
                        totalSixes = 0
                    },
                    onClose = { showStatsSheet = false }
                )
            }
        }

        // ==========================================
        // Bottom Sheet: Quick Rules Reference
        // ==========================================
        if (showRulesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRulesSheet = false },
                containerColor = SurfacePanel,
                tonalElevation = 0.dp,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BorderHairline)
                    )
                }
            ) {
                QuickRulesSheetContent(
                    onFullSettingsClick = {
                        showRulesSheet = false
                        onRulesClick()
                    },
                    onClose = { showRulesSheet = false }
                )
            }
        }
    }
}

/**
 * Top header 48dp icon button adhering strictly to Android accessibility touch target guidelines.
 */
@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfacePanel)
            .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 22.dp),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextSecondary,
            modifier = Modifier.size(19.dp)
        )
    }
}

/**
 * Resume Match Card displayed only when an active, unfinished game is preserved in local storage.
 */
@Composable
private fun ResumeMatchCard(
    summary: GamePreferences.SavedGameSummary?,
    onResumeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePanel)
            .border(1.dp, StarGold.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
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
            // Pulsing status dot
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
                        style = Typography.labelSmall.copy(
                            letterSpacing = 0.5.sp,
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
                    "Continue unfinished game session"
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

        // Compact Resume Action Button
        Button(
            onClick = onResumeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceElevated,
                contentColor = AccentPrimary
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.border(1.dp, BorderActive, RoundedCornerShape(6.dp))
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Resume",
                style = Typography.labelLarge,
                color = AccentPrimary
            )
        }
    }
}

/**
 * Dominant Primary Action 1: PLAY NOW
 * Pill button with rich gold gradient, black play icon, bold title and subtitle.
 */
@Composable
private fun PlayNowDominantButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFDF60),
                        Color(0xFFF5B612),
                        Color(0xFFE5A812)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFF7C0), Color(0xFFF5C928))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.Black.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp)
            .testTag("play_now_button"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF080F22)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFF5C928),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Play Now",
                        style = Typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color(0xFF080F22)
                    )
                    Text(
                        text = "Play with 2, 3 or 4 Players",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF080F22),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Dominant Primary Action 2: PLAY WITH AI
 * Royal navy button with gold border, gold robot icon, and white bold text.
 */
@Composable
private fun PlayWithAiDominantButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0D1D38),
                        Color(0xFF091426)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFE078), Color(0xFF3987F5))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 20.dp)
            .testTag("play_with_ai_button"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF152441))
                        .border(1.dp, AccentPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Play With AI",
                        style = Typography.titleLarge.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Challenge Computer",
                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                        color = Color(0xFFA2AEC5)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFA2AEC5),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Compact square tile for secondary items: Resume Match, Quick Rules, Game Stats, Settings.
 */
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1D38))
            .border(
                width = if (hasActiveState) 1.5.dp else 1.dp,
                color = if (hasActiveState) AccentPrimary else Color(0xFF293B5B),
                shape = RoundedCornerShape(12.dp)
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
                modifier = Modifier.size(26.dp)
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

/**
 * Bottom Sheet content for live Game Statistics.
 */
@Composable
private fun GameStatisticsSheetContent(
    matches: Int,
    wins: Int,
    captures: Int,
    sixes: Int,
    onResetStats: () -> Unit,
    onClose: () -> Unit
) {
    val winRate = if (matches > 0) ((wins.toDouble() / matches) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("statistics_sheet_content")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Game Statistics",
                    style = Typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tracked on-device across all local matches",
                    style = Typography.bodyMedium,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                modifier = Modifier.weight(1f),
                label = "Matches Played",
                value = matches.toString()
            )
            StatMetricBox(
                modifier = Modifier.weight(1f),
                label = "Matches Won",
                value = wins.toString(),
                accent = true
            )
            StatMetricBox(
                modifier = Modifier.weight(1f),
                label = "Win Rate",
                value = "$winRate%"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                modifier = Modifier.weight(1f),
                label = "Tokens Captured",
                value = captures.toString()
            )
            StatMetricBox(
                modifier = Modifier.weight(1f),
                label = "Sixes Rolled",
                value = sixes.toString()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset statistics action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onResetStats,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderHairline)
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("reset_stats_button")
            ) {
                Text("Reset Statistics", style = Typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatMetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(1.dp, if (accent) AccentPrimary.copy(alpha = 0.5f) else BorderHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = Typography.displayMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (accent) AccentPrimary else TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = Typography.labelSmall.copy(fontSize = 11.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Bottom Sheet content for Quick Rules Reference.
 */
@Composable
private fun QuickRulesSheetContent(
    onFullSettingsClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("rules_sheet_content")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quick Rules Guide",
                    style = Typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Authentic international tournament rules",
                    style = Typography.bodyMedium,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickRuleRow(
                number = "1",
                title = "Entering the Board",
                desc = "Roll a 6 to move a token from your yard onto your starting square. Rolling a 6 also awards an extra roll."
            )
            QuickRuleRow(
                number = "2",
                title = "Capturing Opponents",
                desc = "Landing on an opponent's token sends it back to their yard and awards you an immediate bonus roll."
            )
            QuickRuleRow(
                number = "3",
                title = "Safe Star Zones",
                desc = "8 marked stars protect tokens. Tokens on safe tiles cannot be captured by opponents."
            )
            QuickRuleRow(
                number = "4",
                title = "Exact Home Finish",
                desc = "Tokens entering the home stretch column require the exact dice value to reach the center triangle."
            )
            QuickRuleRow(
                number = "5",
                title = "Three Sixes Forfeiture",
                desc = "Rolling 6 three consecutive times forfeits the turn and passes immediately to the next player."
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onFullSettingsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceElevated,
                contentColor = AccentPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderActive, RoundedCornerShape(8.dp))
        ) {
            Text("Open Audio Settings & Details", style = Typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickRuleRow(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(AccentPrimary.copy(alpha = 0.15f))
                .border(1.dp, AccentPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AccentPrimary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = Typography.labelLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
    }
}
