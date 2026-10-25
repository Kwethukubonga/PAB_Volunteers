package com.kantu.pab_volunteers.ui.volunteer.community

import com.kantu.pab_volunteers.data.model.Announcement
import java.util.Calendar
import java.util.TimeZone

/** A row in Community: a month heading that folds its announcements away, or an announcement. */
sealed class CommunityRow {
    data class Month(val key: String, val start: Long, val count: Int, val collapsed: Boolean) : CommunityRow()
    data class Post(val announcement: Announcement) : CommunityRow()
}

/** Groups announcements by the month they were posted, newest first. Plain Kotlin, so it is tested. */
object AnnouncementMonths {

    /** "2026-10" for any time in October 2026, in [zone]. */
    fun keyFor(millis: Long, zone: TimeZone = TimeZone.getDefault()): String {
        val calendar = Calendar.getInstance(zone).apply { timeInMillis = millis }
        return "%04d-%02d".format(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    /** When it was posted: the last time it was published, or when it was written. */
    fun postedAt(announcement: Announcement): Long =
        announcement.publishedAt.takeIf { it > 0 } ?: announcement.date

    /** Only the newest month starts open, so a long history doesn't bury this month's news. */
    fun startCollapsed(announcements: List<Announcement>, zone: TimeZone = TimeZone.getDefault()): Set<String> =
        announcements.map { keyFor(postedAt(it), zone) }.distinct().drop(1).toSet()

    fun rows(
        announcements: List<Announcement>,
        collapsed: Set<String>,
        zone: TimeZone = TimeZone.getDefault()
    ): List<CommunityRow> =
        announcements
            .sortedByDescending { postedAt(it) }
            .groupBy { keyFor(postedAt(it), zone) }
            .flatMap { (key, posts) ->
                val folded = key in collapsed
                listOf(CommunityRow.Month(key, postedAt(posts.first()), posts.size, folded)) +
                    if (folded) emptyList() else posts.map { CommunityRow.Post(it) }
            }
}
