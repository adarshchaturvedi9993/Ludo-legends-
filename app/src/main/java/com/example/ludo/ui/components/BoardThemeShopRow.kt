package com.example.ludo.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.BoardThemeCatalog
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
 * High-Quality Board Themes Shop Strip with Live Miniature Board Previews.
 * Includes:
 * - Miniature board visual preview inside each theme card.
 * - Tap to open Full-Screen Board Theme Preview Dialog.
 * - Strict coin deduction confirmation dialog before spending 150 coins.
 */
@Composable
fun BoardThemeShopRow(
    selectedThemeId: String,
    gamePreferences: GamePreferences?,
    currentCoins: Int,
    onThemeEquipped: (String) -> Unit,
    onCoinsUpdated: (Int) -> Unit,
    onWatchRewardAd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var previewTheme by remember { mutableStateOf<BoardTheme?>(null) }
    var confirmPurchaseTheme by remember { mutableStateOf<BoardTheme?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
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
                    text = "BOARD THEMES & PREVIEWS (150 🪙)",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = StarGold
                )
            }

            Text(
                text = "Tap to Preview",
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }

        // Horizontal Carousel of Theme Cards with Mini Board Previews
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoardThemeCatalog.allThemes.forEach { theme ->
                val isUnlocked = gamePreferences?.isBoardThemeUnlocked(theme.id) ?: theme.isDefaultUnlocked
                val isEquipped = selectedThemeId == theme.id

                BoardThemeMiniPreviewCard(
                    theme = theme,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    onClick = {
                        previewTheme = theme
                    }
                )
            }
        }
    }

    // 1. Full Board Theme Preview Dialog
    if (previewTheme != null) {
        val theme = previewTheme!!
        val isUnlocked = gamePreferences?.isBoardThemeUnlocked(theme.id) ?: theme.isDefaultUnlocked
        val isEquipped = selectedThemeId == theme.id

        BoardThemePreviewDialog(
            theme = theme,
            isUnlocked = isUnlocked,
            isEquipped = isEquipped,
            currentCoins = currentCoins,
            onEquip = {
                gamePreferences?.selectedBoardThemeId = theme.id
                onThemeEquipped(theme.id)
                previewTheme = null
            },
            onRequestUnlock = {
                confirmPurchaseTheme = theme
                previewTheme = null
            },
            onDismiss = {
                previewTheme = null
            }
        )
    }

    // 2. Strict Coin Confirmation Dialog before deducting coins
    if (confirmPurchaseTheme != null) {
        val theme = confirmPurchaseTheme!!
        CoinPurchaseConfirmDialog(
            itemName = theme.displayName,
            itemCategory = "Exclusive Board Theme",
            itemDescription = theme.description,
            priceCoins = theme.priceCoins,
            currentCoins = currentCoins,
            previewContent = {
                MiniBoardPreview(
                    theme = theme,
                    modifier = Modifier.size(120.dp),
                    cornerRadius = 8.dp
                )
            },
            onConfirm = {
                if (gamePreferences?.unlockBoardTheme(theme.id) == true) {
                    gamePreferences.selectedBoardThemeId = theme.id
                    onThemeEquipped(theme.id)
                    onCoinsUpdated(gamePreferences.coinsBalance)
                }
                confirmPurchaseTheme = null
            },
            onDismiss = {
                confirmPurchaseTheme = null
            },
            onWatchAd = {
                confirmPurchaseTheme = null
                onWatchRewardAd?.invoke()
            }
        )
    }
}

@Composable
private fun BoardThemeMiniPreviewCard(
    theme: BoardTheme,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isEquipped -> StarGold
        isUnlocked -> AccentPrimary.copy(alpha = 0.5f)
        else -> BorderHairline
    }

    Box(
        modifier = Modifier
            .width(136.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(if (isEquipped) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Miniature Board Preview
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                MiniBoardPreview(
                    theme = theme,
                    modifier = Modifier.size(68.dp),
                    cornerRadius = 6.dp
                )
            }

            // Theme Name
            Text(
                text = theme.displayName,
                style = Typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1
            )

            // Status Badge
            when {
                isEquipped -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StarGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF101626)
                        )
                    }
                }
                isUnlocked -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentPrimary.copy(alpha = 0.2f))
                            .border(1.dp, AccentPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OWNED",
                            style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = AccentPrimary
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF271C0B))
                            .border(1.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Real3DGoldCoin(size = 10.dp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "150",
                                style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = StarGold
                            )
                        }
                    }
                }
            }
        }
    }
}
