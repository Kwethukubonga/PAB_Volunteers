package com.kantu.pab_volunteers.data.repository

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.ErrorMessages
import kotlinx.coroutines.tasks.await

/** Removing an account means clearing the person's own data, then the sign-in itself. */
class AccountRepository {

    private val db = FirestoreManager.db

    suspend fun deleteCurrentAccount(): Result<Unit> {
        val user = FirebaseAuthManager.currentUser
            ?: return Result.failure(IllegalStateException(ErrorMessages.NOT_SIGNED_IN))

        return try {
            db.collection(Constants.COLLECTION_SIGNUPS)
                .whereEqualTo("userId", user.uid)
                .get()
                .await()
                .documents
                .forEach { it.reference.delete().await() }

            db.collection(Constants.COLLECTION_USERS).document(user.uid).delete().await()

            // Last, because once this succeeds there is no permission to touch anything else.
            user.delete().await()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
