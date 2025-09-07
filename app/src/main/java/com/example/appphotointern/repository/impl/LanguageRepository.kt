package com.example.appphotointern.repository.impl

import android.content.Context
import androidx.core.content.edit
import com.example.appphotointern.repository.ILanguageRepository
import com.example.appphotointern.common.KEY_CHOOSE_LANGUAGE
import com.example.appphotointern.common.PREFS_NAME

class LanguageRepository(context: Context) : ILanguageRepository {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun markLanguage() {
        sharedPreferences.edit {
            putBoolean(KEY_CHOOSE_LANGUAGE, true)
            apply()
        }
    }
}