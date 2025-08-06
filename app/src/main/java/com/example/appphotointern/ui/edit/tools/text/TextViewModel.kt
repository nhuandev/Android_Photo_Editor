package com.example.appphotointern.ui.edit.tools.text

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class TextViewModel(application: Application) : AndroidViewModel(application) {
    private val _fonts = MutableLiveData<List<String>>()
    val fonts: LiveData<List<String>> get() = _fonts

    init {
        loadFontsFromAssets()
    }

    private fun loadFontsFromAssets() {
        val assetManager = getApplication<Application>().assets
        try {
            val fontList = assetManager.list("fonts")?.toList() ?: emptyList()
            _fonts.value = fontList
        } catch (e: Exception) {
            e.printStackTrace()
            _fonts.value = emptyList()
        }
    }
}
