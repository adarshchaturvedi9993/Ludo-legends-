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
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.VideoLibrary
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ads.RemoteConfigManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameDialog
import com.example.ludo.ui.components.GameTopBar
import com.example.ludo.ui.components.GlassCard
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Production Settings Screen:
 * Audio, Haptics, Gameplay (Auto-move, Turn timer), Accessibility (Color-blind symbols, Reduce motion)
 */
@Composable
fun SettingsScreen(
    gamePreferences: GamePreferences,
    soundManager: SoundManager,
    onBackClick: () -> Unit,
    onWatchSupportAd: (() -> Unit)? = null,
    onDiagnosticsClick: (() -> Unit)? = null
) {
    var isSoundOn by remember { mutableStateOf(gamePreferences.isSoundEnabled) }
    var isMusicOn by remember { mutableStateOf(gamePreferences.isMusicEnabled) }
    var isHapticsOn by remember { mutableStateOf(gamePreferences.isHapticsEnabled) }
    var isAutoMoveOn by remember { mutableStateOf(gamePreferences.isAutoMoveSinglePawnEnabled) }
    var isTurnTimerOn by remember { mutableStateOf(gamePreferences.isTurnTimerEnabled) }
    var isColorBlindOn by remember { mutableStateOf(gamePreferences.isColorBlindModeEnabled) }
    var isReduceMotionOn by remember { mutableStateOf(gamePreferences.isReduceMotionEnabled) }
    var showClearSavedDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    GameBackground(enableTwinkle = !isReduceMotionOn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("settings_screen")
        ) {
            GameTopBar(
                title = "Preferences",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Audio & Haptics Section
                Text(
                    text = "AUDIO & FEEDBACK",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingSwitchRow(
                            icon = Icons.AutoMirrored.Outlined.VolumeUp,
                            title = "Sound Effects",
                            description = "Tactile clacks, roll bounces, and pop sounds",
                            checked = isSoundOn,
                            testTag = "sound_switch",
                            onCheckedChange = { checked ->
                                isSoundOn = checked
                                gamePreferences.isSoundEnabled = checked
                                soundManager.isSoundEnabled = checked
                                if (checked) soundManager.playButtonTap()
                            }
                        )

                        DividerLine()

                        SettingSwitchRow(
                            icon = Icons.Outlined.MusicNote,
                            title = "Background Music",
                            description = "Calming ambient acoustic kalimba melody",
                            checked = isMusicOn,
                            testTag = "music_switch",
                            onCheckedChange = { checked ->
                                isMusicOn = checked
                                gamePreferences.isMusicEnabled = checked
                                soundManager.isMusicEnabled = checked
                                if (checked) soundManager.playButtonTap()
                            }
                        )

                        DividerLine()

                        SettingSwitchRow(
                            icon = Icons.Outlined.Vibration,
                            title = "Haptic Vibrations",
                            description = "Tactile clicks on pawn pick, roll, and victory",
                            checked = isHapticsOn,
                            testTag = "haptics_switch",
                            onCheckedChange = { checked ->
                                isHapticsOn = checked
                                gamePreferences.isHapticsEnabled = checked
                                soundManager.isHapticsEnabled = checked
                                if (checked) soundManager.playButtonTap()
                            }
                        )
                    }
                }

                // 2. Gameplay Controls Section
                Text(
                    text = "GAMEPLAY MECHANICS",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Outlined.AutoMode,
                            title = "Auto-Move Single Pawn",
                            description = "Automatically moves pawn after 400ms if only 1 move is legal",
                            checked = isAutoMoveOn,
                            testTag = "auto_move_switch",
                            onCheckedChange = { checked ->
                                isAutoMoveOn = checked
                                gamePreferences.isAutoMoveSinglePawnEnabled = checked
                                soundManager.playButtonTap()
                            }
                        )

                        DividerLine()

                        SettingSwitchRow(
                            icon = Icons.Outlined.HourglassBottom,
                            title = "Turn Timer (15s)",
                            description = "Circular ring countdown with automated action on timeout",
                            checked = isTurnTimerOn,
                            testTag = "turn_timer_switch",
                            onCheckedChange = { checked ->
                                isTurnTimerOn = checked
                                gamePreferences.isTurnTimerEnabled = checked
                                soundManager.playButtonTap()
                            }
                        )
                    }
                }

                // 3. Accessibility Section
                Text(
                    text = "ACCESSIBILITY & MOTION",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Outlined.Visibility,
                            title = "Color-Blind Symbols",
                            description = "Draws distinct symbols (● ▲ ■ ★) on pawns for clear identification",
                            checked = isColorBlindOn,
                            testTag = "color_blind_switch",
                            onCheckedChange = { checked ->
                                isColorBlindOn = checked
                                gamePreferences.isColorBlindModeEnabled = checked
                                soundManager.playButtonTap()
                            }
                        )

                        DividerLine()

                        SettingSwitchRow(
                            icon = Icons.Outlined.Animation,
                            title = "Reduce Motion",
                            description = "Minimizes particle twinkling and high-frequency animations",
                            checked = isReduceMotionOn,
                            testTag = "reduce_motion_switch",
                            onCheckedChange = { checked ->
                                isReduceMotionOn = checked
                                gamePreferences.isReduceMotionEnabled = checked
                                soundManager.playButtonTap()
                            }
                        )
                    }
                }

                // 4. Data & Session Management
                Text(
                    text = "DATA MANAGEMENT",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("100% Offline & Private", style = Typography.bodyLarge, color = Color.White)
                                Text("All state is safely stored in local device storage", style = Typography.bodyMedium, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        GameButton(
                            text = "Clear Preserved Match",
                            onClick = { showClearSavedDialog = true },
                            style = GameButtonStyle.SECONDARY_GLASS,
                            icon = Icons.Outlined.DeleteSweep,
                            height = 48.dp,
                            testTag = "clear_saved_game_button"
                        )
                    }
                }

                // 5. Ad Network & Remote Config Support
                val adConfig by RemoteConfigManager.configState.collectAsState()
                Text(
                    text = "ADS & REMOTE CONFIG",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Campaign,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (adConfig.isAdsEnabled) "AdMob Network: Active" else "AdMob Network: Disabled",
                                    style = Typography.bodyLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = if (adConfig.isLiveMode) "Mode: Live (Production IDs Active)" else "Mode: Test (Google Official Sample IDs)",
                                    style = Typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = if (adConfig.isLiveMode) Color(0xFF00E676) else TextMuted
                                )
                            }
                        }

                        if (onWatchSupportAd != null && adConfig.isAdsEnabled) {
                            Spacer(modifier = Modifier.height(2.dp))
                            GameButton(
                                text = "Watch Bonus Video (Support Dev)",
                                subtitle = "+500 XP and 1 Lucky Reroll Perk",
                                onClick = onWatchSupportAd,
                                style = GameButtonStyle.SECONDARY_GLASS,
                                icon = Icons.Outlined.VideoLibrary,
                                height = 48.dp,
                                testTag = "watch_support_ad_button"
                            )
                        }

                        if (onDiagnosticsClick != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            GameButton(
                                text = "Open Ad Diagnostics Monitor",
                                subtitle = "Live status & test buttons for all 6 ad formats",
                                onClick = onDiagnosticsClick,
                                style = GameButtonStyle.SECONDARY_GLASS,
                                icon = Icons.Outlined.Visibility,
                                height = 48.dp,
                                testTag = "open_ad_diagnostics_button"
                            )
                        }
                    }
                }

                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_SETTINGS,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = Color(0xFF0D1D38),
                    containerBorderColor = Color(0xFF293B5B)
                )

                AppFooter(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
            }
        }

        // Clear Match Confirmation Dialog
        GameDialog(
            visible = showClearSavedDialog,
            title = "Clear Preserved Match?",
            onDismissRequest = { showClearSavedDialog = false },
            buttons = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameButton(
                        text = "Yes, Clear Session",
                        onClick = {
                            gamePreferences.clearSavedGame()
                            showClearSavedDialog = false
                            soundManager.playButtonTap()
                        },
                        style = GameButtonStyle.DANGER
                    )
                    GameButton(
                        text = "Cancel",
                        onClick = { showClearSavedDialog = false },
                        style = GameButtonStyle.SECONDARY_GLASS
                    )
                }
            }
        ) {
            Text(
                text = "This will remove the current in-progress match from saved memory.",
                style = Typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = Typography.bodyLarge, color = Color.White)
                Text(description, style = Typography.bodyMedium.copy(fontSize = 11.sp), color = TextMuted)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceBackground,
                checkedTrackColor = AccentPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderHairline)
    )
}
