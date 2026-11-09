package com.example.ludo.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

/**
 * Manages loading and showing of Interstitial Ads with natural placement and cooldowns.
 */
class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var lastShownTime: Long = 0

    fun loadAd() {
        if (isLoading || interstitialAd != null) {
            return
        }

        if (!RemoteConfigManager.isAdsEnabled()) {
            return
        }

        isLoading = true
        val adUnitId = RemoteConfigManager.getInterstitialAdUnitId()
        val request = AdRequest.Builder().build()

        Log.d(TAG, "Loading Interstitial Ad with unit ID: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Interstitial Ad loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
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
        // 45 seconds cooldown between interstitials unless forceShow
        if (!forceShow && timeSinceLast < 45_000) {
            Log.d(TAG, "Skipping interstitial due to cooldown (${timeSinceLast / 1000}s < 45s)")
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready; proceeding directly.")
            loadAd()
            onAdClosed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownTime = Date().time
                Log.d(TAG, "Interstitial ad dismissed.")
                loadAd()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                loadAd()
                onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed full screen content.")
            }
        }

        ad.show(activity)
    }

    companion object {
        private const val TAG = "InterstitialAdManager"
    }
}
