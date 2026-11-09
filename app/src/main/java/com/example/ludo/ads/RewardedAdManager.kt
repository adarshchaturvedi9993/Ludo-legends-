package com.example.ludo.ads

import android.app.Activity
import android.content.Context
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

    val isLoaded: Boolean
        get() = rewardedAd != null

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getRewardedAdUnitId()
        val request = AdRequest.Builder().build()

        Log.d(TAG, "Loading Rewarded Ad with unit ID: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        RewardedAd.load(
            context,
            adUnitId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded Ad loaded successfully.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
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
            Log.d(TAG, "Rewarded ad is not ready yet.")
            loadAd()
            onAdClosed(false)
            return
        }

        var rewardGranted = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                Log.d(TAG, "Rewarded ad dismissed. Reward earned: $rewardGranted")
                loadAd()
                onAdClosed(rewardGranted)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                loadAd()
                onAdClosed(false)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed.")
            }
        }

        ad.show(activity) { rewardItem ->
            rewardGranted = true
            Log.d(TAG, "User earned verified reward: ${rewardItem.type} amount ${rewardItem.amount}")
            onRewardEarned(rewardItem)
        }
    }

    companion object {
        private const val TAG = "RewardedAdManager"
    }
}
