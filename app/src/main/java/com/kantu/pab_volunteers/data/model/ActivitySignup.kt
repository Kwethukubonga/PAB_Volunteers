package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId

// A volunteer's place on an activity. Joining is immediate, so there is no status to approve.
// Leaving simply deletes the record.
data class ActivitySignup(
    @DocumentId
    val id: String = "",
    val activityId: String = "",
    val userId: String = "",
    val volunteerName: String = "",
    val activityTitle: String = "",
    val programme: String = "",
    val date: String = "",
    val dateMillis: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val signedUpDate: Long = 0L
) {
    /**
     * Enough of the activity to keep it in the volunteer's history, and in their hours,
     * after an admin has unpublished or deleted it.
     */
    fun asActivity(): Activity = Activity(
        id = activityId,
        title = activityTitle,
        programme = programme,
        date = date,
        dateMillis = dateMillis,
        startTime = startTime,
        endTime = endTime,
        location = location
    )

    companion object {
        /** One fixed id per person per activity, so the same place cannot be taken twice. */
        fun idFor(activityId: String, userId: String) = "${activityId}_$userId"
    }
}
