package com.example.appphotointern.ui.language

import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import java.util.Locale
import androidx.core.content.edit
import com.example.appphotointern.utils.KEY_LANGUAGE

object LanguageManager {
    fun setLanguage(context: Context, langCode: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit { putString(KEY_LANGUAGE, langCode) }
    }

    fun getLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
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
