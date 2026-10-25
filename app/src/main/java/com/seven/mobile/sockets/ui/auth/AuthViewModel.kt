package com.seven.mobile.sockets.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seven.mobile.sockets.data.model.LoginRequest
import com.seven.mobile.sockets.data.model.RegisterRequest
import com.seven.mobile.sockets.data.repository.AuthRepository
import com.seven.mobile.sockets.util.NetworkResult
import com.seven.mobile.sockets.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (password.isBlank()) {
            _authState.value = AuthState.Error("Password is required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.login(LoginRequest(trimmedEmail, password))) {
                is NetworkResult.Success -> {
                    _authState.value = AuthState.Success(user = result.data.user)
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()

        if (!ValidationUtils.isValidUsername(trimmedUsername)) {
            _authState.value = AuthState.Error("Username must be 3-30 characters (letters, numbers, underscores)")
            return
        }

        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            _authState.value = AuthState.Error("Password must be at least 8 characters with upper, lower, digit & special char")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.register(RegisterRequest(trimmedUsername, trimmedEmail, password))) {
                is NetworkResult.Success -> {
                    _authState.value = AuthState.Success(user = result.data.user, message = "Account created successfully!")
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        val trimmedEmail = email.trim()
        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.forgotPassword(trimmedEmail)) {
                is NetworkResult.Success -> {
                    _authState.value = AuthState.PasswordResetSent(result.data.message)
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun handleGoogleOAuth(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.googleOAuth(idToken)) {
                is NetworkResult.Success -> {
                    _authState.value = AuthState.Success(user = result.data.user)
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun handleAppleOAuth(idToken: String, authCode: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = repository.appleOAuth(idToken, authCode)) {
                is NetworkResult.Success -> {
                    _authState.value = AuthState.Success(user = result.data.user)
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}