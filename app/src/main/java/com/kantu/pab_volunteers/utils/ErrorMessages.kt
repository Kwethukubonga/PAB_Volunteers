package com.kantu.pab_volunteers.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kantu.pab_volunteers.R

/**
 * Turns anything thrown by Firebase into one short sentence the user can act on.
 * Every screen sends its failures through here so the wording stays the same everywhere.
 */
object ErrorMessages {

    val OFFLINE: UiText = UiText.Res(R.string.error_no_connection)
    val GENERIC: UiText = UiText.Res(R.string.error_generic)

    fun textFor(error: Throwable): UiText {
        // A transaction hands back whatever was thrown inside it, sometimes wrapped.
        val appError = generateSequence(error) { it.cause }.firstOrNull { it is AppError }
        if (appError is AppError) return UiText.Res(appError.messageRes, appError.args)

        return when (error) {
            is FirebaseNetworkException -> OFFLINE

            is FirebaseTooManyRequestsException -> UiText.Res(R.string.error_too_many_tries)

            // Must stay above the credentials case, because it is a kind of that exception.
            is FirebaseAuthWeakPasswordException -> UiText.Res(R.string.error_weak_password)

            is FirebaseAuthUserCollisionException -> UiText.Res(R.string.error_email_in_use)

            is FirebaseAuthInvalidUserException -> UiText.Res(R.string.error_account_not_found)

            is FirebaseAuthInvalidCredentialsException ->
                if (error.errorCode == "ERROR_INVALID_EMAIL") {
                    UiText.Res(R.string.error_invalid_email)
                } else {
                    UiText.Res(R.string.error_login_failed)
                }

            is FirebaseAuthRecentLoginRequiredException -> UiText.Res(R.string.error_sign_in_again)

            is FirebaseFirestoreException -> forFirestore(error)

            else -> GENERIC
        }
    }

    private fun forFirestore(error: FirebaseFirestoreException): UiText = when (error.code) {
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> OFFLINE

        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            UiText.Res(R.string.error_permission_denied)

        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            UiText.Res(R.string.error_session_ended)

        FirebaseFirestoreException.Code.NOT_FOUND -> UiText.Res(R.string.error_record_missing)

        FirebaseFirestoreException.Code.ALREADY_EXISTS -> UiText.Res(R.string.error_record_exists)

        else -> GENERIC
    }
}
