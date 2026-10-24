package com.kantu.pab_volunteers.data.repository

import com.google.firebase.auth.AuthCredential
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.utils.AppError
import com.kantu.pab_volunteers.utils.Constants
import kotlinx.coroutines.tasks.await

/** Removing an account means clearing the person's own data, then the sign-in itself. */
class AccountRepository {

    private val db = FirestoreManager.db
    private val signupRepository = SignupRepository()

    /**
     * Firebase only deletes a sign-in that was confirmed in the last few minutes. [credential] is
     * the password or Google account the person has just given, so it is checked first and
     * nothing is removed unless the final step is going to be allowed.
     */
    suspend fun deleteCurrentAccount(credential: AuthCredential): Result<Unit> {
        val user = FirebaseAuthManager.currentUser
            ?: return Result.failure(AppError(R.string.error_not_signed_in))

        return try {
            user.reauthenticate(credential).await()

            // Leaving each activity properly gives the spot back to other volunteers.
            signupRepository.getMySignups(user.uid).forEach { signup ->
                signupRepository.leave(signup).getOrThrow()
            }

            db.collection(Constants.COLLECTION_USERS).document(user.uid).delete().await()

            // Last, because once this succeeds there is no permission to touch anything else.
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
