package com.example.appphotointern.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import com.example.appphotointern.R
import com.example.appphotointern.ui.analytics.AnalyticsActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val NATIVE_AD_ID = "ca-app-pub-3940256099942544/2247696110"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var nativeAd: NativeAd? = null
    private var isLoading = false
    var isCheckReward = false // Check ad rewarded

    private val adRequest: AdRequest by lazy {
        AdRequest.Builder().build()
    }

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
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.starRatingView = adView.findViewById(R.id.rating_bar)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.bodyView = adView.findViewById(R.id.ad_body)

        (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating!!.toFloat()
        (adView.callToActionView as? Button)?.text = nativeAd.callToAction
        (adView.headlineView as? TextView)?.text = nativeAd.headline
        adView.mediaView?.setMediaContent(nativeAd.mediaContent)
        (adView.bodyView as? TextView)?.text = nativeAd.body
        adView.setNativeAd(nativeAd)
    }

    fun loadAdReward(context: Context) {
        if (rewardedAd == null) {
            isLoading = true
            RewardedAd.load(
                context,
                REWARDED_AD_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        rewardedAd = null
                        isLoading = false
                    }
                }
            )
        }
    }

    fun showReward(activity: Activity) {
        var isRewarded = false
        rewardedAd?.let {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    if (isRewarded) {
                        val intent = Intent(activity, AnalyticsActivity::class.java)
                        activity.startActivity(intent)
                    }
                    loadAdReward(activity)
                }
            }

            rewardedAd?.show(
                activity,
                OnUserEarnedRewardListener { rewardItem ->
                    isCheckReward = true
                    isRewarded = true
                },
            )
        }
    }

    fun destroy() {
        nativeAd?.destroy()
        nativeAd = null
        interstitialAd = null
    }
}
