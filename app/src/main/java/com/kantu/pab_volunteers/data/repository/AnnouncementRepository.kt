package com.kantu.pab_volunteers.data.repository

import com.kantu.pab_volunteers.data.firebase.FirestoreManager
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.tasks.await

class AnnouncementRepository {

    private val db = FirestoreManager.db
    private fun announcementsCollection() = db.collection(Constants.COLLECTION_ANNOUNCEMENTS)

    suspend fun createAnnouncement(announcement: Announcement, createdBy: String): Result<String> {
        return try {
            val now = DateUtils.now()
            val toSave = announcement.copy(
                createdBy = createdBy,
                date = now,
                publishedAt = if (announcement.status == Announcement.STATUS_PUBLISHED) now else 0L
            )
            val ref = announcementsCollection().add(toSave).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            announcementsCollection().document(announcement.id).set(announcement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPublishStatus(announcementId: String, status: String): Result<Unit> {
        return try {
            val changes = mutableMapOf<String, Any>("status" to status)
            if (status == Announcement.STATUS_PUBLISHED) changes["publishedAt"] = DateUtils.now()
            announcementsCollection().document(announcementId).update(changes).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        return try {
            announcementsCollection().document(announcementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnnouncement(announcementId: String): Announcement? {
        return announcementsCollection().document(announcementId).get().await()
            .toObject(Announcement::class.java)
    }

    suspend fun getPublishedAnnouncements(): List<Announcement> {
        return announcementsCollection()
            .whereEqualTo("status", Announcement.STATUS_PUBLISHED)
            .get()
            .await()
            .toObjects(Announcement::class.java)
            .sortedByDescending { it.date }
    }

    // Only announcements published after [since], so the background check reads very little.
    suspend fun getPublishedSince(since: Long): List<Announcement> {
        return announcementsCollection()
            .whereGreaterThan("publishedAt", since)
            .get()
            .await()
            .toObjects(Announcement::class.java)
            .filter { it.status == Announcement.STATUS_PUBLISHED }
    }

    suspend fun getAllAnnouncements(): List<Announcement> {
        return announcementsCollection()
            .get()
            .await()
            .toObjects(Announcement::class.java)
            .sortedByDescending { it.date }
    }
}
