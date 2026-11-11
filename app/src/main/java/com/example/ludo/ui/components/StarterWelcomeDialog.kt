package com.example.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.PlayerColor
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * First-Time User Induction Dialog:
 * Tactile, tournament-themed welcome bounty celebrating new player arrival.
 * Grants:
 * 1. 100 Gold Coins directly credited to in-game treasury.
 * 2. Ruby Royal Pawn Skin unlocked for free (Value: 25 coins).
 * 3. 2X First Match Bounty active on inaugural victory (+100 extra victory coins).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarterWelcomeDialog(
    onClaimClick: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C1322))
            .border(1.2.dp, StarGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("starter_welcome_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Category label & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEW CONTENDER INDUCTION",
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = StarGold
                    )
                    Text(
                        text = "Starter Welcome Bounty",
                        style = Typography.titleMedium.copy(
                            fontSize = 17.sp,
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
                        .testTag("close_welcome_dialog"),
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

            Spacer(modifier = Modifier.height(18.dp))

            // Center Visual: Preview of Ruby Royal Pawn + Gold Coin Chest
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TurnedPawn(
                    color = PlayerColor.RED,
                    isMovable = false,
                    isFinished = false,
                    size = 44.dp,
                    customPrimaryColor = Color(0xFFEF4444),
                    customAccentColor = Color(0xFFFCA5A5),
                    customDarkShade = Color(0xFF991B1B)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StarGold.copy(alpha = 0.2f))
                            .border(1.dp, StarGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STARTER TROPHY",
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = StarGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ruby Royal Jewel Pawn",
                        style = Typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Pre-unlocked for your roster",
                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Concrete Perks List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BountyPerkRow(
                    icon = Icons.Outlined.MonetizationOn,
                    iconTint = StarGold,
                    title = "+100 Gold Coins",
                    subtitle = "Instant treasury credit for custom themes & skins"
                )

                BountyPerkRow(
                    icon = Icons.Outlined.Palette,
                    iconTint = Color(0xFFF87171),
                    title = "Ruby Royal Pawn Skin",
                    subtitle = "Lustrous lacquer gem pawn unlocked forever"
                )

                BountyPerkRow(
                    icon = Icons.Outlined.EmojiEvents,
                    iconTint = AccentPrimary,
                    title = "2X First Match Bounty",
                    subtitle = "Double coin victory bounty on your inaugural win"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tactile Claim Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StarGold)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.Black),
                        onClick = onClaimClick
                    )
                    .testTag("claim_starter_bounty_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFF101626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Claim Starter Bounty (+100 Coins)",
                        style = Typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF101626)
                    )
                }
            }
        }
    }
}

@Composable
private fun BountyPerkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f))
                .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = StatusSuccess,
            modifier = Modifier.size(16.dp)
        )
    }
}
