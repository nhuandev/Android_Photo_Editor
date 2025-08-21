package com.example.appphotointern.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    private fun getAnalytics(): FirebaseAnalytics {
        return firebaseAnalytics ?: throw IllegalStateException("Call init() first!")
    }

    fun init(firebaseAnalytics: FirebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics
    }

    fun logEvent(name: String, params: Map<String, String>? = null) {
        val bundle = Bundle()
        params?.forEach {
            (key, value) -> bundle.putString(key, value)
        }
        getAnalytics().logEvent(name, bundle)
    }
}