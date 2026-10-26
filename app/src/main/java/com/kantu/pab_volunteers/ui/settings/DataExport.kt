package com.kantu.pab_volunteers.ui.settings

import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The file behind Settings, Privacy, "Download my data": everything the app holds about one
 * volunteer, written out in the order they would expect to read it. Plain text rather than
 * JSON, because the person asking for it is a volunteer and not a developer.
 */
object DataExport {

    private const val NOT_GIVEN = "Not given"

    fun fileName(now: Long): String =
        "philisa-volunteers-my-data-" +
            SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(Date(now)) + ".txt"

    /**
     * [formatDate] is handed in so the dates can be checked in a test. The app always uses
     * DateUtils, which writes the month name in whichever language Settings is set to.
     */
    fun asText(
        user: User,
        signups: List<ActivitySignup>,
        now: Long,
        formatDate: (Long) -> String = DateUtils::formatDate
    ): String {
        val summary = AccountSummary.from(signups, now)
        val lines = mutableListOf(
            "Philisa Volunteers",
            "What this app holds about me",
            "",
            "Downloaded: " + formatDate(now),
            "",
            "MY PROFILE",
            "Name: " + orNotGiven(user.fullName),
            "Email: " + orNotGiven(user.email),
            "Phone: " + orNotGiven(user.phone),
            "Area: " + orNotGiven(user.area),
            "Volunteer ID: " + orNotGiven(user.volunteerId),
            "Programmes I chose: " + orNotGiven(user.programmeInterests.joinToString(", ")),
            "Joined: " + orNotGiven(formatDate(user.joinedDate)),
            "",
            "MY VOLUNTEERING",
            "Activities completed: " + summary.completed,
            "Hours given: " + summary.hours,
            "Places still to come: " + summary.upcoming,
            "",
            "MY SIGN-UPS"
        )

        if (signups.isEmpty()) {
            lines += "I have not signed up for anything yet."
        } else {
            // Newest first, which is the order the app shows them in.
            signups.sortedByDescending { it.dateMillis }.forEach { signup ->
                lines += ""
                lines += orNotGiven(signup.activityTitle)
                lines += "  Programme: " + orNotGiven(signup.programme)
                lines += "  When: " + whenItIs(signup)
                lines += "  Where: " + orNotGiven(signup.location)
                lines += "  Signed up: " + orNotGiven(formatDate(signup.signedUpDate))
            }
        }

        lines += ""
        lines += "Philisa Abafazi Bethu holds nothing else about me in this app."
        lines += "To have it removed, use Settings, Delete my account."
        return lines.joinToString("\n")
    }

    private fun whenItIs(signup: ActivitySignup): String {
        val day = orNotGiven(signup.date)
        val times = listOf(signup.startTime, signup.endTime).filter { it.isNotBlank() }
        return if (times.size == 2) "$day, ${times[0]} to ${times[1]}" else day
    }

    /** A blank field says so, rather than leaving a line that looks like something went wrong. */
    private fun orNotGiven(value: String): String = value.trim().ifEmpty { NOT_GIVEN }
}
