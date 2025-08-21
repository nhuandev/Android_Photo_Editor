package com.example.appphotointern.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.appphotointern.utils.LanguageManager

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LanguageManager.getLanguage(newBase)
        val context = LanguageManager.applyLanguage(newBase, lang)
        super.attachBaseContext(context)
    }
}