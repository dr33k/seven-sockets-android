package com.seven.mobile.sockets.ui.auth

import com.seven.mobile.sockets.data.model.UserDto

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserDto, val message: String? = null) : AuthState()
    data class PasswordResetSent(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}