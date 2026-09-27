package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.ErrorMessages
import kotlinx.coroutines.tasks.await

class SignupRepository {

    private val db = FirestoreManager.db
    private fun signupsCollection() = db.collection(Constants.COLLECTION_SIGNUPS)
    private fun activitiesCollection() = db.collection(Constants.COLLECTION_ACTIVITIES)

    suspend fun getMySignups(userId: String): List<ActivitySignup> {
        return signupsCollection()
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(ActivitySignup::class.java)
            .sortedBy { it.dateMillis }
    }

    suspend fun getMySignup(activityId: String, userId: String): ActivitySignup? {
        return signupsCollection()
            .whereEqualTo("activityId", activityId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(ActivitySignup::class.java)
            .firstOrNull()
    }

    // Used by the admin side to see who is coming to an activity.
    suspend fun getSignupsForActivity(activityId: String): List<ActivitySignup> {
        return signupsCollection()
            .whereEqualTo("activityId", activityId)
            .get()
            .await()
            .toObjects(ActivitySignup::class.java)
            .sortedBy { it.volunteerName }
    }

    suspend fun join(activity: Activity, user: User): Result<Unit> {
        return try {
            if (getMySignup(activity.id, user.uid) != null) return Result.success(Unit)
            if (activity.spotsRemaining <= 0) {
                return Result.failure(IllegalStateException(ErrorMessages.SPOTS_FULL))
            }
            val signup = ActivitySignup(
                activityId = activity.id,
                userId = user.uid,
                volunteerName = user.fullName,
                activityTitle = activity.title,
                programme = activity.programme,
                dateMillis = activity.dateMillis,
                signedUpDate = DateUtils.now()
            )
            signupsCollection().add(signup).await()
            adjustFilledSpots(activity.id, +1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leave(signup: ActivitySignup): Result<Unit> {
        return try {
            signupsCollection().document(signup.id).delete().await()
            adjustFilledSpots(signup.activityId, -1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Runs in a transaction so two volunteers joining at once cannot both claim the last spot.
    private suspend fun adjustFilledSpots(activityId: String, delta: Int) {
        val docRef = activitiesCollection().document(activityId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val current = snapshot.getLong("filledSpots") ?: 0L
            transaction.update(docRef, "filledSpots", (current + delta).coerceAtLeast(0))
        }.await()
    }
}
