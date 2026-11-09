package com.example

import android.app.Application
import com.example.ludo.ads.LudoAds
import com.google.firebase.FirebaseApp

class LudoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }

        LudoAds.initialize(this)
    }
}
