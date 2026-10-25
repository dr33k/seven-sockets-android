package com.seven.mobile.sockets.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun isValidEmail_validEmails_returnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"))
        assertTrue(ValidationUtils.isValidEmail("john.doe+test@domain.co.uk"))
        assertTrue(ValidationUtils.isValidEmail("dev_123@sub.domain.org"))
    }

    @Test
    fun isValidEmail_invalidEmails_returnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("invalidemail"))
        assertFalse(ValidationUtils.isValidEmail("user@"))
        assertFalse(ValidationUtils.isValidEmail("@domain.com"))
        assertFalse(ValidationUtils.isValidEmail("user@domain"))
    }

    @Test
    fun isValidUsername_validUsernames_returnsTrue() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"))
        assertTrue(ValidationUtils.isValidUsername("dev123"))
        assertTrue(ValidationUtils.isValidUsername("abc"))
    }

    @Test
    fun isValidUsername_invalidUsernames_returnsFalse() {
        assertFalse(ValidationUtils.isValidUsername("ab"))
        assertFalse(ValidationUtils.isValidUsername("a".repeat(31)))
        assertFalse(ValidationUtils.isValidUsername("user-name"))
        assertFalse(ValidationUtils.isValidUsername("user name"))
    }

    @Test
    fun isValidPassword_strongPassword_returnsTrue() {
        assertTrue(ValidationUtils.isValidPassword("P@ssword123"))
        assertTrue(ValidationUtils.isValidPassword("Secure#2025"))
    }

    @Test
    fun isValidPassword_weakPassword_returnsFalse() {
        assertFalse(ValidationUtils.isValidPassword("password"))
        assertFalse(ValidationUtils.isValidPassword("Pass1"))
        assertFalse(ValidationUtils.isValidPassword("PASSWORD123"))
    }

    @Test
    fun calculatePasswordStrength_correctEvaluations() {
        assertEquals(ValidationUtils.PasswordStrength.WEAK, ValidationUtils.calculatePasswordStrength("123"))
        assertEquals(ValidationUtils.PasswordStrength.MEDIUM, ValidationUtils.calculatePasswordStrength("Pass123"))
        assertEquals(ValidationUtils.PasswordStrength.STRONG, ValidationUtils.calculatePasswordStrength("P@ssword123!"))
    }
}