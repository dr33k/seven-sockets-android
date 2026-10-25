package com.seven.mobile.sockets

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.seven.mobile.sockets.data.local.TokenManager
import com.seven.mobile.sockets.databinding.ActivityMainBinding
import com.seven.mobile.sockets.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displayUserInfo()
        setupListeners()
    }

    private fun displayUserInfo() {
        val username = tokenManager.getUsername() ?: "User"
        val email = tokenManager.getEmail() ?: "Not provided"

        binding.tvUserUsername.text = "Username: $username"
        binding.tvUserEmail.text = "Email: $email"
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            tokenManager.clear()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }
}