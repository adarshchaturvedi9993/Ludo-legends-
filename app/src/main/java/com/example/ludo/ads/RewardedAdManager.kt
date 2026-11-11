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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manages loading and showing of Rewarded Ads.
 * Strictly guarantees that rewards are ONLY granted when the verified
 * OnUserEarnedRewardListener callback is invoked by Google Mobile Ads SDK.
 */
class RewardedAdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var retryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    val isLoaded: Boolean
        get() = rewardedAd != null

    fun loadAd(activityContext: Context? = null, forceReload: Boolean = false, onLoaded: (() -> Unit)? = null) {
        val ctx = activityContext ?: LudoAds.currentActivity ?: context

        if (forceReload) {
            rewardedAd = null
            isLoading = false
            retryCount = 0
        }

        if (isLoading || rewardedAd != null) {
            if (rewardedAd != null) onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getRewardedAdUnitId()
        AdDiagnosticsTracker.recordLoading("Rewarded", adUnitId)
        val request = AdRequest.Builder().build()

        Log.d(TAG, "[AdMob Request] Rewarded -> AdUnitId: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        RewardedAd.load(
            ctx,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    retryCount = 0
                    val responseSummary = ad.responseInfo.toString()
                    Log.d(TAG, "[AdMob Loaded] Rewarded -> Success! Response: $responseSummary")
                    AdDiagnosticsTracker.recordLoaded("Rewarded", adUnitId, responseSummary)
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w(
                        TAG,
                        "[AdMob Failed] Rewarded -> Code: ${loadAdError.code}, Domain: ${loadAdError.domain}, Message: ${loadAdError.message}"
                    )
                    AdDiagnosticsTracker.recordFailed(
                        format = "Rewarded",
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
                        Log.d(TAG, "Scheduling Rewarded retry #$retryCount in ${delayMs / 1000}s")
                        mainHandler.postDelayed({ loadAd(ctx) }, delayMs)
                    }
                }
            }
        )
    }

    /**
     * Shows the rewarded ad.
     * @param onRewardEarned Invoked ONLY upon verified Google SDK callback.
     * @param onAdClosed Invoked when ad is dismissed or fails to display.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.d(TAG, "Rewarded ad is not ready yet. Preloading and continuing.")
            loadAd(activity)
            onAdClosed(false)
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                Log.d(TAG, "[AdMob Event] Rewarded ad dismissed. Reward earned: $rewardGranted")
                AdDiagnosticsTracker.recordDismissed("Rewarded")
                loadAd(activity)
                onAdClosed(rewardGranted)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                Log.w(TAG, "[AdMob Error] Rewarded ad failed to show: ${adError.message}")
                AdDiagnosticsTracker.recordFailed(
                    format = "Rewarded",
                    unitId = RemoteConfigManager.getRewardedAdUnitId(),
                    code = adError.code,
                    domain = adError.domain,
                    message = adError.message
                )
                loadAd(activity)
                onAdClosed(false)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "[AdMob Event] Rewarded ad showed.")
                AdDiagnosticsTracker.recordShowing("Rewarded")
            }
        }

        ad.show(activity) { rewardItem ->
            // CRITICAL: Reward is strictly granted ONLY through Google's verified callback
            rewardGranted = true
            Log.d(TAG, "[AdMob Reward] User earned verified reward: ${rewardItem.type} amount ${rewardItem.amount}")
            onRewardEarned(rewardItem)
        }
    }

    companion object {
        private const val TAG = "RewardedAdManager"
    }
}
