package com.kantu.pab_volunteers.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Turns anything thrown by Firebase into one short sentence the user can act on.
 * Every screen sends its failures through here so the wording stays the same everywhere.
 */
object ErrorMessages {

    const val SPOTS_FULL = "This activity is full."
    const val NOT_SIGNED_IN = "You are not signed in."
    const val OFFLINE = "No connection. Check your internet and try again."
    const val GENERIC = "Something went wrong. Please try again."

    fun textFor(error: Throwable): String = when (error) {
        is FirebaseNetworkException -> OFFLINE

        is FirebaseTooManyRequestsException -> "Too many tries. Wait a moment and try again."

        is FirebaseAuthWeakPasswordException ->
            error.reason ?: "Choose a stronger password."

        is FirebaseAuthUserCollisionException ->
            "That email already has an account. Sign in instead."

        is FirebaseAuthInvalidUserException ->
            "No account was found for that email."

        is FirebaseAuthInvalidCredentialsException ->
            "That email or password is not correct."

        is FirebaseAuthRecentLoginRequiredException ->
            "Sign in again before you do that."

        is FirebaseFirestoreException -> forFirestore(error)

        else -> error.message?.takeIf { it.isNotBlank() } ?: GENERIC
    }

    private fun forFirestore(error: FirebaseFirestoreException): String = when (error.code) {
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> OFFLINE

        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "You do not have permission to do that."

        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
            "Your session has ended. Sign in again."

        FirebaseFirestoreException.Code.NOT_FOUND ->
            "That record no longer exists."

        FirebaseFirestoreException.Code.ALREADY_EXISTS ->
            "That record already exists."

        else -> error.message?.takeIf { it.isNotBlank() } ?: GENERIC
    }
}
