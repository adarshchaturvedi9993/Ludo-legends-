package com.example.ludo.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Central coordinator for Google AdMob in Ludo Legends.
 * Integrates all 6 ad formats with Firebase Remote Config controls,
 * strict single-initialization guarantee, and detailed diagnostics tracking.
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

    private val isInitializing = AtomicBoolean(false)
    private val isInitialized = AtomicBoolean(false)

    fun initialize(application: Application, onReady: (() -> Unit)? = null) {
        if (isInitialized.get()) {
            onReady?.invoke()
            return
        }

        if (!isInitializing.compareAndSet(false, true)) {
            Log.d(TAG, "LudoAds is already initializing; waiting for completion.")
            return
        }

        Log.d(TAG, "Starting centralized LudoAds initialization...")

        // Instantiate managers with application context
        appOpenAdManager = AppOpenAdManager(application)
        interstitialAdManager = InterstitialAdManager(application)
        rewardedAdManager = RewardedAdManager(application)
        rewardedInterstitialAdManager = RewardedInterstitialAdManager(application)

        // 1. Initialize Remote Config (safe in-memory test fallback if Firebase unavailable)
        RemoteConfigManager.initialize(application) {
            // 2. Initialize MobileAds SDK once
            try {
                // Ensure test device configuration
                val requestConfiguration = RequestConfiguration.Builder()
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)

                MobileAds.initialize(application) { initStatus ->
                    val adapterMap = initStatus.adapterStatusMap
                    Log.d(TAG, "MobileAds initialized successfully! Adapters: $adapterMap")
                    AdDiagnosticsTracker.updateSdkInit(
                        initialized = true,
                        message = "Initialized (${adapterMap.size} adapters)"
                    )

                    isInitialized.set(true)
                    isInitializing.set(false)

                    // 3. Preload full-screen ads only AFTER SDK is initialized
                    preloadAds(application)
                    onReady?.invoke()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error initializing MobileAds SDK", e)
                AdDiagnosticsTracker.updateSdkInit(
                    initialized = false,
                    message = "Error: ${e.message}"
                )
                isInitializing.set(false)
                onReady?.invoke()
            }
        }
    }

    private fun preloadAds(context: Context) {
        if (!RemoteConfigManager.isAdsEnabled()) {
            Log.d(TAG, "Ads are disabled via Remote Config; skipping preloads.")
            return
        }

        Log.d(TAG, "Preloading all 4 full-screen test ad formats...")
        appOpenAdManager.loadAd()
        interstitialAdManager.loadAd()
        rewardedAdManager.loadAd()
        rewardedInterstitialAdManager.loadAd()
    }

    // Unified Full-Screen Ad Cooldown Tracking
    private var lastFullScreenAdDismissedTime: Long = 0
    private var lastRewardEarnedTime: Long = 0

    fun canShowFullScreenAd(customCooldownSeconds: Long? = null): Boolean {
        if (!RemoteConfigManager.isAdsEnabled()) return false

        val now = System.currentTimeMillis()
        val cooldownSecs = customCooldownSeconds ?: RemoteConfigManager.getFullscreenCooldownSeconds()
        val cooldownMs = cooldownSecs * 1000L

        val timeSinceLast = now - lastFullScreenAdDismissedTime
        if (timeSinceLast < cooldownMs) {
            Log.d(TAG, "Suppressed full-screen ad due to global cooldown (${timeSinceLast / 1000}s < ${cooldownSecs}s)")
            return false
        }

        val timeSinceReward = now - lastRewardEarnedTime
        if (timeSinceReward < 45_000L) {
            Log.d(TAG, "Suppressed full-screen ad due to recent rewarded ad grace period (${timeSinceReward / 1000}s < 45s)")
            return false
        }

        return true
    }

    fun recordFullScreenAdDismissed(wasRewarded: Boolean = false) {
        val now = System.currentTimeMillis()
        lastFullScreenAdDismissedTime = now
        if (wasRewarded) {
            lastRewardEarnedTime = now
        }
        Log.d(TAG, "Recorded full-screen ad dismissal. wasRewarded=$wasRewarded")
    }

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
        if (!::interstitialAdManager.isInitialized || !RemoteConfigManager.isAdsEnabled()) {
            onAdClosed()
            return
        }

        if (!forceShow && !canShowFullScreenAd()) {
            onAdClosed()
            return
        }

        interstitialAdManager.showInterstitial(activity, forceShow) {
            recordFullScreenAdDismissed(false)
            onAdClosed()
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        if (!::rewardedAdManager.isInitialized || !RemoteConfigManager.isAdsEnabled()) {
            onAdClosed(false)
            return
        }

        rewardedAdManager.showRewardedAd(
            activity = activity,
            onRewardEarned = onRewardEarned,
            onAdClosed = { earned ->
                recordFullScreenAdDismissed(earned)
                onAdClosed(earned)
            }
        )
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onAdClosed: (earnedReward: Boolean) -> Unit
    ) {
        if (!::rewardedInterstitialAdManager.isInitialized || !RemoteConfigManager.isAdsEnabled()) {
            onAdClosed(false)
            return
        }

        rewardedInterstitialAdManager.showAd(
            activity = activity,
            onRewardEarned = onRewardEarned,
            onAdClosed = { earned ->
                recordFullScreenAdDismissed(earned)
                onAdClosed(earned)
            }
        )
    }
}
