package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.kantu.pab_volunteers.utils.DateUtils

// This is a volunteer opportunity, not an android.app.Activity.
data class Activity(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val programme: String = "",
    val date: String = "",
    val dateMillis: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val volunteerRole: String = "",
    val totalSpots: Int = 0,
    val filledSpots: Int = 0,
    val description: String = "",
    val status: String = STATUS_DRAFT,
    val createdBy: String = "",
    val createdDate: Long = 0L
) {
    // Derived, so they must not be written to Firestore as fields.
    @get:Exclude
    val spotsRemaining: Int get() = (totalSpots - filledSpots).coerceAtLeast(0)

    // An activity counts as finished once this moment has passed.
    @get:Exclude
    val endsAtMillis: Long get() = DateUtils.endOfActivity(dateMillis, startTime, endTime)

    @get:Exclude
    val dateTimeLabel: String get() = "$date · $startTime - $endTime"

    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_PUBLISHED = "published"
    }
}
