package com.example.appphotointern

import android.app.Application
import android.content.Context
import com.example.appphotointern.utils.AnalyticsManager
import com.example.appphotointern.utils.LanguageManager
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AnalyticsManager.init(firebaseAnalytics)
    }

    override fun attachBaseContext(base: Context) {
        val lang = LanguageManager.getLanguage(base)
        val context = LanguageManager.applyLanguage(base, lang)
        super.attachBaseContext(context)
    }
}