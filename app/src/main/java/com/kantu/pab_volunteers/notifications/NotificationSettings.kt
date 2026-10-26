package com.kantu.pab_volunteers.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Which notifications the person wants, chosen in Settings. Everything starts switched on. */
object NotificationSettings {

    const val NEW_ACTIVITIES = "notify_new_activities"
    const val ANNOUNCEMENTS = "notify_announcements"
    const val ACTIVITY_FULL = "notify_activity_full"
    const val ONLY_MY_PROGRAMMES = "notify_only_my_programmes"

    private const val PREFS = "pab_settings"
    private const val KEY_PERMISSION_ASKED = "notification_permission_asked"
    private const val KEY_REMINDER_LEAD = "reminder_lead"

    /**
     * Whether a kind of notification is wanted. Most start switched on, so [whenUnset] is
     * true unless a setting would take something away by default.
     */
    fun isOn(context: Context, key: String, whenUnset: Boolean = true): Boolean =
        prefs(context).getBoolean(key, whenUnset)

    fun set(context: Context, key: String, on: Boolean) {
        prefs(context).edit().putBoolean(key, on).apply()
    }

    /** How long before a joined activity the reminder comes. A day before, until changed. */
    fun reminderLead(context: Context): ReminderLead {
        val saved = prefs(context).getString(KEY_REMINDER_LEAD, ReminderLead.DAY.name)
        return ReminderLead.values().firstOrNull { it.name == saved } ?: ReminderLead.DAY
    }

    fun setReminderLead(context: Context, lead: ReminderLead) {
        prefs(context).edit().putString(KEY_REMINDER_LEAD, lead.name).apply()
    }

    /** Android 13 and newer need the person's permission before any notification shows. */
    fun needsPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    fun wasPermissionAsked(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PERMISSION_ASKED, false)

    fun markPermissionAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_PERMISSION_ASKED, true).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
