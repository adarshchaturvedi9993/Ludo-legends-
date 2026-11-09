package com.example.ludo.ads

import android.app.Activity
import android.content.Context
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
 * Ensures rewards are granted ONLY after verified Google callback.
 */
class RewardedInterstitialAdManager(private val context: Context) {

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoading = false

    val isLoaded: Boolean
        get() = rewardedInterstitialAd != null

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedInterstitialAd != null) {
            onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getRewardedInterstitialAdUnitId()
        val request = AdRequest.Builder().build()

        Log.d(TAG, "Loading Rewarded Interstitial with unit ID: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        RewardedInterstitialAd.load(
            context,
            adUnitId,
            request,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded Interstitial loaded.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Rewarded Interstitial failed to load: ${loadAdError.message}")
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
                Log.d(TAG, "Rewarded Interstitial dismissed. Reward granted: $rewardGranted")
                loadAd()
                onAdClosed(rewardGranted)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedInterstitialAd = null
                Log.w(TAG, "Rewarded Interstitial failed to show: ${adError.message}")
                loadAd()
                onAdClosed(false)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Interstitial showed.")
            }
        }

        ad.show(activity) { rewardItem ->
            rewardGranted = true
            Log.d(TAG, "Verified reward earned from Rewarded Interstitial: ${rewardItem.type} ${rewardItem.amount}")
            onRewardEarned(rewardItem)
        }
    }

    companion object {
        private const val TAG = "RewardedInterstitialMgr"
    }
}
