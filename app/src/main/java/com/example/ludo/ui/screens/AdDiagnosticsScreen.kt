package com.example.ludo.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.ads.AdDiagnosticsTracker
import com.example.ludo.ads.FormatDiagnostics
import com.example.ludo.ads.LudoAds
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ludo.ui.components.GlassCard
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

/**
 * Temporary DEBUG-only Ad Diagnostics Screen.
 * Displays real-time SDK initialization status, Firebase Remote Config status,
 * TEST/LIVE mode, and loading/loaded/failed status for all 6 ad formats.
 * Includes direct TEST buttons to test loading and showing each ad format.
 */
@Composable
fun AdDiagnosticsScreen(
    onBackClick: () -> Unit
) {
    val diagState by AdDiagnosticsTracker.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    var testActionMessage by remember { mutableStateOf("Tap any button below to test ad loading/display") }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ad_diagnostics_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, CircleShape)
                            .clickable { onBackClick() }
                            .testTag("diag_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AD MOB DIAGNOSTICS",
                                style = Typography.titleLarge.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Real-time Google AdMob Test Ad Engine Monitor",
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                // Global System Status Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SYSTEM & SDK INITIALIZATION",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = AccentPrimary
                        )

                        StatusRow(
                            label = "MobileAds SDK",
                            status = if (diagState.isSdkInitialized) "READY (Initialized)" else "INITIALIZING...",
                            isSuccess = diagState.isSdkInitialized,
                            extra = diagState.sdkInitMessage
                        )

                        StatusRow(
                            label = "Firebase Remote Config",
                            status = if (diagState.isFirebaseInitialized) "ACTIVE" else "OFFLINE (Local Defaults)",
                            isSuccess = true,
                            extra = if (diagState.isFirebaseInitialized) "Fetched config" else "Graceful offline fallback"
                        )

                        StatusRow(
                            label = "Ad Engine Mode",
                            status = if (diagState.isLiveMode) "LIVE (Production)" else "TEST (Official Sample IDs)",
                            isSuccess = true,
                            extra = "Mode: ${diagState.adsMode}"
                        )
                    }
                }

                // Interactive Action Message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F203D))
                        .border(1.dp, Color(0xFF1E3A68), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Status: $testActionMessage",
                        style = Typography.bodySmall.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFFE2E8F0)
                    )
                }

                // Section Header
                Text(
                    text = "ALL 6 AD FORMATS (STATUS & TEST ACTIONS)",
                    style = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
                    color = AccentPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )

                // 1. App Open Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.appOpenStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting App Open Test Ad..."
                        LudoAds.appOpenAdManager.loadAd {
                            testActionMessage = "App Open Test Ad Loaded!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying App Open Test Ad..."
                            LudoAds.appOpenAdManager.showAdIfAvailable(activity, forceShow = true) {
                                testActionMessage = "App Open Test Ad dismissed"
                            }
                        } else {
                            testActionMessage = "Error: Activity is null"
                        }
                    }
                )

                // 2. Banner Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.bannerStatus,
                    onLoadClick = null,
                    onShowClick = null
                )

                // 3. Interstitial Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.interstitialStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Interstitial Test Ad..."
                        LudoAds.interstitialAdManager.loadAd {
                            testActionMessage = "Interstitial Test Ad Loaded!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying Interstitial Test Ad..."
                            LudoAds.showInterstitial(activity, forceShow = true) {
                                testActionMessage = "Interstitial Test Ad closed"
                            }
                        } else {
                            testActionMessage = "Error: Activity is null"
                        }
                    }
                )

                // 4. Rewarded Interstitial Format
                FormatDiagnosticCard(
                    diagnostics = diagState.rewardedInterstitialStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Rewarded Interstitial Test Ad..."
                        LudoAds.rewardedInterstitialAdManager.loadAd {
                            testActionMessage = "Rewarded Interstitial Test Ad Loaded!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying Rewarded Interstitial Test Ad..."
                            LudoAds.showRewardedInterstitialAd(
                                activity = activity,
                                onRewardEarned = { item ->
                                    testActionMessage = "REWARD EARNED: ${item.type} x${item.amount}"
                                },
                                onAdClosed = { earned ->
                                    testActionMessage = "Rewarded Interstitial closed. Earned: $earned"
                                }
                            )
                        } else {
                            testActionMessage = "Error: Activity is null"
                        }
                    }
                )

                // 5. Rewarded Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.rewardedStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Rewarded Video Test Ad..."
                        LudoAds.rewardedAdManager.loadAd {
                            testActionMessage = "Rewarded Video Test Ad Loaded!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying Rewarded Video Test Ad..."
                            LudoAds.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { item ->
                                    testActionMessage = "REWARD EARNED: ${item.type} x${item.amount}"
                                },
                                onAdClosed = { earned ->
                                    testActionMessage = "Rewarded Video closed. Earned: $earned"
                                }
                            )
                        } else {
                            testActionMessage = "Error: Activity is null"
                        }
                    }
                )

                // 6. Native Advanced Format
                FormatDiagnosticCard(
                    diagnostics = diagState.nativeStatus,
                    onLoadClick = null,
                    onShowClick = null
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    status: String,
    isSuccess: Boolean,
    extra: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            Text(text = extra, style = Typography.labelSmall.copy(fontSize = 11.sp), color = TextMuted)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSuccess) StatusSuccess.copy(alpha = 0.15f) else StatusDanger.copy(alpha = 0.15f))
                .border(1.dp, if (isSuccess) StatusSuccess else StatusDanger, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status,
                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = if (isSuccess) StatusSuccess else StatusDanger
            )
        }
    }
}

@Composable
private fun FormatDiagnosticCard(
    diagnostics: FormatDiagnostics,
    onLoadClick: (() -> Unit)? = null,
    onShowClick: (() -> Unit)? = null
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Format Title + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = diagnostics.formatName,
                    style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                val badgeColor = when (diagnostics.status) {
                    "LOADED" -> StatusSuccess
                    "LOADING" -> StatusWarning
                    "FAILED" -> StatusDanger
                    "SHOWING" -> AccentPrimary
                    "SHOWN/DISMISSED" -> Color(0xFF60A5FA)
                    else -> TextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = diagnostics.status,
                        style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
            }

            // Unit ID
            Text(
                text = "AdUnit: ${diagnostics.adUnitId.ifEmpty { "Default Test ID" }}",
                style = Typography.bodySmall.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                color = if (diagnostics.isTestId) Color(0xFF818CF8) else Color(0xFFFBBF24)
            )

            // Error or response info
            if (diagnostics.lastError.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusDanger.copy(alpha = 0.1f))
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Error (${diagnostics.errorCode ?: -1}): ${diagnostics.lastError}",
                        style = Typography.labelSmall.copy(fontSize = 11.sp, color = StatusDanger)
                    )
                }
            }

            // Action Buttons
            if (onLoadClick != null || onShowClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onLoadClick != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                .clickable { onLoadClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reload Ad", style = Typography.labelMedium.copy(fontSize = 11.sp), color = Color.White)
                            }
                        }
                    }

                    if (onShowClick != null) {
                        val isReady = diagnostics.status == "LOADED"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isReady) AccentPrimary else SurfaceElevated)
                                .border(1.dp, if (isReady) AccentPrimary else BorderHairline, RoundedCornerShape(8.dp))
                                .clickable { onShowClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.PlayCircle,
                                    contentDescription = null,
                                    tint = if (isReady) Color(0xFF080F22) else AccentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Test Show",
                                    style = Typography.labelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = if (isReady) Color(0xFF080F22) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
