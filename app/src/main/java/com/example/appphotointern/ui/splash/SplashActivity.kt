package com.example.appphotointern.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.content.Intent
import android.os.Handler
import com.example.appphotointern.ui.main.MainActivity
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivitySplashBinding
import com.example.appphotointern.repository.impl.SplashRepository
import com.example.appphotointern.ui.language.LanguageActivity
import com.example.appphotointern.ui.welcome.WelcomeActivity
import com.example.appphotointern.utils.BaseActivity
import com.example.appphotointern.utils.SPLASH_DELAY

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private val viewModel: SplashViewModel by lazy { SplashViewModel(SplashRepository(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            when {
                !viewModel.hasScreenLanguage() -> {
                    val intent = Intent(this, LanguageActivity::class.java)
                    startActivity(intent)
                }

                !viewModel.hasScreenWelcome() -> {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                }

                else -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            finish()
        }, SPLASH_DELAY)
    }
}
