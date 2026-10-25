package com.seven.mobile.sockets.data.repository

import com.google.gson.Gson
import com.seven.mobile.sockets.data.local.TokenManager
import com.seven.mobile.sockets.data.model.AuthResponse
import com.seven.mobile.sockets.data.model.ErrorResponse
import com.seven.mobile.sockets.data.model.ForgotPasswordRequest
import com.seven.mobile.sockets.data.model.GenericResponse
import com.seven.mobile.sockets.data.model.LoginRequest
import com.seven.mobile.sockets.data.model.OAuthRequest
import com.seven.mobile.sockets.data.model.RegisterRequest
import com.seven.mobile.sockets.data.remote.AuthApiService
import com.seven.mobile.sockets.util.NetworkResult
import retrofit2.Response
import java.io.IOException

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse>
    suspend fun register(registerRequest: RegisterRequest): NetworkResult<AuthResponse>
    suspend fun forgotPassword(email: String): NetworkResult<GenericResponse>
    suspend fun googleOAuth(idToken: String): NetworkResult<AuthResponse>
    suspend fun appleOAuth(idToken: String, authCode: String?): NetworkResult<AuthResponse>
    fun isLoggedIn(): Boolean
    fun logout()
}

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.login(loginRequest) }
    }

    override suspend fun register(registerRequest: RegisterRequest): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.register(registerRequest) }
    }

    override suspend fun forgotPassword(email: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.forgotPassword(ForgotPasswordRequest(email)) }
    }

    override suspend fun googleOAuth(idToken: String): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.googleOAuth(OAuthRequest(provider = "google", idToken = idToken)) }
    }

    override suspend fun appleOAuth(idToken: String, authCode: String?): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.appleOAuth(OAuthRequest(provider = "apple", idToken = idToken, authCode = authCode)) }
    }

    override fun isLoggedIn(): Boolean {
        return tokenManager.hasValidToken()
    }

    override fun logout() {
        tokenManager.clear()
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body is AuthResponse) {
                    tokenManager.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken,
                        tokenType = body.tokenType ?: "Bearer"
                    )
                    tokenManager.saveUser(
                        userId = body.user.id,
                        username = body.user.username,
                        email = body.user.email,
                        avatarUrl = body.user.avatarUrl
                    )
                }
                NetworkResult.Success(body)
            } else {
                val errorMsg = parseErrorMessage(response)
                NetworkResult.Error(message = errorMsg, code = response.code())
            }
        } catch (e: IOException) {
            NetworkResult.Error(message = "Network connection error. Please check your connection.", cause = e)
        } catch (e: Exception) {
            NetworkResult.Error(message = e.localizedMessage ?: "An unexpected error occurred.", cause = e)
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: errorResponse.error ?: "Request failed with code ${response.code()}"
            } else {
                "Request failed with code ${response.code()}"
            }
        } catch (e: Exception) {
            "HTTP ${response.code()}: ${response.message()}"
        }
    }
}