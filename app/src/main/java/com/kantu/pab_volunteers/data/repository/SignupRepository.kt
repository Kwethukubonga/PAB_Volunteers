package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.utils.AppError
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
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

    /**
     * Checks for space and takes it in one transaction, reading the activity fresh from the
     * server. Checking the copy already on the phone let two people take the last spot, and
     * saving the place and the count separately let them drift apart if one save failed.
     */
    suspend fun join(activity: Activity, user: User): Result<Unit> {
        return try {
            // Places taken before ids were fixed still count.
            if (getMySignup(activity.id, user.uid) != null) return Result.success(Unit)

            val activityRef = activitiesCollection().document(activity.id)
            val signupRef = signupsCollection().document(ActivitySignup.idFor(activity.id, user.uid))

            db.runTransaction { transaction ->
                val current = transaction.get(activityRef).toObject(Activity::class.java)
                    ?: throw AppError(R.string.error_record_missing)
                val alreadyJoined = transaction.get(signupRef).exists()

                if (!alreadyJoined) {
                    if (current.status != Activity.STATUS_PUBLISHED ||
                        current.endsAtMillis <= DateUtils.now()
                    ) {
                        throw AppError(R.string.error_activity_closed)
                    }
                    if (current.filledSpots >= current.totalSpots) {
                        throw AppError(R.string.error_spots_full)
                    }
                    transaction.set(
                        signupRef,
                        ActivitySignup(
                            activityId = current.id,
                            userId = user.uid,
                            volunteerName = user.fullName,
                            activityTitle = current.title,
                            programme = current.programme,
                            date = current.date,
                            dateMillis = current.dateMillis,
                            startTime = current.startTime,
                            endTime = current.endTime,
                            location = current.location,
                            signedUpDate = DateUtils.now()
                        )
                    )
                    transaction.update(activityRef, "filledSpots", current.filledSpots + 1)
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Gives the place back in the same transaction that removes it. */
    suspend fun leave(signup: ActivitySignup): Result<Unit> {
        return try {
            val signupRef = signupsCollection().document(signup.id)
            val activityRef = activitiesCollection().document(signup.activityId)

            db.runTransaction { transaction ->
                val activity = transaction.get(activityRef)
                transaction.delete(signupRef)
                // The activity may already have been deleted by an admin.
                val filled = activity.getLong("filledSpots") ?: 0L
                if (activity.exists() && filled > 0) {
                    transaction.update(activityRef, "filledSpots", filled - 1)
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
