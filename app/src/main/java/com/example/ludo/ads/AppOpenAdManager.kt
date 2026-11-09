package com.example.ludo.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Manages App Open Ad lifecycle and loading.
 * Ensures ads are only shown when app is foregrounded and not in active gameplay.
 */
class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
        private set

    private var loadTime: Long = 0
    private var lastAdDismissedTime: Long = 0

    private var currentActivity: Activity? = null

    /**
     * If user is currently playing a Ludo match on the board,
     * set this to true so we never disrupt gameplay.
     */
    var isGameActive: Boolean = false

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        if (!RemoteConfigManager.isAppOpenEnabled()) {
            Log.d(TAG, "App Open Ads are disabled via Remote Config; skipping load.")
            return
        }

        isLoadingAd = true
        val adUnitId = RemoteConfigManager.getAppOpenAdUnitId()
        val request = AdRequest.Builder().build()

        Log.d(TAG, "Loading App Open Ad with unit ID: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        AppOpenAd.load(
            application,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(TAG, "App Open Ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        // App Open Ads are valid for up to 4 hours
        val isNotExpired = Date().time - loadTime < (4 * 3600 * 1000)
        return appOpenAd != null && isNotExpired
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        if (isShowingAd) {
            Log.d(TAG, "An ad is already showing.")
            onAdDismissed?.invoke()
            return
        }

        if (!RemoteConfigManager.isAppOpenEnabled()) {
            Log.d(TAG, "App Open Ads are disabled via Remote Config.")
            onAdDismissed?.invoke()
            return
        }

        if (isGameActive) {
            Log.d(TAG, "User is playing a match; skipping App Open Ad to preserve gameplay.")
            onAdDismissed?.invoke()
            return
        }

        // 60-second cooldown between app open ads to prevent spam
        val now = Date().time
        if (now - lastAdDismissedTime < 60_000) {
            Log.d(TAG, "App Open Ad skipped due to cooldown.")
            onAdDismissed?.invoke()
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "App Open Ad is not ready yet.")
            loadAd()
            onAdDismissed?.invoke()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastAdDismissedTime = Date().time
                Log.d(TAG, "App Open Ad dismissed.")
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                Log.d(TAG, "App Open Ad displayed.")
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    // ProcessLifecycleObserver callbacks
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let { activity ->
            showAdIfAvailable(activity)
        }
    }

    // ActivityLifecycleCallbacks
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    companion object {
        private const val TAG = "AppOpenAdManager"
    }
}
