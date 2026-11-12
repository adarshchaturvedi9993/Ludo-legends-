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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ludo.ads.BannerAdView
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.BoardThemeCatalog
import com.example.ludo.model.BotDifficulty
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotSetupScreen(
    initialUserName: String,
    gamePreferences: GamePreferences? = null,
    onWatchRewardAd: (() -> Unit)? = null,
    onBackClick: () -> Unit,
    onStartGame: (List<Player>) -> Unit
) {
    var totalPlayers by remember { mutableIntStateOf(4) }
    var selectedDifficulty by remember { mutableStateOf(BotDifficulty.MEDIUM) }
    var userName by remember { mutableStateOf(initialUserName) }
    var userColor by remember { mutableStateOf(PlayerColor.RED) }
    var selectedCustomSkinId by remember { mutableStateOf<String?>(null) }
    var selectedThemeId by remember {
        mutableStateOf(gamePreferences?.selectedBoardThemeId ?: BoardThemeCatalog.THEME_ROYAL_NAVY.id)
    }

    var userCoins by remember { mutableIntStateOf(gamePreferences?.coinsBalance ?: 50) }
    val currentCoins = gamePreferences?.coinsBalance ?: userCoins

    // Confirmation dialog state for pawn skin purchase
    var confirmPurchaseSkin by remember { mutableStateOf<PawnColorSkin?>(null) }

    val scrollState = rememberScrollState()
    val allColors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)

    val currentCustomSkin = PawnColorCatalog.getSkinById(selectedCustomSkinId)

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("bot_setup_screen")
        ) {
            // Header + Coin Treasury Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTopBar(
                    title = "AI Arena & Themes",
                    subtitle = "Solo Match Setup",
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

                // 2. Opponent Count
                Text(
                    text = "OPPONENT BOT COUNT",
                    style = Typography.labelLarge.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.0.sp,
                        fontWeight = FontWeight.Bold
                    ),
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
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentPrimary else BorderHairline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { totalPlayers = count },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = Typography.titleMedium.copy(fontSize = 13.sp),
                                color = if (isSelected) AccentPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Bot Difficulty Selection
                Text(
                    text = "BOT INTELLIGENCE TIER",
                    style = Typography.labelLarge.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.0.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotDifficulty.values().forEach { difficulty ->
                        val isSelected = selectedDifficulty == difficulty
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentPrimary else BorderHairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDifficulty = difficulty }
                                .padding(12.dp)
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
                                            .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else SurfaceCard)
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
                                            text = difficulty.displayName,
                                            style = Typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                            color = if (isSelected) AccentPrimary else TextPrimary
                                        )
                                        Text(
                                            text = difficulty.description,
                                            style = Typography.bodyMedium.copy(fontSize = 11.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(AccentPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF090E1A),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. User Profile & Team Color & Pawn Skin
                Text(
                    text = "YOUR PROFILE & CUSTOM PAWN",
                    style = Typography.labelLarge.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.0.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TurnedPawn(
                                color = userColor,
                                isMovable = false,
                                isFinished = false,
                                size = 34.dp,
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
                                    text = "YOUR PLAYER NAME",
                                    style = Typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
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

                        // Starting Corner Color
                        Text(
                            text = "STARTING YARD CORNER:",
                            style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allColors.forEach { color ->
                                val isChosen = userColor == color
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) SurfaceElevated else SurfaceCard)
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
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = color.displayName,
                                            style = Typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isChosen) TextPrimary else TextSecondary,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Premium Pawn Color Skin Selector (25 Coins)
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

                        val botSkinRowState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(botSkinRowState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isDefaultChosen = selectedCustomSkinId == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDefaultChosen) AccentPrimary.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (isDefaultChosen) AccentPrimary else BorderHairline, RoundedCornerShape(8.dp))
                                    .clickable { selectedCustomSkinId = null }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(userColor.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Default",
                                        style = Typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isDefaultChosen) AccentPrimary else TextSecondary
                                    )
                                }
                            }

                            PawnColorCatalog.allPremiumColors.forEach { skin ->
                                val isSkinUnlocked = gamePreferences?.isPawnColorUnlocked(skin.id) ?: false
                                val isSkinSelected = selectedCustomSkinId == skin.id

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSkinSelected) SurfaceElevated else SurfaceCard)
                                        .border(
                                            if (isSkinSelected) 1.5.dp else 1.dp,
                                            if (isSkinSelected) skin.primaryColor else if (isSkinUnlocked) BorderHairline else StarGold.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (isSkinUnlocked) {
                                                selectedCustomSkinId = skin.id
                                            } else {
                                                // Ask for confirmation before deducting coins!
                                                confirmPurchaseSkin = skin
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("bot_skin_${skin.id}"),
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
                                                    fontWeight = if (isSkinSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSkinSelected) TextPrimary else TextSecondary
                                            )
                                            if (!isSkinUnlocked) {
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
                        }
                    }
                }
            }

            // Start Battle & Banner Ad
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
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
                                isBot = false,
                                customSkinId = selectedCustomSkinId
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
            val skin = confirmPurchaseSkin!!
            CoinPurchaseConfirmDialog(
                itemName = "${skin.displayName} Pawn Skin",
                itemCategory = "Premium Pawn Customization",
                itemDescription = "Equip a distinctive custom lacquer color on your game pawns.",
                priceCoins = 25,
                currentCoins = currentCoins,
                previewContent = {
                    TurnedPawn(
                        color = userColor,
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
                        selectedCustomSkinId = skin.id
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
