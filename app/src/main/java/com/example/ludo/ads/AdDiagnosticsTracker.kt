package com.example.ludo.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Diagnostics and monitoring state for AdMob in Ludo Legends.
 * Records SDK initialization, individual ad format status, load times, error codes, and responses.
 */
data class FormatDiagnostics(
    val formatName: String,
    val adUnitId: String = "",
    val isTestId: Boolean = true,
    val status: String = "NOT_STARTED", // NOT_STARTED, LOADING, LOADED, FAILED, SHOWING, SHOWN
    val lastLoadedTime: Long = 0,
    val lastError: String = "",
    val errorCode: Int? = null,
    val errorDomain: String = "",
    val responseInfo: String = ""
)

data class AdMobDiagnosticsState(
    val isSdkInitialized: Boolean = false,
    val sdkInitMessage: String = "Initializing...",
    val isFirebaseInitialized: Boolean = false,
    val isLiveMode: Boolean = false,
    val adsMode: String = "test",
    val appOpenStatus: FormatDiagnostics = FormatDiagnostics("App Open"),
    val bannerStatus: FormatDiagnostics = FormatDiagnostics("Banner"),
    val interstitialStatus: FormatDiagnostics = FormatDiagnostics("Interstitial"),
    val rewardedInterstitialStatus: FormatDiagnostics = FormatDiagnostics("Rewarded Interstitial"),
    val rewardedStatus: FormatDiagnostics = FormatDiagnostics("Rewarded"),
    val nativeStatus: FormatDiagnostics = FormatDiagnostics("Native Advanced")
)

object AdDiagnosticsTracker {
    private val _state = MutableStateFlow(AdMobDiagnosticsState())
    val state: StateFlow<AdMobDiagnosticsState> = _state.asStateFlow()

    fun updateSdkInit(initialized: Boolean, message: String) {
        _state.value = _state.value.copy(
            isSdkInitialized = initialized,
            sdkInitMessage = message
        )
    }

    fun updateFirebaseStatus(initialized: Boolean, isLive: Boolean, mode: String) {
        _state.value = _state.value.copy(
            isFirebaseInitialized = initialized,
            isLiveMode = isLive,
            adsMode = mode
        )
    }

    fun recordLoading(format: String, unitId: String) {
        val isTest = unitId.contains("3940256099942544")
        updateFormat(format) {
            it.copy(
                formatName = format,
                adUnitId = unitId,
                isTestId = isTest,
                status = "LOADING",
                lastError = ""
            )
        }
    }

    fun recordLoaded(format: String, unitId: String, responseInfoSummary: String = "") {
        val isTest = unitId.contains("3940256099942544")
        updateFormat(format) {
            it.copy(
                formatName = format,
                adUnitId = unitId,
                isTestId = isTest,
                status = "LOADED",
                lastLoadedTime = System.currentTimeMillis(),
                responseInfo = responseInfoSummary,
                lastError = ""
            )
        }
    }

    fun recordFailed(format: String, unitId: String, code: Int, domain: String, message: String, responseInfoSummary: String = "") {
        val isTest = unitId.contains("3940256099942544")
        updateFormat(format) {
            it.copy(
                formatName = format,
                adUnitId = unitId,
                isTestId = isTest,
                status = "FAILED",
                errorCode = code,
                errorDomain = domain,
                lastError = message,
                responseInfo = responseInfoSummary
            )
        }
    }

    fun recordShowing(format: String) {
        updateFormat(format) {
            it.copy(status = "SHOWING")
        }
    }

    fun recordDismissed(format: String) {
        updateFormat(format) {
            it.copy(status = "SHOWN/DISMISSED")
        }
    }

    private inline fun updateFormat(format: String, crossinline transform: (FormatDiagnostics) -> FormatDiagnostics) {
        val cur = _state.value
        _state.value = when (format) {
            "App Open" -> cur.copy(appOpenStatus = transform(cur.appOpenStatus))
            "Banner" -> cur.copy(bannerStatus = transform(cur.bannerStatus))
            "Interstitial" -> cur.copy(interstitialStatus = transform(cur.interstitialStatus))
            "Rewarded Interstitial" -> cur.copy(rewardedInterstitialStatus = transform(cur.rewardedInterstitialStatus))
            "Rewarded" -> cur.copy(rewardedStatus = transform(cur.rewardedStatus))
            "Native Advanced" -> cur.copy(nativeStatus = transform(cur.nativeStatus))
            else -> cur
        }
    }
}
