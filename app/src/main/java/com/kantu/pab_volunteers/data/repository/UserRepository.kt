package com.kantu.pab_volunteers.data.repository

import com.google.firebase.firestore.FieldValue
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirestoreManager.db
    private fun usersCollection() = db.collection(Constants.COLLECTION_USERS)

    suspend fun getUser(uid: String): User? {
        return usersCollection().document(uid).get().await().toObject(User::class.java)
    }

    // Called the first time someone signs in, before they fill in their profile.
    suspend fun createUserIfMissing(uid: String, email: String): Result<Unit> {
        return try {
            val existing = getUser(uid)
            if (existing == null) {
                val user = User(
                    uid = uid,
                    email = email,
                    volunteerId = DateUtils.generateVolunteerId(),
                    profileComplete = false,
                    joinedDate = DateUtils.now()
                )
                usersCollection().document(uid).set(user).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(user: User): Result<Unit> {
        return try {
            usersCollection().document(user.uid).set(user.copy(profileComplete = true)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Favourites live on the user's own record, so no extra read is needed to show them. */
    suspend fun setFavourite(uid: String, activityId: String, favourite: Boolean): Result<Unit> {
        return try {
            val change = if (favourite) {
                FieldValue.arrayUnion(activityId)
            } else {
                FieldValue.arrayRemove(activityId)
            }
            usersCollection().document(uid).update("favouriteActivityIds", change).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isProfileComplete(uid: String): Boolean {
        return getUser(uid)?.profileComplete == true
    }

    // Used by the admin volunteers page.
    suspend fun getAllVolunteers(): List<User> {
        return usersCollection()
            .whereEqualTo("role", User.ROLE_VOLUNTEER)
            .get()
            .await()
            .toObjects(User::class.java)
            .sortedBy { it.firstName }
    }
}
