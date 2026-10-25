package com.seven.mobile.sockets.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

/**
 * Secure token manager utilizing EncryptedSharedPreferences for safe storage of JWT tokens.
 */
class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String, tokenType: String = "Bearer") {
        sharedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_TOKEN_TYPE, tokenType)
                .putLong(KEY_SAVED_AT, System.currentTimeMillis())
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun getTokenType(): String = sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"

    fun saveUser(userId: String, username: String, email: String, avatarUrl: String? = null) {
        sharedPreferences.edit {
            putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_AVATAR_URL, avatarUrl)
        }
    }

    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)
    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null)

    fun clear() {
        sharedPreferences.edit { clear() }
    }

    fun hasValidToken(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrEmpty()
    }

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "secure_auth_prefs"
        private const val FALLBACK_PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_SAVED_AT = "saved_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_AVATAR_URL = "avatar_url"
    }
}