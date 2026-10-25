package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId

/**
 * A volunteer's background check (police clearance or child-protection check). Kept in its own
 * collection, one record per volunteer, rather than on the user record: a volunteer saving their
 * profile can then never overwrite an admin's decision. Volunteers bring their documents to the
 * centre and an admin checks them in person, so nothing is uploaded.
 */
data class Clearance(
    @DocumentId
    val userId: String = "",
    val volunteerName: String = "",
    val status: String = STATUS_REQUESTED,
    val requestedAt: Long = 0L,
    val clearedAt: Long = 0L,
    val expiresAt: Long = 0L,
    val checkedBy: String = ""
) {
    companion object {
        const val STATUS_REQUESTED = "requested"
        const val STATUS_CLEARED = "cleared"
    }
}

/** Something a volunteer wants Philisa's admins to know about. Only admins can read these. */
data class Concern(
    @DocumentId
    val id: String = "",
    val reportedBy: String = "",
    val reporterName: String = "",
    // Empty when it isn't about a particular activity.
    val activityId: String = "",
    val activityTitle: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val status: String = STATUS_NEW,
    val handledBy: String = "",
    val handledAt: Long = 0L
) {
    companion object {
        const val STATUS_NEW = "new"
        const val STATUS_HANDLED = "handled"
    }
}

/** "How did it go?" from a volunteer who was on an activity. One per person per activity. */
data class ActivityFeedback(
    @DocumentId
    val id: String = "",
    val activityId: String = "",
    val userId: String = "",
    val volunteerName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L
) {
    companion object {
        /** The same shape as a sign-up's id, so the rules can tie it to that sign-up. */
        fun idFor(activityId: String, userId: String) = "${activityId}_$userId"
    }
}
