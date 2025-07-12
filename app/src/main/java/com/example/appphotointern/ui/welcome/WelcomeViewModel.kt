package com.example.appphotointern.ui.welcome

import androidx.lifecycle.ViewModel

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {

    fun markWelcome() {
        welcomeRepository.markWelcome()
    }
}