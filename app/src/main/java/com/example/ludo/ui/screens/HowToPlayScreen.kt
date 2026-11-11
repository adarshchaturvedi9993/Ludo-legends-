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
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameTopBar
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceCard
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
                title = "Rules of Ludo",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RuleSectionCard(
                    number = "1",
                    title = "Leaving the Yard",
                    badge = "ROLL 6",
                    description = "To move a pawn from your corner yard onto the starting track tile, you must roll a 6. Rolling a 6 also awards an immediate extra turn."
                )

                RuleSectionCard(
                    number = "2",
                    title = "Capturing Opponents",
                    badge = "BONUS TURN",
                    description = "If your pawn lands on the exact track tile occupied by an opponent's pawn, the opponent's pawn is captured and sent back to their yard. You earn an immediate bonus roll!"
                )

                RuleSectionCard(
                    number = "3",
                    title = "Safe Star Zones",
                    badge = "PROTECTED",
                    description = "There are 8 designated Safe Star tiles on the perimeter track (marked with gold and white stars). Pawns standing on a safe tile cannot be captured."
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
                    description = "Tap highlighted pawns directly on the board to move them. If only one pawn can move legally, it will move automatically for rapid play."
                )

                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_HOW_TO_PLAY,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = SurfaceCard,
                    containerBorderColor = BorderHairline
                )

                AppFooter(modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary.copy(alpha = 0.15f))
                            .border(1.dp, AccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = AccentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = TextSecondary
            )
        }
    }
}
