package com.example.appphotointern.auth

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appphotointern.ui.auth.IAuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        repository.signOut {
            repository.launchGoogleSignIn(launcher)
        }
    }

    fun handleSignInResult(
        data: Intent?,
        onError: (String) -> Unit
    ) {
        repository.extractGoogleCredential(
            data,
            onSuccess = { credential ->
                signInWithGoogleCredential(credential)
            },
            onError = onError
        )
    }

    private fun signInWithGoogleCredential(credential: AuthCredential) {
        _loading.value = true
        viewModelScope.launch {
            val user = repository.signInWithCredential(credential)
            _user.value = user
            _loading.value = false
        }
    }

    fun signOut(onComplete: () -> Unit) {
        repository.signOut(onComplete)
    }

    fun saveEmail() {
        repository.saveEmail()
    }
}
