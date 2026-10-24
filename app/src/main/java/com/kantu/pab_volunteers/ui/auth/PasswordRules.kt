package com.kantu.pab_volunteers.ui.auth

import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R

/**
 * The rules for a new password. Only new passwords are judged this hard: signing in must not
 * re-check strength, or anyone who joined before this rule could never get back in. These match
 * the policy Firebase enforces, so the person is told here instead of being refused afterwards.
 */
object PasswordRules {

    const val MIN_LENGTH = 8

    /** The message to show for a new password and its confirmation, or null if both are fine. */
    @StringRes
    fun problemWithNewPassword(password: String, confirmPassword: String): Int? = when {
        password.length < MIN_LENGTH -> R.string.error_password_short
        password.none { !it.isLetterOrDigit() } -> R.string.error_password_symbol
        password != confirmPassword -> R.string.error_passwords_mismatch
        else -> null
    }
}
