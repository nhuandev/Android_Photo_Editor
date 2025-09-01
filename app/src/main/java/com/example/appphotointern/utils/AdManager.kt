package com.example.appphotointern.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.example.appphotointern.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object AdManager {
    private const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val NATIVE_AD_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val APP_OPEN_AD_ID = "ca-app-pub-3940256099942544/9257395921"

    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var nativeAd: NativeAd? = null
    private var isShowingAd = false

    private val adRequest: AdRequest by lazy {
        AdRequest.Builder().build()
    }

    /** ---------------- Interstitial ---------------- */
    fun loadInterstitial(context: Context, onLoaded: (() -> Unit)? = null) {
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e("AdManager", "Failed to load interstitial: ${error.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismiss: (() -> Unit)? = null) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismiss?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismiss?.invoke()
                }
            }
            ad.show(activity)
        } ?: run {
            Log.d("AdManager", "Interstitial is null")
            onDismiss?.invoke()
        }
    }

    /** ---------------- Native ---------------- */
    fun loadNative(context: Context, container: FrameLayout) {
        val builder = AdLoader.Builder(context, NATIVE_AD_ID)
        builder.forNativeAd { ad: NativeAd ->
            nativeAd?.destroy()
            nativeAd = ad

            val adView = (ViewGroup.inflate(context, R.layout.ad_native, null) as NativeAdView)
            populateNativeAdView(ad, adView)

            container.removeAllViews()
            container.addView(adView)
        }
        builder.build().loadAd(adRequest)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

        (adView.headlineView as? TextView)?.text = nativeAd.headline
        adView.mediaView?.setMediaContent(nativeAd.mediaContent)
        (adView.bodyView as? TextView)?.text = nativeAd.body
        (adView.callToActionView as? Button)?.text = nativeAd.callToAction

        adView.setNativeAd(nativeAd)
    }

    /** ---------------- App Open ---------------- */
    fun loadAppOpen(context: Context) {
        AppOpenAd.load(
            context,
            APP_OPEN_AD_ID,
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d("AdManager", "AppOpenAd loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Failed to load AppOpenAd: ${error.message}")
                    appOpenAd = null
                }
            }
        )
    }

    fun showAppOpen(activity: Activity, onDismiss: (() -> Unit)? = null) {
        if (appOpenAd != null && !isShowingAd) {
            isShowingAd = true
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingAd = false
                    appOpenAd = null
                    loadAppOpen(activity) // preload next ad
                    onDismiss?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
                    appOpenAd = null
                    onDismiss?.invoke()
                }
            }
            appOpenAd?.show(activity)
        } else {
            Log.d("AdManager", "AppOpenAd is not ready")
            onDismiss?.invoke()
        }
    }

    fun destroy() {
        nativeAd?.destroy()
        nativeAd = null
        interstitialAd = null
    }
}
