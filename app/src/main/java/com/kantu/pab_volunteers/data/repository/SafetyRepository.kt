package com.kantu.pab_volunteers.data.repository

import com.google.firebase.firestore.Query
import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.ActivityFeedback
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.data.model.Concern
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.tasks.await

/** Volunteer clearances. firestore.rules lets a volunteer only ask to be checked; admins decide. */
class ClearanceRepository {

    private fun clearances() = FirestoreManager.db.collection(Constants.COLLECTION_CLEARANCES)

    suspend fun get(userId: String): Clearance? =
        clearances().document(userId).get().await().toObject(Clearance::class.java)

    suspend fun request(userId: String, volunteerName: String): Result<Unit> = runCatching {
        val fields = mapOf(
            "volunteerName" to volunteerName,
            "status" to Clearance.STATUS_REQUESTED,
            "requestedAt" to DateUtils.now()
        )
        clearances().document(userId).set(fields).await()
        Unit
    }

    /** Withdraws a request, or (for an admin) removes a clearance. */
    suspend fun remove(userId: String): Result<Unit> = runCatching {
        clearances().document(userId).delete().await()
        Unit
    }

    suspend fun approve(userId: String, volunteerName: String, adminName: String, months: Int): Result<Unit> =
        runCatching {
            val now = DateUtils.now()
            val fields = mapOf(
                "volunteerName" to volunteerName,
                "status" to Clearance.STATUS_CLEARED,
                "clearedAt" to now,
                "expiresAt" to DateUtils.addMonths(now, months),
                "checkedBy" to adminName
            )
            clearances().document(userId).set(fields).await()
            Unit
        }
}

/** Concerns reported by volunteers. Anyone signed in can send one; only admins can read them. */
class ConcernRepository {

    private fun concerns() = FirestoreManager.db.collection(Constants.COLLECTION_CONCERNS)

    suspend fun report(concern: Concern): Result<Unit> = runCatching {
        val fields = mapOf(
            "reportedBy" to concern.reportedBy,
            "reporterName" to concern.reporterName,
            "activityId" to concern.activityId,
            "activityTitle" to concern.activityTitle,
            "message" to concern.message,
            "createdAt" to DateUtils.now(),
            "status" to Concern.STATUS_NEW
        )
        concerns().add(fields).await()
        Unit
    }

    suspend fun getAll(): List<Concern> =
        concerns().orderBy("createdAt", Query.Direction.DESCENDING).get().await().toObjects(Concern::class.java)

    suspend fun markHandled(concernId: String, adminName: String): Result<Unit> = runCatching {
        concerns().document(concernId).update(
            mapOf(
                "status" to Concern.STATUS_HANDLED,
                "handledBy" to adminName,
                "handledAt" to DateUtils.now()
            )
        ).await()
        Unit
    }
}

/** "How did it go?" after an activity. */
class FeedbackRepository {

    private fun feedback() = FirestoreManager.db.collection(Constants.COLLECTION_FEEDBACK)

    suspend fun getMine(activityId: String, userId: String): ActivityFeedback? =
        feedback().document(ActivityFeedback.idFor(activityId, userId)).get().await()
            .toObject(ActivityFeedback::class.java)

    suspend fun save(activityId: String, userId: String, volunteerName: String, rating: Int, comment: String): Result<Unit> =
        runCatching {
            val fields = mapOf(
                "activityId" to activityId,
                "userId" to userId,
                "volunteerName" to volunteerName,
                "rating" to rating,
                "comment" to comment,
                "createdAt" to DateUtils.now()
            )
            feedback().document(ActivityFeedback.idFor(activityId, userId)).set(fields).await()
            Unit
        }

    suspend fun getForActivity(activityId: String): List<ActivityFeedback> =
        feedback().whereEqualTo("activityId", activityId).get().await()
            .toObjects(ActivityFeedback::class.java)
            .sortedByDescending { it.createdAt }

    suspend fun getMineAll(userId: String): List<ActivityFeedback> =
        feedback().whereEqualTo("userId", userId).get().await().toObjects(ActivityFeedback::class.java)

    suspend fun delete(feedbackId: String) {
        feedback().document(feedbackId).delete().await()
    }
}
