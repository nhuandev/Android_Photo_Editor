package com.example.appphotointern.utils

import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.example.appphotointern.common.KEY_LANGUAGE
import java.util.Locale

object LanguageManager {
    fun setLanguage(context: Context, langCode: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit { putString(KEY_LANGUAGE, langCode) }
    }

    fun getLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        return saved ?: getSystemLanguage()
    }

    fun getSystemLanguage() : String {
        return Locale.getDefault().language
    }

    fun applyLanguage(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}