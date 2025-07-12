package com.example.appphotointern.ui.welcome

import android.content.Context
import androidx.core.content.edit
import com.example.appphotointern.utils.KEY_SEEN_WELCOME
import com.example.appphotointern.utils.PREFS_NAME

class WelcomeRepository(context: Context) : IWelcomeRepository {
    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun markWelcome() {
        sharedPreferences.edit {
            putBoolean(KEY_SEEN_WELCOME, true)
            apply()
        }
    }
}
