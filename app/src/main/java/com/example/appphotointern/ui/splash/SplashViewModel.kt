package com.example.appphotointern.ui.splash

import androidx.lifecycle.ViewModel
import com.example.appphotointern.repository.impl.SplashRepository

class SplashViewModel(private val repository: SplashRepository) : ViewModel() {
    fun hasScreenWelcome(): Boolean{
       return repository.hasScreenWelcome()
    }
}
