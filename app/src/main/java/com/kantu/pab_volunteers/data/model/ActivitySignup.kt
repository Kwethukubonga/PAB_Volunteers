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
    val dateMillis: Long = 0L,
    val signedUpDate: Long = 0L
)
