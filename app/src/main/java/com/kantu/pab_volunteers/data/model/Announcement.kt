package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId

data class Announcement(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val messageBody: String = "",
    val date: Long = 0L,
    val status: String = STATUS_DRAFT,
    val createdBy: String = ""
) {
    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_PUBLISHED = "published"
    }
}
