package com.example.appphotointern

import android.app.Application
import android.content.Context
import com.example.appphotointern.firebase.AnalyticsManager
import com.example.appphotointern.ui.language.LanguageManager
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AnalyticsManager.init(firebaseAnalytics)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }

    override fun attachBaseContext(base: Context) {
        val lang = LanguageManager.getLanguage(base)
        val context = LanguageManager.applyLanguage(base, lang)
        super.attachBaseContext(context)
    }
}