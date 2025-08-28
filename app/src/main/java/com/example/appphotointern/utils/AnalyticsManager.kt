package com.example.appphotointern.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    data object LogEvent{
        const val EVENT_FILTER_SELECTED = "filter_selected"
        const val EVENT_FRAME_SELECTED = "frame_selected"
        const val EVENT_STICKER_SELECTED = "sticker_selected"

        const val PARAM_FILTER_NAME = "filter_name"
        const val PARAM_FRAME_NAME = "frame_name"
        const val PARAM_STICKER_NAME = "sticker_name"
    }

    private var firebaseAnalytics: FirebaseAnalytics? = null
    private val pendingAnalytics = mutableListOf<Pair<String, Bundle>>()

    private fun getAnalytics(): FirebaseAnalytics {
        return firebaseAnalytics ?: throw IllegalStateException("Call init() first!")
    }

    fun init(firebaseAnalytics: FirebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics
    }

    fun logEvent(name: String, params: Map<String, Any>? = null) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        pendingAnalytics.add(Pair(name, bundle))
    }

    fun flushEvents() {
        if (firebaseAnalytics == null) {
            throw IllegalStateException("Call init() first!")
        }
        pendingAnalytics.forEach { (name, bundle) ->
            getAnalytics().logEvent(name, bundle)
        }
        pendingAnalytics.clear()
    }

    fun clearAllEvents() {
        pendingAnalytics.clear()
    }
}