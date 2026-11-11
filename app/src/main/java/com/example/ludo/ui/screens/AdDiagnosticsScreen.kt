package com.example.ludo.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import com.example.ludo.ads.BannerAdView
import com.example.ludo.ads.FormatDiagnostics
import com.example.ludo.ads.LudoAds
import com.example.ludo.ads.NativeAdCard
import com.example.ludo.ads.RemoteConfigManager
import com.example.ludo.ui.components.GameBackground
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
 * Production-ready Ad Diagnostics & Testing Screen.
 * Displays real-time SDK initialization status, Firebase Remote Config status,
 * TEST vs LIVE mode switcher, and loading/loaded/failed status for all 6 ad formats.
 * Includes direct action buttons to reload ads with Activity context, trigger companion fallbacks,
 * and test-display every format.
 */
@Composable
fun AdDiagnosticsScreen(
    onBackClick: () -> Unit
) {
    val diagState by AdDiagnosticsTracker.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    var testActionMessage by remember { mutableStateOf("Ready. Tap any action button below to test ad delivery.") }
    var isReloadingAll by remember { mutableStateOf(false) }
    var showBannerPreview by remember { mutableStateOf(false) }
    var showNativePreview by remember { mutableStateOf(false) }
    var showHelpCard by remember { mutableStateOf(true) }

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

                    Column(modifier = Modifier.weight(1f)) {
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
                            extra = if (diagState.isFirebaseInitialized) "Remote config synchronized" else "Safe offline fallback active"
                        )

                        StatusRow(
                            label = "Ad Engine Mode",
                            status = if (diagState.isLiveMode) "LIVE (User IDs)" else "TEST (Official Sample IDs)",
                            isSuccess = true,
                            extra = "Active Mode: ${diagState.adsMode.uppercase()}"
                        )
                    }
                }

                // Global Control Buttons: "Reload All Formats" & "Mode Switcher"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reload All Ads
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPrimary)
                            .clickable(enabled = !isReloadingAll) {
                                isReloadingAll = true
                                testActionMessage = "Reloading all 4 full-screen formats with staggered delays..."
                                LudoAds.reloadAllAds(activity) {
                                    isReloadingAll = false
                                    testActionMessage = "All ad formats reload cycle finished!"
                                }
                            }
                            .testTag("diag_reload_all_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isReloadingAll) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF080F22),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF080F22),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isReloadingAll) "Pacing..." else "Reload All Ads",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF080F22)
                            )
                        }
                    }

                    // Mode Switcher (TEST <-> LIVE)
                    val isLive = diagState.isLiveMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, if (isLive) Color(0xFFFBBF24) else Color(0xFF818CF8), RoundedCornerShape(10.dp))
                            .clickable {
                                val nextMode = !isLive
                                RemoteConfigManager.setDebugMode(nextMode)
                                testActionMessage = if (nextMode) {
                                    "Switched to LIVE (Production User IDs). Note: Fresh live IDs require Google AdMob activation."
                                } else {
                                    "Switched to TEST (Official Google Sample IDs)."
                                }
                            }
                            .testTag("diag_switch_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                tint = if (isLive) Color(0xFFFBBF24) else Color(0xFF818CF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLive) "Use Test IDs" else "Use Live IDs",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // Interactive Action Message Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F203D))
                        .border(1.dp, Color(0xFF1E3A68), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "STATUS FEED:",
                            style = Typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = testActionMessage,
                            style = Typography.bodySmall.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                // Error (3) Explanation & Guidance Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHelpCard = !showHelpCard },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Why AdMob Error (3) 'No Fill' Occurs",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Text(
                                text = if (showHelpCard) "Hide" else "Show",
                                style = Typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        AnimatedVisibility(visible = showHelpCard) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "• Error 3 means the request reached Google AdMob servers, but no test ad creative was filled.",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                                Text(
                                    text = "• Test device registered: Emulator ID is now configured in RequestConfiguration.",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                                Text(
                                    text = "• Activity context: Full-screen video ads now pass the Activity context for window metrics.",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                                Text(
                                    text = "• Paced loading: Requests are staggered to avoid AdMob's concurrent burst rate-limits.",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                                Text(
                                    text = "• Video fallbacks: If the static interstitial or native unit has temporary no-fill, companion video units load automatically.",
                                    style = Typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                            }
                        }
                    }
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
                        LudoAds.appOpenAdManager.loadAd(activity, forceReload = true) {
                            testActionMessage = "App Open Test Ad successfully LOADED!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying App Open Test Ad..."
                            LudoAds.appOpenAdManager.showAdIfAvailable(activity, forceShow = true) {
                                testActionMessage = "App Open Test Ad dismissed"
                            }
                        } else {
                            testActionMessage = "Error: Activity context is not available"
                        }
                    }
                )

                // 2. Banner Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.bannerStatus,
                    onLoadClick = null,
                    onShowClick = {
                        showBannerPreview = !showBannerPreview
                    },
                    showButtonLabel = if (showBannerPreview) "Hide Banner" else "Preview Banner"
                )

                if (showBannerPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        BannerAdView(hasCardContainer = false)
                    }
                }

                // 3. Interstitial Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.interstitialStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Interstitial Test Ad..."
                        LudoAds.interstitialAdManager.loadAd(activity, forceReload = true) {
                            testActionMessage = "Interstitial Test Ad successfully LOADED!"
                        }
                    },
                    onShowClick = {
                        if (activity != null) {
                            testActionMessage = "Displaying Interstitial Test Ad..."
                            LudoAds.showInterstitial(activity, forceShow = true) {
                                testActionMessage = "Interstitial Test Ad closed"
                            }
                        } else {
                            testActionMessage = "Error: Activity context is not available"
                        }
                    }
                )

                // 4. Rewarded Interstitial Format
                FormatDiagnosticCard(
                    diagnostics = diagState.rewardedInterstitialStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Rewarded Interstitial Test Ad..."
                        LudoAds.rewardedInterstitialAdManager.loadAd(activity, forceReload = true) {
                            testActionMessage = "Rewarded Interstitial Test Ad successfully LOADED!"
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
                            testActionMessage = "Error: Activity context is not available"
                        }
                    }
                )

                // 5. Rewarded Ad Format
                FormatDiagnosticCard(
                    diagnostics = diagState.rewardedStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Rewarded Video Test Ad..."
                        LudoAds.rewardedAdManager.loadAd(activity, forceReload = true) {
                            testActionMessage = "Rewarded Video Test Ad successfully LOADED!"
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
                            testActionMessage = "Error: Activity context is not available"
                        }
                    }
                )

                // 6. Native Advanced Format
                FormatDiagnosticCard(
                    diagnostics = diagState.nativeStatus,
                    onLoadClick = {
                        testActionMessage = "Requesting Native Advanced Test Ad..."
                        LudoAds.testLoadNativeAd(
                            context = activity ?: context,
                            onLoaded = {
                                testActionMessage = "Native Advanced Test Ad successfully LOADED!"
                            },
                            onFailed = { msg ->
                                testActionMessage = msg
                            }
                        )
                    },
                    onShowClick = {
                        showNativePreview = !showNativePreview
                    },
                    showButtonLabel = if (showNativePreview) "Hide Preview" else "Preview Native"
                )

                if (showNativePreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        NativeAdCard()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
    onShowClick: (() -> Unit)? = null,
    showButtonLabel: String = "Test Show"
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
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Error (${diagnostics.errorCode ?: -1}): ${diagnostics.lastError}",
                            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusDanger)
                        )
                        if (diagnostics.errorCode == 3) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Code 3 = No Fill. Emulator test device registration and video fallback are now configured. Tap 'Reload Ad' with an active Activity window.",
                                style = Typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted)
                            )
                        }
                    }
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
                                    imageVector = if (showButtonLabel.contains("Preview") || showButtonLabel.contains("Banner")) {
                                        Icons.Outlined.Visibility
                                    } else {
                                        Icons.Outlined.PlayCircle
                                    },
                                    contentDescription = null,
                                    tint = if (isReady) Color(0xFF080F22) else AccentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    showButtonLabel,
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
