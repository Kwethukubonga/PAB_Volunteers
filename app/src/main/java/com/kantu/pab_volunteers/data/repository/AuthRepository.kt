package com.kantu.pab_volunteers.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.utils.AppError
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuthManager.auth

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return Result.failure(AppError(R.string.error_generic))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAccountWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return Result.failure(AppError(R.string.error_generic))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(AppError(R.string.error_generic))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks the password of whoever is signed in, without signing them in again. Fingerprint
     * sign-in saves a password only after this says it is the right one.
     */
    suspend fun confirmPassword(email: String, password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(AppError(R.string.error_not_signed_in))
        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(email.trim(), password)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // The email is written in the language picked in Settings.
    suspend fun sendPasswordReset(email: String, languageCode: String): Result<Unit> {
        return try {
            auth.setLanguageCode(languageCode)
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
