package com.seven.mobile.sockets.data.remote

import com.seven.mobile.sockets.data.local.TokenManager
import com.seven.mobile.sockets.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiServiceSupplier: () -> AuthApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("auth/refresh")) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        synchronized(this) {
            val currentAccessToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            if (currentAccessToken != null && currentAccessToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "${tokenManager.getTokenType()} $currentAccessToken")
                    .build()
            }

            return try {
                val refreshResult = runBlocking {
                    authApiServiceSupplier().refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (refreshResult.isSuccessful && refreshResult.body() != null) {
                    val authResponse = refreshResult.body()!!
                    tokenManager.saveTokens(
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken,
                        tokenType = authResponse.tokenType ?: "Bearer"
                    )
                    authResponse.user.let {
                        tokenManager.saveUser(it.id, it.username, it.email, it.avatarUrl)
                    }

                    response.request.newBuilder()
                        .header("Authorization", "${authResponse.tokenType ?: "Bearer"} ${authResponse.accessToken}")
                        .build()
                } else {
                    tokenManager.clear()
                    null
                }
            } catch (e: Exception) {
                tokenManager.clear()
                null
            }
        }
    }
}