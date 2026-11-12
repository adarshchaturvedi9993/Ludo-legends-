package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ads.RemoteConfigManager
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.GamePreferences
import com.example.ludo.ui.components.AppFooter
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GameDialog
import com.example.ludo.ui.components.GameTopBar
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * World-Class Settings Screen:
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
    var animIntensity by remember { mutableStateOf(gamePreferences.animationIntensity) }
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
                title = "Settings",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Audio & Haptics Section
                Text(
                    text = "AUDIO & FEEDBACK",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingSwitchRow(
                            icon = Icons.AutoMirrored.Outlined.VolumeUp,
                            title = "Sound Effects",
                            description = "Tactile clacks, roll bounces, and pawn steps",
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
                            description = "Relaxing ambient acoustic melody",
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
                            description = "Tactile feedback on dice roll and capture",
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
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Outlined.AutoMode,
                            title = "Auto-Move Single Pawn",
                            description = "Automatically moves pawn if only 1 move is legal",
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
                            description = "Countdown ring with auto-play on timeout",
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

                // 3. Accessibility & Performance Section
                Text(
                    text = "ACCESSIBILITY & PERFORMANCE",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingSegmentedRow(
                            icon = Icons.Outlined.Animation,
                            title = "Animation Intensity",
                            description = "Full: 60fps sweeps & particles • Reduced: fast hops • Off: instant",
                            options = listOf("Full", "Reduced", "Off"),
                            selectedIndex = when (animIntensity) {
                                com.example.ludo.data.AnimationIntensity.FULL -> 0
                                com.example.ludo.data.AnimationIntensity.REDUCED -> 1
                                com.example.ludo.data.AnimationIntensity.OFF -> 2
                            },
                            onSelect = { idx ->
                                val newIntensity = when (idx) {
                                    0 -> com.example.ludo.data.AnimationIntensity.FULL
                                    1 -> com.example.ludo.data.AnimationIntensity.REDUCED
                                    else -> com.example.ludo.data.AnimationIntensity.OFF
                                }
                                animIntensity = newIntensity
                                gamePreferences.animationIntensity = newIntensity
                                isReduceMotionOn = (newIntensity != com.example.ludo.data.AnimationIntensity.FULL)
                                soundManager.playButtonTap()
                            }
                        )

                        DividerLine()

                        SettingSwitchRow(
                            icon = Icons.Outlined.Visibility,
                            title = "Color-Blind Symbols",
                            description = "Draws distinct symbols (● ▲ ■ ★) on pawns for identification",
                            checked = isColorBlindOn,
                            testTag = "color_blind_switch",
                            onCheckedChange = { checked ->
                                isColorBlindOn = checked
                                gamePreferences.isColorBlindModeEnabled = checked
                                soundManager.playButtonTap()
                            }
                        )
                    }
                }

                // 4. Data & Session Management
                Text(
                    text = "DATA MANAGEMENT",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
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
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Offline & Private", style = Typography.bodyLarge, color = TextPrimary)
                                Text("Game data is stored locally on this device", style = Typography.bodyMedium.copy(fontSize = 11.sp), color = TextMuted)
                            }
                        }

                        GameButton(
                            text = "Clear Preserved Match",
                            onClick = { showClearSavedDialog = true },
                            style = GameButtonStyle.SECONDARY_GLASS,
                            icon = Icons.Outlined.DeleteSweep,
                            height = 46.dp,
                            testTag = "clear_saved_game_button"
                        )
                    }
                }

                // 5. Ad Network & Remote Config Support
                val adConfig by RemoteConfigManager.configState.collectAsState()
                Text(
                    text = "ADS & ENGINE STATUS",
                    style = Typography.labelLarge.copy(
                        fontSize = 11.sp,
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Campaign,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (adConfig.isAdsEnabled) "Ad Engine: Active" else "Ad Engine: Disabled",
                                    style = Typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (adConfig.isLiveMode) "Live Mode (Production)" else "Test Mode (Google Official Sample IDs)",
                                    style = Typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        if (onWatchSupportAd != null && adConfig.isAdsEnabled) {
                            GameButton(
                                text = "Watch Bonus Video",
                                subtitle = "+500 XP and 1 Lucky Reroll Perk",
                                onClick = onWatchSupportAd,
                                style = GameButtonStyle.SECONDARY_GLASS,
                                icon = Icons.Outlined.VideoLibrary,
                                height = 46.dp,
                                testTag = "watch_support_ad_button"
                            )
                        }

                        if (onDiagnosticsClick != null) {
                            GameButton(
                                text = "Ad Diagnostics Monitor",
                                subtitle = "Real-time engine status & format tests",
                                onClick = onDiagnosticsClick,
                                style = GameButtonStyle.SECONDARY_GLASS,
                                icon = Icons.Outlined.Visibility,
                                height = 46.dp,
                                testTag = "open_ad_diagnostics_button"
                            )
                        }
                    }
                }

                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp),
                    placementKey = com.example.ludo.ads.AdConstants.KEY_BANNER_SETTINGS,
                    hasCardContainer = true,
                    cornerRadius = 14.dp,
                    containerBackground = SurfaceCard,
                    containerBorderColor = BorderHairline
                )

                AppFooter(modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = Typography.bodyLarge, color = TextPrimary)
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

@Composable
private fun SettingSegmentedRow(
    icon: ImageVector,
    title: String,
    description: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = Typography.bodyLarge, color = TextPrimary)
                Text(description, style = Typography.bodyMedium.copy(fontSize = 11.sp), color = TextMuted)
            }
        }

        // Segmented selector buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated)
                .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { idx, opt ->
                val isSelected = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AccentPrimary else Color.Transparent)
                        .clickable { onSelect(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        style = Typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) SurfaceBackground else TextSecondary
                    )
                }
            }
        }
    }
}
