package com.example.appphotointern.ui.splash

import androidx.lifecycle.ViewModel

class SplashViewModel(private val repository: SplashRepository) : ViewModel() {
    fun hasScreenWelcome(): Boolean{
       return  repository.hasScreenWelcome()
    }

    fun getSaveEdEmail() : String?{
        return repository.getSavaEdEmail()
    }
}
