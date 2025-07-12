package com.example.appphotointern.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.appphotointern.MainActivity
import com.example.appphotointern.R
import com.example.appphotointern.auth.AuthViewModel
import com.example.appphotointern.databinding.ActivitySignUpBinding
import com.example.appphotointern.extention.toast

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    private val viewModel: AuthViewModel by lazy { AuthViewModel(AuthRepository(this)) }

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.handleSignInResult(
                data = result.data,
                onError = { error ->
                    toast(R.string.toast_google_login_failed)
                }
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        initUI()
        observeUser()
    }

    private fun initUI() {
        binding.apply {
            btnGoogle.setOnClickListener {
                viewModel.launchGoogleSignIn(googleLauncher)
            }
        }
    }

    private fun observeUser() {
        viewModel.loading.observe(this) {
            binding.progressCircular.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.user.observe(this) { user ->
            if (user != null) {
                toast(R.string.toast_google_login_success)
                viewModel.saveEmail()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                toast(R.string.toast_google_login_failed)
            }
        }
    }
}
