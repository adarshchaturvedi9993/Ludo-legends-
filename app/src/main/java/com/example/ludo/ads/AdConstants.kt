package com.example.ludo.ads

/**
 * Constants for Google AdMob and Firebase Remote Config parameters.
 */
object AdConstants {
    // Remote Config Parameter Keys
    const val KEY_ADS_ENABLED = "ads_enabled"
    const val KEY_ADS_MODE = "ads_mode"
    const val KEY_AD_UNIT_APP_OPEN = "ad_unit_app_open"
    const val KEY_AD_UNIT_BANNER = "ad_unit_banner"
    const val KEY_AD_UNIT_INTERSTITIAL = "ad_unit_interstitial"
    const val KEY_AD_UNIT_REWARDED_INTERSTITIAL = "ad_unit_rewarded_interstitial"
    const val KEY_AD_UNIT_REWARDED = "ad_unit_rewarded"
    const val KEY_AD_UNIT_NATIVE = "ad_unit_native"

    // Remote Placement Flags
    const val KEY_BANNER_HOME = "banner_home_enabled"
    const val KEY_BANNER_GAME = "banner_game_enabled"
    const val KEY_BANNER_LOBBY = "banner_lobby_enabled"
    const val KEY_BANNER_VICTORY = "banner_victory_enabled"
    const val KEY_BANNER_SETTINGS = "banner_settings_enabled"
    const val KEY_NATIVE_HOME = "native_home_enabled"
    const val KEY_INTERSTITIAL_MATCH_END = "interstitial_match_end_enabled"
    const val KEY_INTERSTITIAL_EXIT = "interstitial_exit_enabled"
    const val KEY_REWARDED_REROLL = "rewarded_reroll_enabled"
    const val KEY_REWARDED_VICTORY_DOUBLE = "rewarded_victory_double_enabled"
    const val KEY_APP_OPEN_ENABLED = "app_open_enabled"
    const val KEY_BANNER_STATS = "banner_stats_enabled"
    const val KEY_BANNER_HOW_TO_PLAY = "banner_how_to_play_enabled"
    const val KEY_NATIVE_STATS = "native_stats_enabled"
    const val KEY_COOLDOWN_FULLSCREEN_SECONDS = "cooldown_fullscreen_seconds"
    const val KEY_COOLDOWN_APP_OPEN_SECONDS = "cooldown_app_open_seconds"

    // Modes
    const val MODE_TEST = "test"
    const val MODE_LIVE = "live"

    // Exact Production Ad Unit IDs provided by user
    const val PROD_APP_OPEN = "ca-app-pub-3093045675277628/9087363896"
    const val PROD_BANNER = "ca-app-pub-3093045675277628/3283151685"
    const val PROD_INTERSTITIAL = "ca-app-pub-3093045675277628/6322433902"
    const val PROD_REWARDED_INTERSTITIAL = "ca-app-pub-3093045675277628/3638374905"
    const val PROD_REWARDED = "ca-app-pub-3093045675277628/1666817686"
    const val PROD_NATIVE = "ca-app-pub-3093045675277628/1485040199"

    // Google's Official Test Ad Unit IDs (used when ads_mode == "test")
    const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_INTERSTITIAL_VIDEO = "ca-app-pub-3940256099942544/8691691433"
    const val TEST_NATIVE_VIDEO = "ca-app-pub-3940256099942544/1044960115"
}
