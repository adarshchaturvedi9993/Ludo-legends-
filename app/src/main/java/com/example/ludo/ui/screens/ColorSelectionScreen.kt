package com.example.ludo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ludo.ads.BannerAdView
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.PawnColorCatalog
import com.example.ludo.model.PawnColorSkin
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.CoinBalanceChip
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Match Setup & Customization Screen (Pass & Play):
 * - Customize Player Names and Track Corners (Red, Green, Yellow, Blue).
 * - Unlock and equip 6 New Premium Pawn Colors (25 coins each).
 * - Unlock and equip 5 Exclusive Board Themes (150 coins each).
 * - Coin Treasury Chip with +50 Rewarded Video ad trigger.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelectionScreen(
    playerCount: Int,
    initialNames: List<String>,
    gamePreferences: GamePreferences? = null,
    onWatchRewardAd: (() -> Unit)? = null,
    onBackClick: () -> Unit,
    onStartGame: (List<Player>) -> Unit
) {
    var userCoins by remember { mutableIntStateOf(gamePreferences?.coinsBalance ?: 50) }
    var selectedThemeId by remember {
        mutableStateOf(gamePreferences?.selectedBoardThemeId ?: BoardThemeCatalog.THEME_ROYAL_NAVY.id)
    }

    // Refresh coins balance when returning or watching ad
    val currentCoins = gamePreferences?.coinsBalance ?: userCoins

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

    // Selected custom skin ID per player (null = classic corner color)
    val selectedCustomSkins = remember {
        mutableStateListOf<String?>().apply {
            repeat(playerCount) { add(null) }
        }
    }

    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val themeScrollState = rememberScrollState()

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
            // Top Bar + Coin Treasury Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTopBar(
                    title = "Match Setup & Shop",
                    subtitle = "$playerCount Contenders",
                    onBackClick = onBackClick,
                    modifier = Modifier.weight(1f)
                )

                CoinBalanceChip(
                    coins = currentCoins,
                    onEarnCoinsClick = onWatchRewardAd
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. BOARD THEMES SHOP STRIP (150 Coins each)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXCLUSIVE BOARD THEMES (150 🪙)",
                                style = Typography.labelLarge.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = StarGold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(themeScrollState),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BoardThemeCatalog.allThemes.forEach { theme ->
                            val isUnlocked = gamePreferences?.isBoardThemeUnlocked(theme.id) ?: theme.isDefaultUnlocked
                            val isSelected = selectedThemeId == theme.id

                            BoardThemeShopCard(
                                theme = theme,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onClick = {
                                    if (isUnlocked) {
                                        selectedThemeId = theme.id
                                        gamePreferences?.selectedBoardThemeId = theme.id
                                    } else {
                                        // Attempt unlock
                                        if (gamePreferences?.unlockBoardTheme(theme.id) == true) {
                                            selectedThemeId = theme.id
                                            userCoins = gamePreferences.coinsBalance
                                            dialogMessage = "Unlocked ${theme.displayName} for 150 coins!"
                                        } else {
                                            dialogMessage = "Insufficient coins! You need 150 coins. Watch rewarded videos (+50 coins each) to earn more!"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // 2. CONTENDER ROSTER & PAWN SKINS (25 Coins each)
                Text(
                    text = "CONTENDER ROSTER & PAWN CUSTOMIZATION",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary
                )

                for (i in 0 until playerCount) {
                    val currentColor = selectedColors[i]
                    val currentCustomSkin = PawnColorCatalog.getSkinById(selectedCustomSkins[i])

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Player Info & Name Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background((currentCustomSkin?.primaryColor ?: currentColor.primaryColor).copy(alpha = 0.2f))
                                        .border(1.dp, (currentCustomSkin?.primaryColor ?: currentColor.primaryColor).copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P${i + 1}",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = currentCustomSkin?.primaryColor ?: currentColor.primaryColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                TurnedPawn(
                                    color = currentColor,
                                    isMovable = false,
                                    isFinished = false,
                                    size = 32.dp,
                                    customPrimaryColor = currentCustomSkin?.primaryColor,
                                    customAccentColor = currentCustomSkin?.accentColor,
                                    customDarkShade = currentCustomSkin?.darkShade
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
                                        text = "PLAYER ${i + 1} NAME",
                                        style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
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

                            // Track Corner Selector (Red, Green, Yellow, Blue)
                            Text(
                                text = "STARTING YARD CORNER:",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted)
                            )
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

                            // Premium Pawn Color Skin Picker (25 Coins)
                            Text(
                                text = "CUSTOM PAWN COLOR (25 🪙 EACH):",
                                style = Typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarGold
                                )
                            )

                            val pawnSkinRowState = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(pawnSkinRowState),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Default Corner Color option
                                val isDefaultSelected = selectedCustomSkins[i] == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDefaultSelected) AccentPrimary.copy(alpha = 0.2f) else SurfaceElevated)
                                        .border(1.dp, if (isDefaultSelected) AccentPrimary else BorderHairline, RoundedCornerShape(8.dp))
                                        .clickable { selectedCustomSkins[i] = null }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(currentColor.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Default",
                                            style = Typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isDefaultSelected) AccentPrimary else TextSecondary
                                        )
                                    }
                                }

                                // 6 Premium Pawn Colors (25 coins each)
                                PawnColorCatalog.allPremiumColors.forEach { skin ->
                                    val isSkinUnlocked = gamePreferences?.isPawnColorUnlocked(skin.id) ?: false
                                    val isSkinSelected = selectedCustomSkins[i] == skin.id

                                    PawnSkinChip(
                                        skin = skin,
                                        isUnlocked = isSkinUnlocked,
                                        isSelected = isSkinSelected,
                                        onClick = {
                                            if (isSkinUnlocked) {
                                                selectedCustomSkins[i] = skin.id
                                            } else {
                                                if (gamePreferences?.unlockPawnColor(skin.id) == true) {
                                                    selectedCustomSkins[i] = skin.id
                                                    userCoins = gamePreferences.coinsBalance
                                                    dialogMessage = "Unlocked ${skin.displayName} for 25 coins!"
                                                } else {
                                                    dialogMessage = "Insufficient coins! You need 25 coins for ${skin.displayName}. Watch a video ad (+50 coins) to unlock!"
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Start Match & Banner Ad
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                GameButton(
                    text = "Start Match",
                    onClick = {
                        val configuredPlayers = (0 until playerCount).map { i ->
                            val cleanName = names[i].trim().ifEmpty { "Player ${i + 1}" }
                            Player(
                                id = i,
                                name = cleanName,
                                color = selectedColors[i],
                                customSkinId = selectedCustomSkins[i]
                            )
                        }
                        onStartGame(configuredPlayers)
                    },
                    style = GameButtonStyle.PRIMARY_GOLD,
                    icon = Icons.Default.PlayArrow,
                    testTag = "start_game_button"
                )
                Spacer(modifier = Modifier.height(6.dp))
                BannerAdView(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_LOBBY,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = SurfaceCard,
                    containerBorderColor = BorderHairline
                )
            }
        }

        // Notification Modal for shop transactions
        if (dialogMessage != null) {
            BasicAlertDialog(
                onDismissRequest = { dialogMessage = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1A2E))
                    .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TREASURY & SHOP",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                    Text(
                        text = dialogMessage ?: "",
                        style = Typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StarGold)
                            .clickable { dialogMessage = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Got It",
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF090E1A)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardThemeShopCard(
    theme: BoardTheme,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> StarGold
        isUnlocked -> AccentPrimary.copy(alpha = 0.5f)
        else -> BorderHairline
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(105.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.boardBackground)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(8.dp)
            .testTag("theme_card_${theme.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(theme.starColor)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StarGold)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF101626)
                        )
                    }
                } else if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF271C0B))
                            .border(1.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "150 🪙",
                            style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = StarGold
                        )
                    }
                } else {
                    Text(
                        text = "OWNED",
                        style = Typography.labelSmall.copy(fontSize = 8.sp, color = AccentPrimary)
                    )
                }
            }

            Column {
                Text(
                    text = theme.displayName,
                    style = Typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = if (isUnlocked) "Tap to equip" else "Tap to unlock",
                    style = Typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PawnSkinChip(
    skin: PawnColorSkin,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> skin.primaryColor
        isUnlocked -> BorderHairline
        else -> StarGold.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SurfaceElevated else SurfaceCard)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("skin_chip_${skin.id}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(skin.primaryColor)
                    .border(1.dp, skin.accentColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Column {
                Text(
                    text = skin.displayName,
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                if (!isUnlocked) {
                    Text(
                        text = "25 🪙",
                        style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                }
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
    val bgColor = if (isSelected) SurfaceElevated else SurfaceCard

    Box(
        modifier = modifier
            .height(50.dp)
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
                    .background(color.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF090E1A),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = takenByLabel ?: color.displayName,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) TextPrimary else if (takenByLabel != null) TextMuted else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
