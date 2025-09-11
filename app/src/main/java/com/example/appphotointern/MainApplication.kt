package com.azmobile.phonemirror

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.example.appphotointern.ui.purchase.BillingManager
import com.example.appphotointern.utils.AdManager
import com.example.appphotointern.utils.AnalyticsManager
import com.example.appphotointern.utils.LanguageManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

private const val APP_OPEN_AD_ID = "ca-app-pub-3940256099942544/9257395921"

class MainApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var billingManager: BillingManager
    private lateinit var currentActivity: Activity
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        MobileAds.initialize(this)
        appOpenAdManager = AppOpenAdManager()

        billingManager = BillingManager(this)
        billingManager.startBillingConnect()

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

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    fun showAdIfAvailable(activity: Activity) {
        appOpenAdManager.showAdIfAvailable(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false

        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) return
            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context, APP_OPEN_AD_ID, request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        currentActivity?.let { ad.show(it) }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                    }
                })
        }

        private fun isAdAvailable() = appOpenAd != null

        fun showAdIfAvailable(activity: Activity) {
            if (!isAdAvailable()) {
                loadAd(activity)
                return
            }
            appOpenAd?.show(activity)
        }
    }
}