package com.kantu.pab_volunteers.ui.volunteer.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.utils.DateUtils

/**
 * Milestones on the Profile screen, a small thank-you for time given. They are worked out from the
 * volunteer's finished activities each time, never stored, so there is nothing anyone could
 * award themselves: the sign-ups behind them are already protected by the security rules.
 */
data class Milestone(
    val kind: Kind,
    val progress: Int
) {
    enum class Kind(val target: Int, @StringRes val title: Int, @DrawableRes val icon: Int) {
        FIRST_SHIFT(1, R.string.milestone_first_shift, R.drawable.ic_star),
        TEN_ACTIVITIES(10, R.string.milestone_ten_activities, R.drawable.ic_trophy),
        TWENTY_FIVE_HOURS(25, R.string.milestone_twenty_five_hours, R.drawable.ic_schedule),
        THREE_PROGRAMMES(3, R.string.milestone_three_programmes, R.drawable.ic_people)
    }

    val earned: Boolean get() = progress >= kind.target

    companion object {
        fun from(finished: List<Activity>): List<Milestone> {
            val hours = finished.sumOf { DateUtils.lengthInMinutes(it.startTime, it.endTime) } / 60
            val programmes = finished.map { it.programme.trim().lowercase() }.filter { it.isNotEmpty() }.toSet().size
            return listOf(
                Milestone(Kind.FIRST_SHIFT, finished.size.coerceAtMost(Kind.FIRST_SHIFT.target)),
                Milestone(Kind.TEN_ACTIVITIES, finished.size.coerceAtMost(Kind.TEN_ACTIVITIES.target)),
                Milestone(Kind.TWENTY_FIVE_HOURS, hours.coerceAtMost(Kind.TWENTY_FIVE_HOURS.target)),
                Milestone(Kind.THREE_PROGRAMMES, programmes.coerceAtMost(Kind.THREE_PROGRAMMES.target))
            )
        }
    }
}
