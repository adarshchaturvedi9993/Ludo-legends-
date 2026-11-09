package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.GlassCard
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

@Composable
fun HowToPlayScreen(
    onBackClick: () -> Unit
) {
    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("how_to_play_screen")
        ) {
            GameTopBar(
                title = "Rules & How To Play",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RuleSectionCard(
                    number = "1",
                    title = "Leaving the Yard",
                    badge = "ROLL 6",
                    description = "To move a pawn from your corner yard onto the starting square, you must roll a 6. Rolling a 6 also awards an immediate extra roll."
                )

                RuleSectionCard(
                    number = "2",
                    title = "Capturing Opponents",
                    badge = "BONUS TURN",
                    description = "If your pawn lands on the exact track tile occupied by an opponent's pawn, the opponent's pawn is captured and sent back to their yard. You earn an immediate bonus turn!"
                )

                RuleSectionCard(
                    number = "3",
                    title = "Safe Star Zones",
                    badge = "PROTECTED",
                    description = "There are 8 designated Safe Star tiles on the perimeter track (marked with gold and white stars). Pawns standing on a safe tile can never be captured."
                )

                RuleSectionCard(
                    number = "4",
                    title = "Home Run & Exact Roll",
                    badge = "EXACT VALUE",
                    description = "After completing a full circuit of the perimeter, pawns turn into their colored home corridor. Pawns require the exact dice number to reach the central triangle."
                )

                RuleSectionCard(
                    number = "5",
                    title = "Three Consecutive Sixes",
                    badge = "FORFEIT",
                    description = "Rolling 6 three consecutive times forfeits the third roll to prevent monopolization, passing the turn immediately to the next player."
                )

                RuleSectionCard(
                    number = "6",
                    title = "Pawn Selection & Auto-Move",
                    badge = "DIRECT TAP",
                    description = "Tap glowing pawns directly on the board to move them. If only one pawn can move legally, it will move automatically for rapid play."
                )

                com.example.ludo.ads.BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_HOW_TO_PLAY,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = androidx.compose.ui.graphics.Color(0xFF0D1D38),
                    containerBorderColor = androidx.compose.ui.graphics.Color(0xFF293B5B)
                )

                AppFooter(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
            }
        }
    }
}

@Composable
private fun RuleSectionCard(
    number: String,
    title: String,
    badge: String,
    description: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary.copy(alpha = 0.15f))
                            .border(1.dp, AccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = Typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = Typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = TextSecondary
            )
        }
    }
}
