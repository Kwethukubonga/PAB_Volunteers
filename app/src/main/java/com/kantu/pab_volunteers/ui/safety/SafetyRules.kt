package com.kantu.pab_volunteers.ui.safety

import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.ActivityFeedback
import com.kantu.pab_volunteers.data.model.Clearance

/** Where a volunteer's background check stands, as Settings, Profile and the admin show it. */
enum class ClearanceState {
    NONE,
    REQUESTED,
    CLEARED,
    EXPIRED;

    companion object {
        /** How long a clearance lasts once an admin has checked the documents. */
        const val VALID_MONTHS = 12

        fun of(clearance: Clearance?, now: Long): ClearanceState = when {
            clearance == null -> NONE
            clearance.status == Clearance.STATUS_REQUESTED -> REQUESTED
            clearance.status == Clearance.STATUS_CLEARED && clearance.expiresAt > now -> CLEARED
            clearance.status == Clearance.STATUS_CLEARED -> EXPIRED
            else -> NONE
        }
    }
}

/** Checks on a concern before it is sent. The rules check the same length limit on the server. */
object ConcernRules {

    const val MAX_LENGTH = 2000

    @StringRes
    fun problemWith(message: String): Int? = when {
        message.isBlank() -> R.string.concern_error_empty
        message.trim().length > MAX_LENGTH -> R.string.concern_error_too_long
        else -> null
    }
}

/** Checks on "How did it go?" before it is sent, matching the limits in the rules. */
object FeedbackRules {

    const val MAX_COMMENT = 500

    @StringRes
    fun problemWith(rating: Int, comment: String): Int? = when {
        rating !in 1..5 -> R.string.feedback_error_rating
        comment.trim().length > MAX_COMMENT -> R.string.feedback_error_too_long
        else -> null
    }
}

/** What the admin sees about an activity's feedback: how many, the average, and what was said. */
data class FeedbackSummary(val count: Int, val averageTenths: Int, val comments: List<ActivityFeedback>) {

    /** The average out of 5 with one decimal, for example 4.3. */
    val average: Double get() = averageTenths / 10.0

    companion object {
        fun of(feedback: List<ActivityFeedback>): FeedbackSummary {
            val rated = feedback.filter { it.rating in 1..5 }
            val tenths = if (rated.isEmpty()) 0 else {
                // Rounded to one decimal place without floating point surprises.
                (rated.sumOf { it.rating } * 100 / rated.size + 5) / 10
            }
            return FeedbackSummary(
                count = rated.size,
                averageTenths = tenths,
                comments = rated.filter { it.comment.isNotBlank() }
            )
        }
    }
}
