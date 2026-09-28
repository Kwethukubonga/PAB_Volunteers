package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.utils.AppError
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.RecentSignInRequired
import kotlinx.coroutines.tasks.await

/** Removing an account means clearing the person's own data, then the sign-in itself. */
class AccountRepository {

    private val db = FirestoreManager.db
    private val signupRepository = SignupRepository()

    suspend fun deleteCurrentAccount(): Result<Unit> {
        val user = FirebaseAuthManager.currentUser
            ?: return Result.failure(AppError(R.string.error_not_signed_in))

        // Firebase only deletes a sign-in made in the last few minutes. Checking first means
        // nothing is removed unless the final step is going to be allowed.
        val lastSignIn = user.metadata?.lastSignInTimestamp ?: 0L
        if (DateUtils.now() - lastSignIn > RECENT_SIGN_IN_MILLIS) {
            return Result.failure(RecentSignInRequired(R.string.delete_account_recent_login))
        }

        return try {
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

    private companion object {
        const val RECENT_SIGN_IN_MILLIS = 5L * 60 * 1000
    }
}
