package com.example.appphotointern.ui.language

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.R
import com.example.appphotointern.models.Language
import com.example.appphotointern.repository.impl.LanguageRepository
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val languageRepository = LanguageRepository(application)

    private val _languages = MutableLiveData<List<Language>>()
    val languages: LiveData<List<Language>> get() = _languages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        loadLanguages()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            _loading.value = true
            val systemLanguage = Locale.getDefault().language
            val languageList = listOf(
                Language("vi", "Tiếng Việt", R.drawable.img_flag_vietnam),
                Language("en", "English", R.drawable.img_flag_usa),
                Language("ja", "日本語", R.drawable.img_flag_japan)
            )
            val sortLanguage = languageList.sortedByDescending { it.code == systemLanguage }
            _languages.value = sortLanguage
            _loading.value = false
        }
    }

    fun markLanguage() {
        viewModelScope.launch {
            languageRepository.markLanguage()
        }
    }
}
