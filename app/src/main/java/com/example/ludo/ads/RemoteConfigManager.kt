package com.example.ludo.ads

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdConfigState(
    val isAdsEnabled: Boolean = true,
    val isLiveMode: Boolean = false,
    val adsMode: String = AdConstants.MODE_TEST
)

object RemoteConfigManager {
    private const val TAG = "RemoteConfigManager"

    private val _configState = MutableStateFlow(AdConfigState())
    val configState: StateFlow<AdConfigState> = _configState.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context, onReady: (() -> Unit)? = null) {
        if (isInitialized) {
            onReady?.invoke()
            return
        }

        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()

            val fetchInterval = if (BuildConfig.DEBUG) 0L else 3600L
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchInterval)
                .setFetchTimeoutInSeconds(15L)
                .build()

            remoteConfig.setConfigSettingsAsync(configSettings)

            // Safe in-memory defaults
            val defaultValues = mapOf<String, Any>(
                AdConstants.KEY_ADS_ENABLED to true,
                AdConstants.KEY_ADS_MODE to AdConstants.MODE_TEST,
                AdConstants.KEY_AD_UNIT_APP_OPEN to AdConstants.PROD_APP_OPEN,
                AdConstants.KEY_AD_UNIT_BANNER to AdConstants.PROD_BANNER,
                AdConstants.KEY_AD_UNIT_INTERSTITIAL to AdConstants.PROD_INTERSTITIAL,
                AdConstants.KEY_AD_UNIT_REWARDED_INTERSTITIAL to AdConstants.PROD_REWARDED_INTERSTITIAL,
                AdConstants.KEY_AD_UNIT_REWARDED to AdConstants.PROD_REWARDED,
                AdConstants.KEY_AD_UNIT_NATIVE to AdConstants.PROD_NATIVE,
                AdConstants.KEY_BANNER_HOME to true,
                AdConstants.KEY_BANNER_GAME to true,
                AdConstants.KEY_BANNER_LOBBY to true,
                AdConstants.KEY_BANNER_VICTORY to true,
                AdConstants.KEY_BANNER_SETTINGS to true,
                AdConstants.KEY_NATIVE_HOME to true,
                AdConstants.KEY_INTERSTITIAL_MATCH_END to true,
                AdConstants.KEY_INTERSTITIAL_EXIT to true,
                AdConstants.KEY_REWARDED_REROLL to true,
                AdConstants.KEY_REWARDED_VICTORY_DOUBLE to true,
                AdConstants.KEY_APP_OPEN_ENABLED to true
            )

            try {
                remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            } catch (e: Exception) {
                remoteConfig.setDefaultsAsync(defaultValues)
            }

            updateCurrentState(remoteConfig)

            // Fetch and activate latest configuration from Firebase
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Remote config fetched & activated. Live=${isLiveMode()}")
                    } else {
                        Log.w(TAG, "Remote config fetch failed; using cached/defaults", task.exception)
                    }
                    updateCurrentState(remoteConfig)
                    onReady?.invoke()
                }

            // Real-time listener for dynamic Remote Config changes from Firebase Console
            remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys)
                    remoteConfig.activate().addOnCompleteListener {
                        updateCurrentState(remoteConfig)
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w(TAG, "Config update listener error: ${error.code}", error)
                }
            })

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing RemoteConfigManager", e)
            onReady?.invoke()
        }
    }

    private fun updateCurrentState(remoteConfig: FirebaseRemoteConfig) {
        val enabled = remoteConfig.getBoolean(AdConstants.KEY_ADS_ENABLED)
        val mode = remoteConfig.getString(AdConstants.KEY_ADS_MODE).trim().lowercase()
        val isLive = enabled && (mode == AdConstants.MODE_LIVE)

        _configState.value = AdConfigState(
            isAdsEnabled = enabled,
            isLiveMode = isLive,
            adsMode = mode
        )
    }

    /**
     * Master switch for advertisements.
     */
    fun isAdsEnabled(): Boolean {
        return try {
            FirebaseRemoteConfig.getInstance().getBoolean(AdConstants.KEY_ADS_ENABLED)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Returns true ONLY if ads are enabled and ads_mode is set to "live".
     */
    fun isLiveMode(): Boolean {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val enabled = remoteConfig.getBoolean(AdConstants.KEY_ADS_ENABLED)
            val mode = remoteConfig.getString(AdConstants.KEY_ADS_MODE).trim().lowercase()
            enabled && (mode == AdConstants.MODE_LIVE)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * App Open Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getAppOpenAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_APP_OPEN
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_APP_OPEN).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_APP_OPEN
        } catch (_: Exception) {
            AdConstants.PROD_APP_OPEN
        }
    }

    /**
     * Banner Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getBannerAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_BANNER
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_BANNER).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_BANNER
        } catch (_: Exception) {
            AdConstants.PROD_BANNER
        }
    }

    /**
     * Interstitial Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getInterstitialAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_INTERSTITIAL
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_INTERSTITIAL).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_INTERSTITIAL
        } catch (_: Exception) {
            AdConstants.PROD_INTERSTITIAL
        }
    }

    /**
     * Rewarded Interstitial Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getRewardedInterstitialAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_REWARDED_INTERSTITIAL
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_REWARDED_INTERSTITIAL).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_REWARDED_INTERSTITIAL
        } catch (_: Exception) {
            AdConstants.PROD_REWARDED_INTERSTITIAL
        }
    }

    /**
     * Rewarded Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getRewardedAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_REWARDED
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_REWARDED).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_REWARDED
        } catch (_: Exception) {
            AdConstants.PROD_REWARDED
        }
    }

    /**
     * Native Advanced Ad Unit ID.
     * In test mode, strictly returns Google's official test ID.
     * In live mode, returns the production ID from Remote Config or fallback.
     */
    fun getNativeAdUnitId(): String {
        if (!isLiveMode()) return AdConstants.TEST_NATIVE
        return try {
            val id = FirebaseRemoteConfig.getInstance().getString(AdConstants.KEY_AD_UNIT_NATIVE).trim()
            if (id.isNotEmpty()) id else AdConstants.PROD_NATIVE
        } catch (_: Exception) {
            AdConstants.PROD_NATIVE
        }
    }

    /**
     * Checks if a specific ad placement is enabled.
     */
    fun isPlacementEnabled(key: String): Boolean {
        if (!isAdsEnabled()) return false
        return try {
            FirebaseRemoteConfig.getInstance().getBoolean(key)
        } catch (_: Exception) {
            true
        }
    }

    fun isBannerHomeEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_BANNER_HOME)
    fun isBannerGameEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_BANNER_GAME)
    fun isBannerLobbyEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_BANNER_LOBBY)
    fun isBannerVictoryEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_BANNER_VICTORY)
    fun isBannerSettingsEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_BANNER_SETTINGS)
    fun isNativeHomeEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_NATIVE_HOME)
    fun isInterstitialMatchEndEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_INTERSTITIAL_MATCH_END)
    fun isInterstitialExitEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_INTERSTITIAL_EXIT)
    fun isRewardedRerollEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_REWARDED_REROLL)
    fun isRewardedVictoryDoubleEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_REWARDED_VICTORY_DOUBLE)
    fun isAppOpenEnabled(): Boolean = isPlacementEnabled(AdConstants.KEY_APP_OPEN_ENABLED)
}
