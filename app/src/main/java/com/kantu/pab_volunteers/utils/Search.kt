package com.kantu.pab_volunteers.utils

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.User

/**
 * The search boxes on Activities and on the admin's Volunteers list. PAB's lists are small, so
 * the search runs over what is already loaded rather than asking Firestore, which has no text
 * search. Every word typed has to appear somewhere, in any order and any case, so
 * "soup lavender" finds the soup kitchen in Lavender Hill.
 */
object Search {

    fun matches(query: String, vararg fields: String): Boolean {
        val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return true
        val text = fields.joinToString(" ").lowercase()
        return words.all { it in text }
    }

    fun matches(activity: Activity, query: String): Boolean = matches(
        query, activity.title, activity.programme, activity.location, activity.volunteerRole, activity.date
    )

    fun matches(volunteer: User, query: String): Boolean = matches(
        query,
        volunteer.fullName,
        volunteer.email,
        volunteer.phone,
        volunteer.area,
        volunteer.volunteerId,
        volunteer.programmeInterests.joinToString(" ")
    )
}
