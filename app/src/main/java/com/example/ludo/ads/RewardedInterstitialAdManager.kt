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
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * Manages loading and presentation of Rewarded Interstitial Ads.
 * Ensures rewards are granted ONLY after verified Google callback,
 * with retry backoff and detailed diagnostics.
 */
class RewardedInterstitialAdManager(private val context: Context) {

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoading = false
    private var retryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    val isLoaded: Boolean
        get() = rewardedInterstitialAd != null

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedInterstitialAd != null) {
            if (rewardedInterstitialAd != null) onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getRewardedInterstitialAdUnitId()
        AdDiagnosticsTracker.recordLoading("Rewarded Interstitial", adUnitId)
        val request = AdRequest.Builder().build()

        Log.d(TAG, "[AdMob Request] Rewarded Interstitial -> AdUnitId: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        RewardedInterstitialAd.load(
            context,
            adUnitId,
            request,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isLoading = false
                    retryCount = 0
                    val responseSummary = ad.responseInfo.toString()
                    Log.d(TAG, "[AdMob Loaded] Rewarded Interstitial -> Success! Response: $responseSummary")
                    AdDiagnosticsTracker.recordLoaded("Rewarded Interstitial", adUnitId, responseSummary)
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    isLoading = false
                    Log.w(
                        TAG,
                        "[AdMob Failed] Rewarded Interstitial -> Code: ${loadAdError.code}, Domain: ${loadAdError.domain}, Message: ${loadAdError.message}"
                    )
                    AdDiagnosticsTracker.recordFailed(
                        format = "Rewarded Interstitial",
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
                        Log.d(TAG, "Scheduling Rewarded Interstitial retry #$retryCount in ${delayMs / 1000}s")
                        mainHandler.postDelayed({ loadAd() }, delayMs)
                    }
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        val ad = rewardedInterstitialAd
        if (ad == null) {
            Log.d(TAG, "Rewarded Interstitial is not loaded; triggering load and proceeding.")
            loadAd()
            onAdClosed(false)
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedInterstitialAd = null
                Log.d(TAG, "[AdMob Event] Rewarded Interstitial dismissed. Reward granted: $rewardGranted")
                AdDiagnosticsTracker.recordDismissed("Rewarded Interstitial")
                loadAd()
                onAdClosed(rewardGranted)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedInterstitialAd = null
                Log.w(TAG, "[AdMob Error] Rewarded Interstitial failed to show: ${adError.message}")
                AdDiagnosticsTracker.recordFailed(
                    format = "Rewarded Interstitial",
                    unitId = RemoteConfigManager.getRewardedInterstitialAdUnitId(),
                    code = adError.code,
                    domain = adError.domain,
                    message = adError.message
                )
                loadAd()
                onAdClosed(false)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "[AdMob Event] Rewarded Interstitial showed.")
                AdDiagnosticsTracker.recordShowing("Rewarded Interstitial")
            }
        }

        ad.show(activity) { rewardItem ->
            // Verified reward granted through Google's callback
            rewardGranted = true
            Log.d(TAG, "[AdMob Reward] Verified reward earned from Rewarded Interstitial: ${rewardItem.type} ${rewardItem.amount}")
            onRewardEarned(rewardItem)
        }
    }

    companion object {
        private const val TAG = "RewardedInterstitialMgr"
    }
}
