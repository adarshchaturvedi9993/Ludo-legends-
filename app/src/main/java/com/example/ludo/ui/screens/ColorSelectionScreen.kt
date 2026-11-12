package com.example.ludo.ui.screens

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
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.PawnColorCatalog
import com.example.ludo.model.PawnColorSkin
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.BoardThemeShopRow
import com.example.ludo.ui.components.CoinBalanceChip
import com.example.ludo.ui.components.CoinPurchaseConfirmDialog
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.Real3DGoldCoin
import com.example.ludo.ui.components.TurnedPawn
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Match Setup & Customization Screen (Pass & Play):
 * - Customize Player Names and Track Corners (Red, Green, Yellow, Blue).
 * - Full visual Board Theme Previews with confirmation dialog before spending 150 coins.
 * - Unlock and equip Premium Pawn Colors (25 coins each) with confirmation dialog.
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

    // Confirmation dialog state for pawn skin purchase (playerIndex, skin)
    var confirmPurchaseSkin by remember { mutableStateOf<Pair<Int, PawnColorSkin>?>(null) }

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
            // Top Bar + Coin Treasury Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTopBar(
                    title = "Match Setup & Themes",
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
                // 1. BOARD THEMES SHOP WITH LIVE PREVIEWS & CONFIRMATION
                BoardThemeShopRow(
                    selectedThemeId = selectedThemeId,
                    gamePreferences = gamePreferences,
                    currentCoins = currentCoins,
                    onThemeEquipped = { themeId ->
                        selectedThemeId = themeId
                    },
                    onCoinsUpdated = { newCoins ->
                        userCoins = newCoins
                    },
                    onWatchRewardAd = onWatchRewardAd
                )

                // 2. CONTENDER ROSTER & PAWN SKINS
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CUSTOM PAWN COLOR:",
                                    style = Typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarGold
                                    )
                                )
                                Text(
                                    text = "25 🪙 each",
                                    style = Typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextMuted
                                )
                            }

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
                                                // Ask for confirmation before deducting coins!
                                                confirmPurchaseSkin = Pair(i, skin)
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

        // Strict Confirmation Dialog before deducting coins for Pawn Skin
        if (confirmPurchaseSkin != null) {
            val (playerIdx, skin) = confirmPurchaseSkin!!
            CoinPurchaseConfirmDialog(
                itemName = "${skin.displayName} Pawn Skin",
                itemCategory = "Premium Pawn Customization",
                itemDescription = "Lustrous lacquer finish pawn skin usable for any player in your matches.",
                priceCoins = 25,
                currentCoins = currentCoins,
                previewContent = {
                    TurnedPawn(
                        color = selectedColors[playerIdx],
                        isMovable = false,
                        isFinished = false,
                        size = 46.dp,
                        customPrimaryColor = skin.primaryColor,
                        customAccentColor = skin.accentColor,
                        customDarkShade = skin.darkShade
                    )
                },
                onConfirm = {
                    if (gamePreferences?.unlockPawnColor(skin.id) == true) {
                        selectedCustomSkins[playerIdx] = skin.id
                        userCoins = gamePreferences.coinsBalance
                    }
                    confirmPurchaseSkin = null
                },
                onDismiss = {
                    confirmPurchaseSkin = null
                },
                onWatchAd = {
                    confirmPurchaseSkin = null
                    onWatchRewardAd?.invoke()
                }
            )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Real3DGoldCoin(size = 9.dp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "25",
                            style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = StarGold
                        )
                    }
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
