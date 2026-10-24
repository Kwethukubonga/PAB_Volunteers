package com.kantu.pab_volunteers.utils

import android.content.Context
import com.kantu.pab_volunteers.R

/** "today", "tomorrow", "in 3 days", "2 days ago": when an activity is, next to it in a list. */
object RelativeDay {

    /** Whole calendar days from now to [dateMillis]; negative when it has passed. */
    fun daysFrom(now: Long, dateMillis: Long): Int = DateUtils.daysBetween(now, dateMillis)

    fun label(context: Context, now: Long, dateMillis: Long): String {
        val days = daysFrom(now, dateMillis)
        return when {
            days == 0 -> context.getString(R.string.relative_today)
            days == 1 -> context.getString(R.string.relative_tomorrow)
            days == -1 -> context.getString(R.string.relative_yesterday)
            days > 1 -> context.resources.getQuantityString(R.plurals.relative_in_days, days, days)
            else -> context.resources.getQuantityString(R.plurals.relative_days_ago, -days, -days)
        }
    }
}
