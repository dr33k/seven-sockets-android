package com.seven.mobile.sockets.util

import java.util.regex.Pattern

object ValidationUtils {

    private val EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return username.isNotBlank() && username.length in 3..30 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    /**
     * Password validation: At least 8 characters long, contains at least one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSpecial
    }

    enum class PasswordStrength { WEAK, MEDIUM, STRONG }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 6) return PasswordStrength.WEAK
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 2 -> PasswordStrength.WEAK
            score in 3..4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}