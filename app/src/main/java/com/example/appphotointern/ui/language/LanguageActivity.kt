package com.example.appphotointern.ui.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityLanguageBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.Language
import com.example.appphotointern.ui.welcome.WelcomeActivity
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.utils.LanguageManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlin.getValue

class LanguageActivity : BaseActivity() {
    private val binding by lazy { ActivityLanguageBinding.inflate(layoutInflater) }
    private val languageViewModel by viewModels<LanguageViewModel>()
    private lateinit var languageAdapter: LanguageAdapter
    private var selectedLanguage: Language? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBarLanguage)
        supportActionBar?.title = getString(R.string.lb_language)
        initUI()
        initEvent()
        initObserver()
    }

    private fun initUI() {
        languageAdapter = LanguageAdapter(
            emptyList(),
            onItemClick = { language ->
                selectedLanguage = language
            }
        )
        binding.recLanguage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recLanguage.adapter = languageAdapter
    }

    private fun initEvent() {
        binding.btnDoneLanguage.setOnClickListener {
            val selected = languageAdapter.getSelectedLanguage()
            selected?.let {
                Firebase.analytics.setUserProperty("language", selected.code)
                LanguageManager.setLanguage(this, selected.code)
                LanguageManager.applyLanguage(this, selected.code)

                languageViewModel.markLanguage()
                startActivity(Intent(this, WelcomeActivity::class.java))
            } ?: run {
                toast(R.string.lb_please_select_language)
            }
        }
    }

    private fun initObserver() {
        languageViewModel.languages.observe(this) { languages ->
            languageAdapter.updateLanguage(languages)
        }
        languageViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}