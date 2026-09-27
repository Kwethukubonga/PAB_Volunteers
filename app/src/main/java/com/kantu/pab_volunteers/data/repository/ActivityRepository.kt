package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.tasks.await

class ActivityRepository {

    private val db = FirestoreManager.db
    private fun activitiesCollection() = db.collection(Constants.COLLECTION_ACTIVITIES)
    private fun signupsCollection() = db.collection(Constants.COLLECTION_SIGNUPS)

    suspend fun createActivity(activity: Activity, createdBy: String): Result<String> {
        return try {
            val now = DateUtils.now()
            val toSave = activity.copy(
                createdBy = createdBy,
                createdDate = now,
                publishedAt = if (activity.status == Activity.STATUS_PUBLISHED) now else 0L
            )
            val ref = activitiesCollection().add(toSave).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Writes only the fields the editor changes. Saving the whole activity would put back
     * the spot count from when the editor was opened, undoing anyone who joined meanwhile.
     */
    suspend fun updateActivity(activity: Activity): Result<Unit> {
        return try {
            activitiesCollection().document(activity.id).update(
                mapOf(
                    "title" to activity.title,
                    "programme" to activity.programme,
                    "date" to activity.date,
                    "dateMillis" to activity.dateMillis,
                    "startTime" to activity.startTime,
                    "endTime" to activity.endTime,
                    "location" to activity.location,
                    "volunteerRole" to activity.volunteerRole,
                    "totalSpots" to activity.totalSpots,
                    "description" to activity.description,
                    "status" to activity.status,
                    "publishedAt" to activity.publishedAt
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPublishStatus(activityId: String, status: String): Result<Unit> {
        return try {
            val changes = mutableMapOf<String, Any>("status" to status)
            if (status == Activity.STATUS_PUBLISHED) changes["publishedAt"] = DateUtils.now()
            activitiesCollection().document(activityId).update(changes).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Removes the activity and every sign-up for it together, so none are left behind. */
    suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            val signups = signupsCollection()
                .whereEqualTo("activityId", activityId)
                .get()
                .await()
            val batch = db.batch()
            signups.documents.forEach { batch.delete(it.reference) }
            batch.delete(activitiesCollection().document(activityId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivity(activityId: String): Activity? {
        return activitiesCollection().document(activityId).get().await()
            .toObject(Activity::class.java)
    }

    suspend fun getPublishedActivities(): List<Activity> {
        return activitiesCollection()
            .whereEqualTo("status", Activity.STATUS_PUBLISHED)
            .get()
            .await()
            .toObjects(Activity::class.java)
            .sortedBy { it.dateMillis }
    }

    // Only activities published after [since], so the background check reads very little.
    suspend fun getPublishedSince(since: Long): List<Activity> {
        return activitiesCollection()
            .whereGreaterThan("publishedAt", since)
            .get()
            .await()
            .toObjects(Activity::class.java)
            .filter { it.status == Activity.STATUS_PUBLISHED }
    }

    // Activities on or after [dayStart], used to spot ones that have just filled up.
    suspend fun getActivitiesFrom(dayStart: Long): List<Activity> {
        return activitiesCollection()
            .whereGreaterThanOrEqualTo("dateMillis", dayStart)
            .get()
            .await()
            .toObjects(Activity::class.java)
    }

    suspend fun getAllActivities(): List<Activity> {
        return activitiesCollection()
            .get()
            .await()
            .toObjects(Activity::class.java)
            .sortedByDescending { it.createdDate }
    }
}
