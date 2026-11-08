package com.example.ludo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
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
 * Premium Dark Pawn Selection Bottom Sheet:
 *
 * Requirements:
 * 1. Shows when multiple pawns are eligible for movement.
 * 2. Title: "CHOOSE YOUR PAWN".
 * 3. Subtitle: "You rolled [diceValue]. Select a pawn to move."
 * 4. Displays only eligible pawns with clear movement descriptors:
 *    "Pawn 1 — Move 4 steps."
 *    "Pawn 2 — Move 4 steps."
 *    "Pawn 3 — Move 4 steps."
 * 5. Visual 2.5D pawn icon, status badge, and crisp selection tap target.
 * 6. Responsive dark design with backdrop scrim.
 */
@Composable
fun PawnSelectionBottomSheet(
    visible: Boolean,
    player: Player,
    diceValue: Int,
    eligibleTokenIds: Set<Int>,
    onSelectToken: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val eligibleTokens = remember(player.tokens, eligibleTokenIds) {
        player.tokens.filter { eligibleTokenIds.contains(it.id) }
    }

    if (eligibleTokens.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("pawn_selection_bottom_sheet_root")
    ) {
        // Semi-transparent backdrop scrim (clicking dismisses to allow clicking board directly)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("pawn_selection_scrim")
        )

        // Slide-up sheet panel
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(SurfacePanel)
                    .border(
                        width = 1.dp,
                        color = BorderActive,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("pawn_selection_sheet")
            ) {
                // Drag / handle bar
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderHairline)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header Row: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CHOOSE YOUR PAWN",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = StarGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You rolled $diceValue. Select a pawn to move.",
                            style = Typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, CircleShape)
                            .testTag("close_pawn_selection_button")
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

                // Eligible Options List
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    eligibleTokens.forEach { token ->
                        val pawnNumber = token.id + 1
                        val movementDescription = when (token.state) {
                            TokenState.YARD -> "Pawn $pawnNumber — Move $diceValue steps."
                            TokenState.TRACK -> "Pawn $pawnNumber — Move $diceValue steps."
                            TokenState.HOME_STRETCH -> "Pawn $pawnNumber — Move $diceValue steps."
                            TokenState.FINISHED -> "Pawn $pawnNumber — Finished"
                        }

                        val locationDetail = when (token.state) {
                            TokenState.YARD -> "Enter board from yard"
                            TokenState.TRACK -> {
                                if (token.stepsTraveled + diceValue == 56) "Finish pawn into Home Goal!"
                                else if (token.stepsTraveled + diceValue > 50) "Enter colored home stretch"
                                else "Advance along track"
                            }
                            TokenState.HOME_STRETCH -> {
                                if (token.homeStep + diceValue == 6) "Reach final Home Goal!"
                                else "Advance ${diceValue} steps in home stretch"
                            }
                            TokenState.FINISHED -> "Already victorious"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onSelectToken(token.id) }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("pawn_option_${token.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Pawn visual preview & numbering
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceBackground)
                                        .border(1.2.dp, player.color.primaryColor.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TurnedPawn(
                                        color = player.color,
                                        isMovable = true,
                                        isFinished = false,
                                        size = 28.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = movementDescription,
                                        style = Typography.titleMedium.copy(fontSize = 14.sp),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = locationDetail,
                                        style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                        color = if (token.state == TokenState.YARD || token.stepsTraveled + diceValue >= 56) AccentPrimary else TextMuted
                                    )
                                }
                            }

                            // Right: Select arrow indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(player.color.primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, player.color.primaryColor.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Select",
                                    tint = player.color.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
