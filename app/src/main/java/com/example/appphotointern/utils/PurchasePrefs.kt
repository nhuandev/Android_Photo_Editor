package com.example.appphotointern.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.appphotointern.common.KEY_IS_PREMIUM
import com.example.appphotointern.common.PREFS_NAME_BILLING
import androidx.core.content.edit

class PurchasePrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME_BILLING, Context.MODE_PRIVATE)

    var hasPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_PREMIUM, value) }
}