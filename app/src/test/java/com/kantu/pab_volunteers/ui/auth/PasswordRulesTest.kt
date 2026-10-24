package com.kantu.pab_volunteers.ui.auth

import com.kantu.pab_volunteers.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The rules for a new account's password (PV-FR01). */
class PasswordRulesTest {

    @Test
    fun `a long password with a symbol that matches its confirmation is accepted`() {
        assertNull(PasswordRules.problemWithNewPassword("Lavender#Hill", "Lavender#Hill"))
    }

    @Test
    fun `a password shorter than eight characters is refused`() {
        assertEquals(R.string.error_password_short, PasswordRules.problemWithNewPassword("Ab#1", "Ab#1"))
    }

    @Test
    fun `eight characters is long enough`() {
        assertNull(PasswordRules.problemWithNewPassword("abcdefg!", "abcdefg!"))
    }

    @Test
    fun `a password of only letters and digits is refused`() {
        assertEquals(
            R.string.error_password_symbol,
            PasswordRules.problemWithNewPassword("Lavender2026", "Lavender2026")
        )
    }

    @Test
    fun `a confirmation that does not match is refused`() {
        assertEquals(
            R.string.error_passwords_mismatch,
            PasswordRules.problemWithNewPassword("Lavender#Hill", "Lavender#Hil")
        )
    }

    @Test
    fun `strength is checked before the confirmation`() {
        assertEquals(R.string.error_password_short, PasswordRules.problemWithNewPassword("Ab#1", "different"))
    }
}
