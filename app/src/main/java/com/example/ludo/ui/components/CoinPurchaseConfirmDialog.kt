package com.example.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Reusable modal for confirming coin deduction before unlocking shop items.
 * Guarantees zero unintentional coin deductions by explicitly asking confirmation:
 * - Shows visual preview of the item being unlocked.
 * - Shows price in Real 3D Gold Coins.
 * - Shows current balance and projected balance after purchase.
 * - Handles insufficient coins scenario with option to watch a video ad for +50 coins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinPurchaseConfirmDialog(
    itemName: String,
    itemCategory: String, // e.g. "Board Theme" or "Pawn Skin"
    itemDescription: String? = null,
    priceCoins: Int,
    currentCoins: Int,
    previewContent: @Composable () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onWatchAd: (() -> Unit)? = null
) {
    val hasEnoughCoins = currentCoins >= priceCoins
    val remainingCoins = currentCoins - priceCoins

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C1322))
            .border(1.2.dp, if (hasEnoughCoins) StarGold.copy(alpha = 0.5f) else StatusWarning.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("coin_purchase_confirm_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (hasEnoughCoins) "CONFIRM UNLOCK" else "INSUFFICIENT COINS",
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        ),
                        color = if (hasEnoughCoins) StarGold else StatusWarning
                    )
                    Text(
                        text = itemCategory,
                        style = Typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, BorderHairline, CircleShape)
                        .clickable { onDismiss() }
                        .testTag("close_confirm_dialog"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Preview Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                previewContent()
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item Name
            Text(
                text = itemName,
                style = Typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            if (itemDescription != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = itemDescription,
                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Unlock Cost Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Unlock Cost",
                            style = Typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Real3DGoldCoin(size = 16.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$priceCoins Coins",
                                style = Typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = StarGold
                            )
                        }
                    }

                    // Balance Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Balance",
                            style = Typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Real3DGoldCoin(size = 14.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentCoins Coins",
                                style = Typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (hasEnoughCoins) TextPrimary else StatusWarning
                            )
                        }
                    }

                    if (hasEnoughCoins) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderHairline)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining Balance",
                                style = Typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Real3DGoldCoin(size = 14.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$remainingCoins Coins",
                                    style = Typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AccentPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            if (hasEnoughCoins) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Confirm & Deduct Coins
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StarGold)
                            .clickable { onConfirm() }
                            .testTag("confirm_coin_deduction_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color(0xFF101626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirm & Unlock ($priceCoins Coins)",
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF101626)
                            )
                        }
                    }

                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                            .clickable { onDismiss() }
                            .testTag("cancel_coin_deduction_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = Typography.labelMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            } else {
                // Not enough coins: Watch ad button or cancel
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shortfall = priceCoins - currentCoins
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusWarning.copy(alpha = 0.12f))
                            .border(1.dp, StatusWarning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You need $shortfall more coins to unlock this item.",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = StatusWarning
                            )
                        }
                    }

                    if (onWatchAd != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentPrimary)
                                .clickable { onWatchAd() }
                                .testTag("watch_ad_for_coins_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF090E1A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Watch Video to Earn +50 Coins",
                                    style = Typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF090E1A)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                            .clickable { onDismiss() }
                            .testTag("dismiss_not_enough_coins_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back",
                            style = Typography.labelMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
