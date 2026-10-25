package com.seven.mobile.sockets.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.seven.mobile.sockets.MainActivity
import com.seven.mobile.sockets.R
import com.seven.mobile.sockets.data.local.TokenManager
import com.seven.mobile.sockets.data.remote.RetrofitClient
import com.seven.mobile.sockets.data.repository.AuthRepositoryImpl
import com.seven.mobile.sockets.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var tokenManager: TokenManager

    private val viewModel: AuthViewModel by viewModels {
        val apiService = RetrofitClient.createAuthApiService(applicationContext)
        val repo = AuthRepositoryImpl(apiService, TokenManager(applicationContext))
        AuthViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(applicationContext)

        if (tokenManager.hasValidToken()) {
            navigateToMainScreen(animate = false)
            return
        }

        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.authMainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onAuthSuccess() {
        navigateToMainScreen(animate = true)
    }

    private fun navigateToMainScreen(animate: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        if (animate) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        finish()
    }
}