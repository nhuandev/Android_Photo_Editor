package com.example.appphotointern.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import com.example.appphotointern.ui.main.MainActivity
import com.example.appphotointern.R
import com.example.appphotointern.repository.impl.SplashRepository
import com.example.appphotointern.ui.welcome.WelcomeActivity
import com.example.appphotointern.utils.SPLASH_DELAY

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by lazy { SplashViewModel(SplashRepository(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initUI()
    }

    private fun initUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            when {
                !viewModel.hasScreenWelcome() -> {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                }

                else -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            finish()
        }, SPLASH_DELAY)
    }
}
