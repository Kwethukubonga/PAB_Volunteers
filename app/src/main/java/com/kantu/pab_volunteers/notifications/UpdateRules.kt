package com.kantu.pab_volunteers.notifications

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.Announcement

/**
 * What the background check counts as news. Kept apart from [UpdatesWorker] so the rules can be
 * tested without a phone: the worker only fetches, stores what it has seen, and shows the result.
 */
object UpdateRules {

    /** How much further back than the last check to look, in case the poster's clock is behind. */
    const val OVERLAP_MILLIS = 6L * 60 * 60 * 1000

    /** Activities published since checks began that haven't finished yet. */
    fun freshActivities(published: List<Activity>, startedAt: Long, now: Long): List<Activity> =
        published.filter { it.publishedAt > startedAt && it.endsAtMillis > now }

    /** Announcements published since checks began. */
    fun freshAnnouncements(published: List<Announcement>, startedAt: Long): List<Announcement> =
        published.filter { it.publishedAt > startedAt }

    /** Published activities with every spot taken that haven't finished yet. */
    fun fullActivities(activities: List<Activity>, now: Long): List<Activity> =
        activities.filter {
            it.status == Activity.STATUS_PUBLISHED &&
                it.totalSpots > 0 &&
                it.filledSpots >= it.totalSpots &&
                it.endsAtMillis > now
        }

    /** The items not already shown, so nothing is announced twice. */
    fun <T> notShownYet(items: List<T>, shown: Set<String>, idOf: (T) -> String): List<T> =
        items.filter { idOf(it) !in shown }
}
