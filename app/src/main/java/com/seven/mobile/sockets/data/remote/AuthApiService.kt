package com.seven.mobile.sockets.data.remote

import com.seven.mobile.sockets.data.model.AuthResponse
import com.seven.mobile.sockets.data.model.ForgotPasswordRequest
import com.seven.mobile.sockets.data.model.GenericResponse
import com.seven.mobile.sockets.data.model.LoginRequest
import com.seven.mobile.sockets.data.model.OAuthRequest
import com.seven.mobile.sockets.data.model.RefreshTokenRequest
import com.seven.mobile.sockets.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<GenericResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/v1/auth/oauth/google")
    suspend fun googleOAuth(@Body request: OAuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/oauth/apple")
    suspend fun appleOAuth(@Body request: OAuthRequest): Response<AuthResponse>
}