package com.kantu.pab_volunteers.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.ui.auth.SplashActivity
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.Constants

/** Builds and shows the app's phone notifications. Each kind has its own channel. */
object Notifications {

    private const val CHANNEL_ACTIVITIES = "new_activities"
    private const val CHANNEL_ANNOUNCEMENTS = "announcements"
    private const val CHANNEL_FULL = "activity_full"

    // More than this at once becomes one summary instead of a pile of separate notifications.
    private const val MAX_SEPARATE = 3

    private const val SUMMARY_ACTIVITIES = 1
    private const val SUMMARY_ANNOUNCEMENTS = 2
    private const val SUMMARY_FULL = 3

    /** Run on every start, so the channel names follow the language picked in Settings. */
    fun createChannels(context: Context) {
        val text = AppLanguage.localized(context)
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(
            listOf(
                channel(text, CHANNEL_ACTIVITIES, R.string.channel_activities, R.string.channel_activities_desc),
                channel(text, CHANNEL_ANNOUNCEMENTS, R.string.channel_announcements, R.string.channel_announcements_desc),
                channel(text, CHANNEL_FULL, R.string.channel_full, R.string.channel_full_desc)
            )
        )
    }

    private fun channel(text: Context, id: String, nameRes: Int, descriptionRes: Int) =
        NotificationChannelCompat.Builder(id, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(text.getString(nameRes))
            .setDescription(text.getString(descriptionRes))
            .build()

    /** False when the phone has notifications for this app switched off. */
    fun allowedOnPhone(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun showNewActivities(context: Context, activities: List<Activity>) {
        if (activities.isEmpty()) return
        val text = AppLanguage.localized(context)
        if (activities.size > MAX_SEPARATE) {
            post(
                context, SUMMARY_ACTIVITIES,
                builder(text, CHANNEL_ACTIVITIES, Bundle.EMPTY, SUMMARY_ACTIVITIES)
                    .setContentTitle(
                        text.resources.getQuantityString(
                            R.plurals.notify_new_activities, activities.size, activities.size
                        )
                    )
                    .setContentText(text.getString(R.string.notify_open_to_see))
            )
            return
        }
        activities.forEach { activity ->
            val id = "activity:${activity.id}".hashCode()
            post(
                context, id,
                builder(text, CHANNEL_ACTIVITIES, bundleOf(Constants.EXTRA_ACTIVITY_ID to activity.id), id)
                    .setContentTitle(text.getString(R.string.notify_new_activity, activity.title))
                    .setContentText("${activity.programme} · ${activity.dateTimeLabel}")
            )
        }
    }

    fun showAnnouncements(context: Context, announcements: List<Announcement>) {
        if (announcements.isEmpty()) return
        val text = AppLanguage.localized(context)
        if (announcements.size > MAX_SEPARATE) {
            post(
                context, SUMMARY_ANNOUNCEMENTS,
                builder(text, CHANNEL_ANNOUNCEMENTS, Bundle.EMPTY, SUMMARY_ANNOUNCEMENTS)
                    .setContentTitle(
                        text.resources.getQuantityString(
                            R.plurals.notify_new_announcements, announcements.size, announcements.size
                        )
                    )
                    .setContentText(text.getString(R.string.notify_open_to_see))
            )
            return
        }
        announcements.forEach { announcement ->
            val id = "announcement:${announcement.id}".hashCode()
            post(
                context, id,
                builder(
                    text, CHANNEL_ANNOUNCEMENTS,
                    bundleOf(Constants.EXTRA_ANNOUNCEMENT_ID to announcement.id), id
                )
                    .setContentTitle(announcement.title)
                    .setContentText(announcement.messageBody)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(announcement.messageBody))
            )
        }
    }

    /** For admins: activities where every spot has just been taken. */
    fun showFullActivities(context: Context, activities: List<Activity>) {
        if (activities.isEmpty()) return
        val text = AppLanguage.localized(context)
        if (activities.size > MAX_SEPARATE) {
            post(
                context, SUMMARY_FULL,
                builder(text, CHANNEL_FULL, Bundle.EMPTY, SUMMARY_FULL)
                    .setContentTitle(
                        text.resources.getQuantityString(
                            R.plurals.notify_activities_full, activities.size, activities.size
                        )
                    )
                    .setContentText(text.getString(R.string.notify_open_to_see))
            )
            return
        }
        activities.forEach { activity ->
            val id = "full:${activity.id}".hashCode()
            post(
                context, id,
                builder(text, CHANNEL_FULL, bundleOf(Constants.EXTRA_ACTIVITY_ID to activity.id), id)
                    .setContentTitle(text.getString(R.string.notify_activity_full, activity.title))
                    .setContentText(
                        text.resources.getQuantityString(
                            R.plurals.notify_all_spots_taken, activity.totalSpots, activity.totalSpots
                        )
                    )
            )
        }
    }

    // Tapping opens the app through the splash screen, which signs in and then goes to the item.
    private fun builder(
        context: Context,
        channel: String,
        extras: Bundle,
        requestCode: Int
    ): NotificationCompat.Builder {
        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtras(extras)
        }
        val tap = PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.purple_700))
            .setContentIntent(tap)
            .setAutoCancel(true)
    }

    private fun post(context: Context, id: Int, builder: NotificationCompat.Builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
