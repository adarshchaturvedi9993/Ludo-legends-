package com.example.ludo.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem

/**
 * Central coordinator for Google AdMob in Ludo Legends.
 * Integrates all 6 ad formats with Firebase Remote Config controls.
 */
object LudoAds {
    private const val TAG = "LudoAds"

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    lateinit var interstitialAdManager: InterstitialAdManager
        private set

    lateinit var rewardedAdManager: RewardedAdManager
        private set

    lateinit var rewardedInterstitialAdManager: RewardedInterstitialAdManager
        private set

    private var isInitialized = false

    fun initialize(application: Application, onReady: (() -> Unit)? = null) {
        if (isInitialized) {
            onReady?.invoke()
            return
        }

        // Initialize Firebase Remote Config first
        RemoteConfigManager.initialize(application) {
            // Initialize MobileAds SDK
            try {
                MobileAds.initialize(application) { initStatus ->
                    Log.d(TAG, "MobileAds initialized: ${initStatus.adapterStatusMap}")
                    preloadAds(application)
                    onReady?.invoke()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MobileAds", e)
                onReady?.invoke()
            }
        }

        appOpenAdManager = AppOpenAdManager(application)
        interstitialAdManager = InterstitialAdManager(application)
        rewardedAdManager = RewardedAdManager(application)
        rewardedInterstitialAdManager = RewardedInterstitialAdManager(application)

        isInitialized = true
    }

    private fun preloadAds(context: Context) {
        if (!RemoteConfigManager.isAdsEnabled()) {
            Log.d(TAG, "Ads are disabled via Remote Config; skipping preloads.")
            return
        }

        appOpenAdManager.loadAd()
        interstitialAdManager.loadAd()
        rewardedAdManager.loadAd()
        rewardedInterstitialAdManager.loadAd()
    }

    /**
     * Call when a Ludo match starts or is active on Screen.GAME.
     * Guarantees App Open Ads will NEVER pop up while user is actively playing.
     */
    fun setGameActive(active: Boolean) {
        if (::appOpenAdManager.isInitialized) {
            appOpenAdManager.isGameActive = active
        }
    }

    fun showInterstitial(
        activity: Activity,
        forceShow: Boolean = false,
        onAdClosed: () -> Unit
    ) {
        if (!::interstitialAdManager.isInitialized) {
            onAdClosed()
            return
        }
        interstitialAdManager.showInterstitial(activity, forceShow, onAdClosed)
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        if (!::rewardedAdManager.isInitialized) {
            onAdClosed(false)
            return
        }
        rewardedAdManager.showRewardedAd(activity, onRewardEarned, onAdClosed)
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        if (!::rewardedInterstitialAdManager.isInitialized) {
            onAdClosed(false)
            return
        }
        rewardedInterstitialAdManager.showAd(activity, onRewardEarned, onAdClosed)
    }
}
