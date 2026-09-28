package com.kantu.pab_volunteers.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class User(
    @DocumentId
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val area: String = "",
    val programmeInterests: List<String> = emptyList(),
    val favouriteActivityIds: List<String> = emptyList(),
    val role: String = ROLE_VOLUNTEER,
    val volunteerId: String = "",
    val profileComplete: Boolean = false,
    val joinedDate: Long = 0L
) {
    // These are derived, so they must not be written to Firestore as fields.
    @get:Exclude
    val fullName: String get() = "$firstName $lastName".trim()

    @get:Exclude
    val isAdmin: Boolean get() = role == ROLE_ADMIN

    companion object {
        const val ROLE_VOLUNTEER = "volunteer"
        const val ROLE_ADMIN = "admin"
    }
}
