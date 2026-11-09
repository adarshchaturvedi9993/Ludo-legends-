package com.example.ludo.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
 * Ensures ads are only shown when app is foregrounded and not in active gameplay,
 * with retry backoff and detailed diagnostics.
 */
class AppOpenAdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
        private set

    private var loadTime: Long = 0
    private var lastAdDismissedTime: Long = 0
    private var retryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

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

    fun isAdLoaded(): Boolean = isAdAvailable()

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoadingAd || isAdAvailable()) {
            if (isAdAvailable()) onLoaded?.invoke()
            return
        }

        if (!RemoteConfigManager.isAppOpenEnabled()) {
            Log.d(TAG, "App Open Ads are disabled via Remote Config; skipping load.")
            return
        }

        isLoadingAd = true
        val adUnitId = RemoteConfigManager.getAppOpenAdUnitId()
        AdDiagnosticsTracker.recordLoading("App Open", adUnitId)
        val request = AdRequest.Builder().build()

        Log.d(TAG, "[AdMob Request] App Open -> AdUnitId: $adUnitId (LiveMode=${RemoteConfigManager.isLiveMode()})")

        AppOpenAd.load(
            application,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    retryCount = 0
                    loadTime = Date().time
                    val responseSummary = ad.responseInfo.toString()
                    Log.d(TAG, "[AdMob Loaded] App Open -> Success! Response: $responseSummary")
                    AdDiagnosticsTracker.recordLoaded("App Open", adUnitId, responseSummary)
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null
                    Log.w(
                        TAG,
                        "[AdMob Failed] App Open -> Code: ${loadAdError.code}, Domain: ${loadAdError.domain}, Message: ${loadAdError.message}"
                    )
                    AdDiagnosticsTracker.recordFailed(
                        format = "App Open",
                        unitId = adUnitId,
                        code = loadAdError.code,
                        domain = loadAdError.domain,
                        message = loadAdError.message,
                        responseInfoSummary = loadAdError.responseInfo?.toString() ?: ""
                    )

                    // Exponential retry backoff: 5s, 10s, 20s (max 3 retries)
                    if (retryCount < 3) {
                        val delayMs = (5000L * (1 shl retryCount)).coerceAtMost(30000L)
                        retryCount++
                        Log.d(TAG, "Scheduling App Open retry #$retryCount in ${delayMs / 1000}s")
                        mainHandler.postDelayed({ loadAd() }, delayMs)
                    }
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        // App Open Ads are valid for up to 4 hours
        val isNotExpired = Date().time - loadTime < (4 * 3600 * 1000)
        return appOpenAd != null && isNotExpired
    }

    fun showAdIfAvailable(
        activity: Activity,
        forceShow: Boolean = false,
        onAdDismissed: (() -> Unit)? = null
    ) {
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

        if (isGameActive && !forceShow) {
            Log.d(TAG, "User is playing a match; skipping App Open Ad to preserve gameplay.")
            onAdDismissed?.invoke()
            return
        }

        val cooldownSeconds = RemoteConfigManager.getAppOpenCooldownSeconds()
        if (!forceShow && !LudoAds.canShowFullScreenAd(cooldownSeconds)) {
            Log.d(TAG, "App Open Ad skipped due to global full-screen cooldown (${cooldownSeconds}s).")
            onAdDismissed?.invoke()
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "App Open Ad is not ready yet. Triggering reload.")
            loadAd()
            onAdDismissed?.invoke()
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            onAdDismissed?.invoke()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastAdDismissedTime = Date().time
                LudoAds.recordFullScreenAdDismissed(false)
                AdDiagnosticsTracker.recordDismissed("App Open")
                Log.d(TAG, "[AdMob Event] App Open Ad dismissed.")
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                LudoAds.recordFullScreenAdDismissed(false)
                AdDiagnosticsTracker.recordFailed(
                    format = "App Open",
                    unitId = RemoteConfigManager.getAppOpenAdUnitId(),
                    code = adError.code,
                    domain = adError.domain,
                    message = adError.message
                )
                Log.w(TAG, "[AdMob Error] App Open Ad failed to show: ${adError.message}")
                loadAd()
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                AdDiagnosticsTracker.recordShowing("App Open")
                Log.d(TAG, "[AdMob Event] App Open Ad displayed.")
            }
        }

        isShowingAd = true
        ad.show(activity)
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
