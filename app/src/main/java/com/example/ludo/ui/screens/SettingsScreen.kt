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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.GamePreferences
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

@Composable
fun SettingsScreen(
    gamePreferences: GamePreferences,
    soundManager: SoundManager,
    onBackClick: () -> Unit
) {
    var isSoundOn by remember { mutableStateOf(gamePreferences.isSoundEnabled) }
    var isMusicOn by remember { mutableStateOf(gamePreferences.isMusicEnabled) }
    var isHapticsOn by remember { mutableStateOf(gamePreferences.isHapticsEnabled) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(horizontal = 20.dp)
            .testTag("settings_screen")
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfacePanel)
                    .border(1.dp, BorderHairline, CircleShape)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings & Rules",
                style = Typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Audio & Feedback Section
            Text(
                text = "Audio & Haptics",
                style = Typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfacePanel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Sound FX Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sound Effects", style = Typography.bodyLarge, color = TextPrimary)
                            Text("Tactile dice rolls and piece clacks", style = Typography.bodyMedium, color = TextMuted)
                        }
                    }
                    Switch(
                        checked = isSoundOn,
                        onCheckedChange = { checked ->
                            isSoundOn = checked
                            gamePreferences.isSoundEnabled = checked
                            soundManager.isSoundEnabled = checked
                            if (checked) soundManager.playButtonTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceBackground,
                            checkedTrackColor = AccentPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier.testTag("sound_switch")
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderHairline)
                )

                // Background Music Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Background Music", style = Typography.bodyLarge, color = TextPrimary)
                            Text("Subtle, ambient acoustic loop", style = Typography.bodyMedium, color = TextMuted)
                        }
                    }
                    Switch(
                        checked = isMusicOn,
                        onCheckedChange = { checked ->
                            isMusicOn = checked
                            gamePreferences.isMusicEnabled = checked
                            soundManager.isMusicEnabled = checked
                            if (checked) soundManager.playButtonTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceBackground,
                            checkedTrackColor = AccentPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier.testTag("music_switch")
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderHairline)
                )

                // Haptics Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Vibration,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Vibration", style = Typography.bodyLarge, color = TextPrimary)
                            Text("Haptic clicks on roll and capture", style = Typography.bodyMedium, color = TextMuted)
                        }
                    }
                    Switch(
                        checked = isHapticsOn,
                        onCheckedChange = { checked ->
                            isHapticsOn = checked
                            gamePreferences.isHapticsEnabled = checked
                            soundManager.isHapticsEnabled = checked
                            if (checked) soundManager.playButtonTap()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceBackground,
                            checkedTrackColor = AccentPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceElevated
                        ),
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authentic Rules Guide
            Text(
                text = "Ludo Rules Reference",
                style = Typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfacePanel)
                    .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RuleItem(
                    title = "Unlocking Tokens (Roll a 6)",
                    description = "Tokens start in the yard. Rolling a 6 moves a token onto the track starting square and awards an extra roll."
                )
                RuleItem(
                    title = "Capturing Opponents",
                    description = "Landing on an opponent's token sends it back to their yard and grants you a bonus turn. Safe star tiles cannot be captured."
                )
                RuleItem(
                    title = "Safe Star Zones",
                    description = "There are 8 marked stars on the board. Multiple tokens of any player can coexist on a safe tile without risk."
                )
                RuleItem(
                    title = "Exact Finish Required",
                    description = "Tokens entering the home stretch require the exact dice value to reach the center triangle. Overshooting is not allowed."
                )
                RuleItem(
                    title = "Three Sixes Rule",
                    description = "Rolling 6 three consecutive times forfeits the turn and passes immediately to the next player."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Offline Guarantee Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% Offline & Private",
                        style = Typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "No internet permissions, no trackers, zero ads.",
                        style = Typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RuleItem(title: String, description: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = Typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = description,
            style = Typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}
