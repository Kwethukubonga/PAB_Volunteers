package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Announcement(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val messageBody: String = "",
    // Empty when the admin did not attach a picture.
    val imageUrl: String = "",
    val date: Long = 0L,
    val status: String = STATUS_DRAFT,
    val createdBy: String = "",
    // When it last went from hidden to published. Notifications look for anything newer.
    val publishedAt: Long = 0L,
    // One entry per volunteer who gave it a thumbs up.
    val thumbsUpBy: List<String> = emptyList()
) {
    // Derived, so they must not be written to Firestore as fields.
    @get:Exclude
    val thumbsUpCount: Int get() = thumbsUpBy.size

    @get:Exclude
    val hasImage: Boolean get() = imageUrl.isNotBlank()

    fun isThumbedUpBy(userId: String): Boolean = thumbsUpBy.contains(userId)

    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_PUBLISHED = "published"
    }
}
