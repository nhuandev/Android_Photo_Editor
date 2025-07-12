package com.example.appphotointern.ui.auth

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface IAuthRepository {
    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Intent>)

    fun signOut(onComplete: () -> Unit)

    fun extractGoogleCredential(
        data: Intent?,
        onSuccess: (AuthCredential) -> Unit,
        onError: (String) -> Unit
    )

    fun saveEmail ()

    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser?
}
