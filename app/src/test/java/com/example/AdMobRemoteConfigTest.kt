package com.example

import com.example.ludo.ads.AdConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdMobRemoteConfigTest {

    @Test
    fun verifyProductionAdUnitIds() {
        assertEquals("ca-app-pub-3093045675277628/9087363896", AdConstants.PROD_APP_OPEN)
        assertEquals("ca-app-pub-3093045675277628/3283151685", AdConstants.PROD_BANNER)
        assertEquals("ca-app-pub-3093045675277628/6322433902", AdConstants.PROD_INTERSTITIAL)
        assertEquals("ca-app-pub-3093045675277628/3638374905", AdConstants.PROD_REWARDED_INTERSTITIAL)
        assertEquals("ca-app-pub-3093045675277628/1666817686", AdConstants.PROD_REWARDED)
        assertEquals("ca-app-pub-3093045675277628/1485040199", AdConstants.PROD_NATIVE)
    }

    @Test
    fun verifyOfficialGoogleTestAdUnitIds() {
        // Official Google sample ad units always use publisher 3940256099942544
        assertTrue(AdConstants.TEST_APP_OPEN.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(AdConstants.TEST_BANNER.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(AdConstants.TEST_INTERSTITIAL.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(AdConstants.TEST_REWARDED_INTERSTITIAL.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(AdConstants.TEST_REWARDED.startsWith("ca-app-pub-3940256099942544/"))
        assertTrue(AdConstants.TEST_NATIVE.startsWith("ca-app-pub-3940256099942544/"))
    }

    @Test
    fun ensureTestIdsNeverEqualProductionIds() {
        assertNotEquals(AdConstants.TEST_APP_OPEN, AdConstants.PROD_APP_OPEN)
        assertNotEquals(AdConstants.TEST_BANNER, AdConstants.PROD_BANNER)
        assertNotEquals(AdConstants.TEST_INTERSTITIAL, AdConstants.PROD_INTERSTITIAL)
        assertNotEquals(AdConstants.TEST_REWARDED_INTERSTITIAL, AdConstants.PROD_REWARDED_INTERSTITIAL)
        assertNotEquals(AdConstants.TEST_REWARDED, AdConstants.PROD_REWARDED)
        assertNotEquals(AdConstants.TEST_NATIVE, AdConstants.PROD_NATIVE)
    }

    @Test
    fun verifyRemoteConfigKeys() {
        assertEquals("ads_enabled", AdConstants.KEY_ADS_ENABLED)
        assertEquals("ads_mode", AdConstants.KEY_ADS_MODE)
        assertEquals("ad_unit_app_open", AdConstants.KEY_AD_UNIT_APP_OPEN)
        assertEquals("ad_unit_banner", AdConstants.KEY_AD_UNIT_BANNER)
        assertEquals("ad_unit_interstitial", AdConstants.KEY_AD_UNIT_INTERSTITIAL)
        assertEquals("ad_unit_rewarded_interstitial", AdConstants.KEY_AD_UNIT_REWARDED_INTERSTITIAL)
        assertEquals("ad_unit_rewarded", AdConstants.KEY_AD_UNIT_REWARDED)
        assertEquals("ad_unit_native", AdConstants.KEY_AD_UNIT_NATIVE)
        assertEquals("banner_home_enabled", AdConstants.KEY_BANNER_HOME)
        assertEquals("banner_game_enabled", AdConstants.KEY_BANNER_GAME)
        assertEquals("banner_lobby_enabled", AdConstants.KEY_BANNER_LOBBY)
        assertEquals("banner_victory_enabled", AdConstants.KEY_BANNER_VICTORY)
        assertEquals("banner_settings_enabled", AdConstants.KEY_BANNER_SETTINGS)
        assertEquals("native_home_enabled", AdConstants.KEY_NATIVE_HOME)
        assertEquals("interstitial_match_end_enabled", AdConstants.KEY_INTERSTITIAL_MATCH_END)
        assertEquals("interstitial_exit_enabled", AdConstants.KEY_INTERSTITIAL_EXIT)
        assertEquals("rewarded_reroll_enabled", AdConstants.KEY_REWARDED_REROLL)
        assertEquals("rewarded_victory_double_enabled", AdConstants.KEY_REWARDED_VICTORY_DOUBLE)
        assertEquals("app_open_enabled", AdConstants.KEY_APP_OPEN_ENABLED)
        assertEquals("test", AdConstants.MODE_TEST)
        assertEquals("live", AdConstants.MODE_LIVE)
    }
}
