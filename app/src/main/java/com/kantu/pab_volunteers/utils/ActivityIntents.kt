package com.kantu.pab_volunteers.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity

/**
 * Hands an activity to other apps on the phone: the share sheet, the calendar, the dialler and
 * messages. Each one only opens the other app with things filled in; nothing is sent until the
 * person taps send or call there.
 */
object ActivityIntents {

    /** "Share where I'm volunteering": what, where and when, to WhatsApp, SMS or anything else. */
    fun share(context: Context, activity: Activity) {
        val text = context.getString(R.string.share_activity_text, activity.title, activity.location, activity.dateTimeLabel)
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        open(context, Intent.createChooser(send, context.getString(R.string.action_share_activity)))
    }

    /** Opens the phone's calendar with the activity filled in, for the person to save (I3). */
    fun addToCalendar(context: Context, activity: Activity) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, activity.startsAtMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, activity.endsAtMillis)
            .putExtra(CalendarContract.Events.TITLE, activity.title)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, activity.location)
            .putExtra(CalendarContract.Events.DESCRIPTION, listOf(activity.programme, activity.volunteerRole).filter { it.isNotBlank() }.joinToString(" · "))
        open(context, intent, R.string.error_no_calendar_app)
    }

    /** The dialler with the number ready; the person still taps call. */
    fun dial(context: Context, number: String) {
        open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filterNot { it.isWhitespace() })))
    }

    /** Messages with the number and text ready; the person still taps send. */
    fun textMessage(context: Context, number: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number.filterNot { it.isWhitespace() }))
            .putExtra("sms_body", body)
        open(context, intent)
    }

    private fun open(context: Context, intent: Intent, missingApp: Int = R.string.error_no_app) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, missingApp, Toast.LENGTH_LONG).show()
        }
    }
}
