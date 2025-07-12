package com.example.appphotointern.ui.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.tasks.await
import androidx.core.content.edit
import com.example.appphotointern.R
import com.example.appphotointern.utils.ARG_EMAIL
import com.example.appphotointern.utils.PREFS_NAME
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository(private val context: Context) : IAuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    override fun launchGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    override fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    override fun extractGoogleCredential(
        data: Intent?,
        onSuccess: (AuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            onSuccess(credential)
        } catch (e: Exception) {
            onError(e.message ?: context.getString(R.string.toast_google_login_failed))
        }
    }

    override suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    override fun saveEmail() {
        sharedPreferences.edit {
            putString(ARG_EMAIL, auth.currentUser?.email)
        }
    }
}
