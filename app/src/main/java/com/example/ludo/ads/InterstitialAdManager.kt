package com.example.ludo.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

/**
 * Manages loading and showing of Interstitial Ads with natural placement,
 * cooldowns, retry backoff, and diagnostics tracking.
 */
class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShownTime: Long = 0
    private var retryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    val isLoaded: Boolean
        get() = interstitialAd != null

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading || interstitialAd != null) {
            if (interstitialAd != null) onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getInterstitialAdUnitId()
        AdDiagnosticsTracker.recordLoading("Interstitial", adUnitId)
        val request = AdRequest.Builder().build()

        Log.d(TAG, "[AdMob Request] Interstitial -> AdUnitId: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    retryCount = 0
                    val responseSummary = ad.responseInfo.toString()
                    Log.d(TAG, "[AdMob Loaded] Interstitial -> Success! Response: $responseSummary")
                    AdDiagnosticsTracker.recordLoaded("Interstitial", adUnitId, responseSummary)
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(
                        TAG,
                        "[AdMob Failed] Interstitial -> Code: ${loadAdError.code}, Domain: ${loadAdError.domain}, Message: ${loadAdError.message}"
                    )
                    AdDiagnosticsTracker.recordFailed(
                        format = "Interstitial",
                        unitId = adUnitId,
                        code = loadAdError.code,
                        domain = loadAdError.domain,
                        message = loadAdError.message,
                        responseInfoSummary = loadAdError.responseInfo?.toString() ?: ""
                    )

                    // Retry backoff
                    if (retryCount < 3) {
                        val delayMs = (5000L * (1 shl retryCount)).coerceAtMost(30000L)
                        retryCount++
                        Log.d(TAG, "Scheduling Interstitial retry #$retryCount in ${delayMs / 1000}s")
                        mainHandler.postDelayed({ loadAd() }, delayMs)
                    }
                }
            }
        )
    }

    /**
     * Show interstitial ad at natural transition moments (e.g. match finished or exiting game).
     * Enforces a cooldown to avoid interrupting the player too frequently.
     */
    fun showInterstitial(
        activity: Activity,
        forceShow: Boolean = false,
        onAdClosed: () -> Unit
    ) {
        if (!RemoteConfigManager.isAdsEnabled()) {
            onAdClosed()
            return
        }

        val now = Date().time
        val timeSinceLast = now - lastShownTime
        val cooldownMs = RemoteConfigManager.getFullscreenCooldownSeconds() * 1000L
        if (!forceShow && timeSinceLast < cooldownMs) {
            Log.d(TAG, "Skipping interstitial due to cooldown (${timeSinceLast / 1000}s < ${cooldownMs / 1000}s)")
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready; loading in background and proceeding.")
            loadAd()
            onAdClosed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownTime = Date().time
                Log.d(TAG, "[AdMob Event] Interstitial ad dismissed.")
                AdDiagnosticsTracker.recordDismissed("Interstitial")
                loadAd()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                Log.w(TAG, "[AdMob Error] Interstitial ad failed to show: ${adError.message}")
                AdDiagnosticsTracker.recordFailed(
                    format = "Interstitial",
                    unitId = RemoteConfigManager.getInterstitialAdUnitId(),
                    code = adError.code,
                    domain = adError.domain,
                    message = adError.message
                )
                loadAd()
                onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "[AdMob Event] Interstitial ad showed full screen content.")
                AdDiagnosticsTracker.recordShowing("Interstitial")
            }
        }

        ad.show(activity)
    }

    companion object {
        private const val TAG = "InterstitialAdManager"
    }
}
