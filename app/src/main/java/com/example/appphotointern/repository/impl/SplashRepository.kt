package com.example.appphotointern.repository.impl

import android.content.Context
import com.example.appphotointern.repository.ISplashRepository
import com.example.appphotointern.utils.KEY_SEEN_WELCOME
import com.example.appphotointern.utils.PREFS_NAME

class SplashRepository(context: Context) : ISplashRepository {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun hasScreenWelcome(): Boolean  {
        return sharedPreferences.getBoolean(KEY_SEEN_WELCOME, false)
    }
}