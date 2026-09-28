package com.kantu.pab_volunteers.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.repository.ActivityRepository
import com.kantu.pab_volunteers.data.repository.AnnouncementRepository
import com.kantu.pab_volunteers.data.repository.UserRepository
import com.kantu.pab_volunteers.utils.DateUtils
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

/**
 * Checks Firestore in the background and shows a notification for anything new. Android runs
 * it about every 15 minutes while there is a connection, so notifications can take that long.
 *
 * Volunteers hear about new activities and announcements. Admins hear when an activity fills up.
 */
class UpdatesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuthManager.currentUser?.uid ?: return Result.success()
        return try {
            val user = UserRepository().getUser(uid) ?: return Result.success()
            val state = UpdateState(applicationContext, uid)
            when {
                user.isAdmin -> checkFullActivities(state)
                user.profileComplete -> checkNewPosts(state)
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkNewPosts(state: UpdateState) {
        val now = DateUtils.now()
        // The first check only records the starting point, so nothing old shows up as new.
        if (state.startedAt == 0L) {
            state.startedAt = now
            state.lastCheck = now
            return
        }

        // Looks back a little further than the last check, in case the admin's phone clock is
        // behind. The seen lists stop anything being announced twice.
        val since = state.lastCheck - OVERLAP_MILLIS
        val activities = ActivityRepository().getPublishedSince(since)
            .filter { it.publishedAt > state.startedAt && it.endsAtMillis > now }
        val announcements = AnnouncementRepository().getPublishedSince(since)
            .filter { it.publishedAt > state.startedAt }

        if (NotificationSettings.isOn(applicationContext, NotificationSettings.NEW_ACTIVITIES)) {
            Notifications.showNewActivities(
                applicationContext,
                activities.filter { it.id !in state.seenActivities }
            )
        }
        if (NotificationSettings.isOn(applicationContext, NotificationSettings.ANNOUNCEMENTS)) {
            Notifications.showAnnouncements(
                applicationContext,
                announcements.filter { it.id !in state.seenAnnouncements }
            )
        }

        state.seenActivities = activities.map { it.id }.toSet()
        state.seenAnnouncements = announcements.map { it.id }.toSet()
        state.lastCheck = now
    }

    private suspend fun checkFullActivities(state: UpdateState) {
        val now = DateUtils.now()
        val full = ActivityRepository().getActivitiesFrom(DateUtils.startOfDay(now))
            .filter {
                it.status == Activity.STATUS_PUBLISHED &&
                    it.totalSpots > 0 &&
                    it.filledSpots >= it.totalSpots &&
                    it.endsAtMillis > now
            }

        // Activities already full on the first check are the starting point, not news.
        if (state.fullChecked &&
            NotificationSettings.isOn(applicationContext, NotificationSettings.ACTIVITY_FULL)
        ) {
            Notifications.showFullActivities(
                applicationContext,
                full.filter { it.id !in state.fullActivities }
            )
        }
        state.fullActivities = full.map { it.id }.toSet()
        state.fullChecked = true
    }

    companion object {
        private const val WORK_NAME = "pab_updates"
        private const val OVERLAP_MILLIS = 6L * 60 * 60 * 1000

        /** Called when a signed in person opens the app. Does nothing if already scheduled. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdatesWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        /** Called on sign out, so a signed out phone stops checking. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

/** What the background check has already seen, kept per account in case a phone is shared. */
private class UpdateState(context: Context, uid: String) {

    private val prefs = context.getSharedPreferences("pab_updates_$uid", Context.MODE_PRIVATE)

    var startedAt: Long
        get() = prefs.getLong("started_at", 0L)
        set(value) = prefs.edit().putLong("started_at", value).apply()

    var lastCheck: Long
        get() = prefs.getLong("last_check", 0L)
        set(value) = prefs.edit().putLong("last_check", value).apply()

    var seenActivities: Set<String>
        get() = prefs.getStringSet("seen_activities", emptySet()).orEmpty()
        set(value) = prefs.edit().putStringSet("seen_activities", value).apply()

    var seenAnnouncements: Set<String>
        get() = prefs.getStringSet("seen_announcements", emptySet()).orEmpty()
        set(value) = prefs.edit().putStringSet("seen_announcements", value).apply()

    var fullActivities: Set<String>
        get() = prefs.getStringSet("full_activities", emptySet()).orEmpty()
        set(value) = prefs.edit().putStringSet("full_activities", value).apply()

    var fullChecked: Boolean
        get() = prefs.getBoolean("full_checked", false)
        set(value) = prefs.edit().putBoolean("full_checked", value).apply()
}
