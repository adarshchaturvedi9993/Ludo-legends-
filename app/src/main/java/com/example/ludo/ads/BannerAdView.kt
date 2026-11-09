package com.example.ludo.ads

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.TextMuted
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Premium Adaptive Banner Ad Composable.
 * Stretches naturally to match card layouts with rounded corners and subtle styling.
 * Respects Firebase Remote Config placement keys, logs detailed diagnostics, and prevents layout jumps.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    placementKey: String? = null,
    hasCardContainer: Boolean = true,
    cornerRadius: Dp = 14.dp,
    containerBackground: Color = Color(0xFF0D1D38),
    containerBorderColor: Color = Color(0xFF293B5B)
) {
    val configState by RemoteConfigManager.configState.collectAsState()

    if (!configState.isAdsEnabled) {
        return
    }

    if (placementKey != null && !RemoteConfigManager.isPlacementEnabled(placementKey)) {
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val adUnitId = remember(configState.isLiveMode, configState.adsMode) {
        RemoteConfigManager.getBannerAdUnitId()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(300)
        val adaptiveSize = remember(widthDp) {
            try {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
            } catch (_: Exception) {
                AdSize.BANNER
            }
        }

        var isAdLoaded by remember { mutableStateOf(false) }

        val adView = remember(adUnitId, adaptiveSize) {
            AdDiagnosticsTracker.recordLoading("Banner", adUnitId)
            Log.d("BannerAdView", "[AdMob Request] Banner -> UnitId: $adUnitId (${adaptiveSize.width}x${adaptiveSize.height})")

            AdView(context).apply {
                setAdSize(adaptiveSize)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                        val responseSummary = responseInfo?.toString() ?: ""
                        Log.d("BannerAdView", "[AdMob Loaded] Banner -> Success! $adUnitId (${adaptiveSize.width}x${adaptiveSize.height})")
                        AdDiagnosticsTracker.recordLoaded("Banner", adUnitId, responseSummary)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isAdLoaded = false
                        Log.w("BannerAdView", "[AdMob Failed] Banner -> Code: ${error.code}, Domain: ${error.domain}, Message: ${error.message}")
                        AdDiagnosticsTracker.recordFailed(
                            format = "Banner",
                            unitId = adUnitId,
                            code = error.code,
                            domain = error.domain,
                            message = error.message,
                            responseInfoSummary = error.responseInfo?.toString() ?: ""
                        )
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(lifecycleOwner, adView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_DESTROY -> adView.destroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isAdLoaded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically()
        ) {
            val containerShape = RoundedCornerShape(cornerRadius)

            if (hasCardContainer) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(containerShape)
                        .background(containerBackground)
                        .border(1.dp, containerBorderColor, containerShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Discreet attribution label
                        Text(
                            text = "SPONSORED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextMuted.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        AndroidView(
                            factory = { adView },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                        )
                    }
                }
            } else {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
