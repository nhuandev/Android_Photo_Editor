package com.example.appphotointern.ui.welcome

import androidx.lifecycle.ViewModel
import com.example.appphotointern.repository.impl.WelcomeRepository

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {

    fun markWelcome() {
        welcomeRepository.markWelcome()
    }
}