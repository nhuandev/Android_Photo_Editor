package com.azmobile.phonemirror

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.example.appphotointern.utils.AdManager
import com.example.appphotointern.utils.AnalyticsManager
import com.example.appphotointern.utils.LanguageManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainApplication : Application(), Application.ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AnalyticsManager.init(firebaseAnalytics)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        MobileAds.initialize(this)
        registerActivityLifecycleCallbacks(this)
        AdManager.loadAppOpen(this)
    }

    override fun attachBaseContext(base: Context) {
        val lang = LanguageManager.getLanguage(base)
        val context = LanguageManager.applyLanguage(base, lang)
        super.attachBaseContext(context)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}