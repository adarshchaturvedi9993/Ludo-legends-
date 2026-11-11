package com.example.ludo.ads

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Production Native Advanced Ad Component.
 * Styled natively to harmonize with Ludo Legends' dark-navy and golden gaming aesthetic.
 * Strictly adheres to Google Play AdMob policy (Ad attribution badge, registered assets).
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    placementKey: String = AdConstants.KEY_NATIVE_HOME
) {
    val configState by RemoteConfigManager.configState.collectAsState()

    if (!configState.isAdsEnabled || !RemoteConfigManager.isPlacementEnabled(placementKey)) {
        return
    }

    val context = LocalContext.current
    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }

    val adUnitId = remember(configState.isLiveMode, configState.adsMode) {
        RemoteConfigManager.getNativeAdUnitId()
    }

    DisposableEffect(adUnitId) {
        AdDiagnosticsTracker.recordLoading("Native Advanced", adUnitId)
        Log.d("NativeAdCard", "[AdMob Request] Native Advanced -> AdUnitId: $adUnitId")

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                loadedNativeAd?.destroy()
                loadedNativeAd = ad
                val responseSummary = ad.responseInfo?.toString() ?: ""
                Log.d("NativeAdCard", "[AdMob Loaded] Native Advanced -> Success! Headline: ${ad.headline}")
                AdDiagnosticsTracker.recordLoaded("Native Advanced", adUnitId, responseSummary)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("NativeAdCard", "[AdMob Failed] Native Advanced -> Code: ${error.code}, Domain: ${error.domain}, Message: ${error.message}")
                    AdDiagnosticsTracker.recordFailed(
                        format = "Native Advanced",
                        unitId = adUnitId,
                        code = error.code,
                        domain = error.domain,
                        message = error.message,
                        responseInfoSummary = error.responseInfo?.toString() ?: ""
                    )

                    // If primary test native ad returned No Fill (error 3), try companion test native video
                    if (!RemoteConfigManager.isLiveMode() && adUnitId == AdConstants.TEST_NATIVE) {
                        val fallbackId = AdConstants.TEST_NATIVE_VIDEO
                        AdDiagnosticsTracker.recordLoading("Native Advanced", fallbackId)
                        val fallbackLoader = AdLoader.Builder(context, fallbackId)
                            .forNativeAd { fallbackAd ->
                                loadedNativeAd?.destroy()
                                loadedNativeAd = fallbackAd
                                AdDiagnosticsTracker.recordLoaded("Native Advanced", fallbackId, fallbackAd.responseInfo?.toString() ?: "")
                            }
                            .withAdListener(object : AdListener() {
                                override fun onAdFailedToLoad(fbErr: LoadAdError) {
                                    AdDiagnosticsTracker.recordFailed(
                                        format = "Native Advanced",
                                        unitId = fallbackId,
                                        code = fbErr.code,
                                        domain = fbErr.domain,
                                        message = fbErr.message
                                    )
                                }
                            })
                            .build()
                        fallbackLoader.loadAd(AdRequest.Builder().build())
                    }
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            loadedNativeAd?.destroy()
            loadedNativeAd = null
        }
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = loadedNativeAd != null,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically()
    ) {
        val nativeAd = loadedNativeAd ?: return@AnimatedVisibility

        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 4.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    createNativeAdView(ctx, nativeAd)
                },
                update = { view ->
                    populateNativeAdView(nativeAd, view)
                }
            )
        }
    }
}

private fun createNativeAdView(context: Context, nativeAd: NativeAd): NativeAdView {
    val nativeAdView = NativeAdView(context)

    // Main Card Container with Midnight Navy background and subtle border
    val cardLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val bgDrawable = GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#111B2E"))
            cornerRadius = dpToPx(context, 14f)
            setStroke(dpToPx(context, 1f).toInt(), AndroidColor.parseColor("#1E2F4D"))
        }
        background = bgDrawable
        val pad = dpToPx(context, 12f).toInt()
        setPadding(pad, pad, pad, pad)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Top Row: Ad Attribution Badge + Headline + Icon
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        val iconSize = dpToPx(context, 40f).toInt()
        layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
            marginEnd = dpToPx(context, 10f).toInt()
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
    }
    topRow.addView(iconView)
    nativeAdView.iconView = iconView

    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val badgeAndAdvertiserRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val adBadge = TextView(context).apply {
        text = "AD"
        textSize = 10f
        setTextColor(AndroidColor.parseColor("#090E1A"))
        val badgeBg = GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#F59E0B"))
            cornerRadius = dpToPx(context, 4f)
        }
        background = badgeBg
        val hPad = dpToPx(context, 5f).toInt()
        val vPad = dpToPx(context, 2f).toInt()
        setPadding(hPad, vPad, hPad, vPad)
    }
    badgeAndAdvertiserRow.addView(adBadge)

    val advertiserView = TextView(context).apply {
        id = View.generateViewId()
        textSize = 11f
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        setPadding(dpToPx(context, 6f).toInt(), 0, 0, 0)
    }
    badgeAndAdvertiserRow.addView(advertiserView)
    nativeAdView.advertiserView = advertiserView

    textCol.addView(badgeAndAdvertiserRow)

    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        textSize = 14f
        setTextColor(AndroidColor.parseColor("#FFFFFF"))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dpToPx(context, 2f).toInt(), 0, 0)
    }
    textCol.addView(headlineView)
    nativeAdView.headlineView = headlineView

    topRow.addView(textCol)
    cardLayout.addView(topRow)

    // Body Text
    val bodyView = TextView(context).apply {
        id = View.generateViewId()
        textSize = 12f
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        maxLines = 2
        setPadding(0, dpToPx(context, 6f).toInt(), 0, dpToPx(context, 8f).toInt())
    }
    cardLayout.addView(bodyView)
    nativeAdView.bodyView = bodyView

    // Call to Action Button (Tournament Gold pill matching Ludo theme)
    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        textSize = 13f
        setTextColor(AndroidColor.parseColor("#090E1A"))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        val btnBg = GradientDrawable().apply {
            setColor(AndroidColor.parseColor("#F59E0B"))
            cornerRadius = dpToPx(context, 8f)
        }
        background = btnBg
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(context, 38f).toInt()
        )
    }
    cardLayout.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    nativeAdView.addView(cardLayout)
    populateNativeAdView(nativeAd, nativeAdView)

    return nativeAdView
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    (adView.headlineView as? TextView)?.text = nativeAd.headline

    val bodyView = adView.bodyView as? TextView
    if (nativeAd.body.isNullOrEmpty()) {
        bodyView?.visibility = View.GONE
    } else {
        bodyView?.visibility = View.VISIBLE
        bodyView?.text = nativeAd.body
    }

    val iconView = adView.iconView as? ImageView
    if (nativeAd.icon?.drawable != null) {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageDrawable(nativeAd.icon?.drawable)
    } else {
        iconView?.visibility = View.GONE
    }

    val advertiserView = adView.advertiserView as? TextView
    if (nativeAd.advertiser.isNullOrEmpty()) {
        advertiserView?.visibility = View.GONE
    } else {
        advertiserView?.visibility = View.VISIBLE
        advertiserView?.text = nativeAd.advertiser
    }

    val ctaView = adView.callToActionView as? Button
    if (nativeAd.callToAction.isNullOrEmpty()) {
        ctaView?.visibility = View.GONE
    } else {
        ctaView?.visibility = View.VISIBLE
        ctaView?.text = nativeAd.callToAction
    }

    adView.setNativeAd(nativeAd)
}

private fun dpToPx(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}
