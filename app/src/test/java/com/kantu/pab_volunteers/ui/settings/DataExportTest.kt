package com.kantu.pab_volunteers.ui.settings

import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.data.model.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The file behind "Download my data". A volunteer asking for their own data should be able
 * to read it without help, so this checks it reads as a document rather than a dump.
 */
class DataExportTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_800_000_000_000L

    // Dates are handed in so this does not depend on the phone's language.
    private val dates: (Long) -> String = { if (it <= 0L) "" else "day $it" }

    private val nomsa = User(
        firstName = "Nomsa",
        lastName = "Dlamini",
        email = "nomsa@example.org",
        phone = "082 123 4567",
        area = "Steenberg",
        programmeInterests = listOf("Senior Programme", "Soup Kitchen"),
        volunteerId = "PAB-000123",
        joinedDate = now - 90 * day
    )

    private fun signup(title: String, dateMillis: Long) = ActivitySignup(
        activityTitle = title,
        programme = "Senior Programme",
        date = "12 March 2026",
        dateMillis = dateMillis,
        startTime = "09:00",
        endTime = "12:00",
        location = "Lavender Hill",
        signedUpDate = dateMillis - 7 * day
    )

    @Test
    fun `the profile is written out under its own heading`() {
        val text = DataExport.asText(nomsa, emptyList(), now, dates)

        assertTrue(text.contains("MY PROFILE"))
        assertTrue(text.contains("Name: Nomsa Dlamini"))
        assertTrue(text.contains("Email: nomsa@example.org"))
        assertTrue(text.contains("Area: Steenberg"))
        assertTrue(text.contains("Volunteer ID: PAB-000123"))
        assertTrue(text.contains("Programmes I chose: Senior Programme, Soup Kitchen"))
    }

    @Test
    fun `every sign-up is listed with when and where`() {
        val text = DataExport.asText(nomsa, listOf(signup("Community Kitchen", now - 30 * day)), now, dates)

        assertTrue(text.contains("Community Kitchen"))
        assertTrue(text.contains("  Programme: Senior Programme"))
        assertTrue(text.contains("  When: 12 March 2026, 09:00 to 12:00"))
        assertTrue(text.contains("  Where: Lavender Hill"))
    }

    @Test
    fun `sign-ups are newest first`() {
        val text = DataExport.asText(
            nomsa,
            listOf(signup("Older", now - 60 * day), signup("Newer", now - 10 * day)),
            now,
            dates
        )

        assertTrue(text.indexOf("Newer") < text.indexOf("Older"))
    }

    @Test
    fun `the totals come from the same sums as the profile screen`() {
        // Two finished three-hour activities and one still to come.
        val text = DataExport.asText(
            nomsa,
            listOf(
                signup("One", now - 30 * day),
                signup("Two", now - 20 * day),
                signup("Three", now + 10 * day)
            ),
            now,
            dates
        )

        assertTrue(text.contains("Activities completed: 2"))
        assertTrue(text.contains("Hours given: 6"))
        assertTrue(text.contains("Places still to come: 1"))
    }

    @Test
    fun `a volunteer with no sign-ups is told so rather than shown an empty heading`() {
        val text = DataExport.asText(nomsa, emptyList(), now, dates)

        assertTrue(text.contains("I have not signed up for anything yet."))
    }

    @Test
    fun `a blank field says so instead of trailing off`() {
        val text = DataExport.asText(User(firstName = "Nomsa"), emptyList(), now, dates)

        assertTrue(text.contains("Email: Not given"))
        assertTrue(text.contains("Phone: Not given"))
        assertTrue(text.contains("Programmes I chose: Not given"))
        assertTrue(text.contains("Joined: Not given"))
        assertFalse(text.contains("Name: Not given"))
    }

    @Test
    fun `it says where to go to have the data removed`() {
        val text = DataExport.asText(nomsa, emptyList(), now, dates)

        assertTrue(text.contains("Settings, Delete my account"))
    }

    @Test
    fun `the file is named after the day it was taken`() {
        assertTrue(DataExport.fileName(now).startsWith("philisa-volunteers-my-data-"))
        assertTrue(DataExport.fileName(now).endsWith(".txt"))
    }
}
