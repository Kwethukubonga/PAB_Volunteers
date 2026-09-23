package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.tasks.await

class ActivityRepository {

    private val db = FirestoreManager.db
    private fun activitiesCollection() = db.collection(Constants.COLLECTION_ACTIVITIES)

    suspend fun createActivity(activity: Activity, createdBy: String): Result<String> {
        return try {
            val toSave = activity.copy(createdBy = createdBy, createdDate = DateUtils.now())
            val ref = activitiesCollection().add(toSave).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateActivity(activity: Activity): Result<Unit> {
        return try {
            activitiesCollection().document(activity.id).set(activity).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPublishStatus(activityId: String, status: String): Result<Unit> {
        return try {
            activitiesCollection().document(activityId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            activitiesCollection().document(activityId).delete().await()
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

    suspend fun getAllActivities(): List<Activity> {
        return activitiesCollection()
            .get()
            .await()
            .toObjects(Activity::class.java)
            .sortedByDescending { it.createdDate }
    }
}
