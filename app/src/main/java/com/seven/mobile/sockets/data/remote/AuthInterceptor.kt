package com.seven.mobile.sockets.data.remote

import com.seven.mobile.sockets.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        tokenManager.getAccessToken()?.let { token ->
            if (token.isNotBlank()) {
                val tokenType = tokenManager.getTokenType()
                requestBuilder.addHeader("Authorization", "$tokenType $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}