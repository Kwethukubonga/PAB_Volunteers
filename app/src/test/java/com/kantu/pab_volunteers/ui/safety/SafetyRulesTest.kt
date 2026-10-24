package com.kantu.pab_volunteers.ui.safety

import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.ActivityFeedback
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Clearance, concerns and "How did it go?" (the safety features brought over from Collective). */
class SafetyRulesTest {

    private val now = DateUtils.now()
    private val day = DateUtils.DAY_MILLIS

    @Test
    fun `no record means not checked`() {
        assertEquals(ClearanceState.NONE, ClearanceState.of(null, now))
    }

    @Test
    fun `a request waits for an admin`() {
        assertEquals(ClearanceState.REQUESTED, ClearanceState.of(Clearance(status = Clearance.STATUS_REQUESTED), now))
    }

    @Test
    fun `a clearance counts until it runs out`() {
        val cleared = Clearance(status = Clearance.STATUS_CLEARED, expiresAt = now + day)
        assertEquals(ClearanceState.CLEARED, ClearanceState.of(cleared, now))
        assertEquals(ClearanceState.EXPIRED, ClearanceState.of(cleared.copy(expiresAt = now - day), now))
    }

    @Test
    fun `a clearance lasts a year from when it is checked`() {
        val checked = 1_700_000_000_000L
        val expires = DateUtils.addMonths(checked, ClearanceState.VALID_MONTHS)
        // A year is 365 or 366 days, whatever the time zone.
        val days = (expires - checked) / day
        assertEquals(true, days in 364..366)
    }

    @Test
    fun `a concern must say something`() {
        assertEquals(R.string.concern_error_empty, ConcernRules.problemWith("   "))
        assertNull(ConcernRules.problemWith("The kitchen door does not lock."))
    }

    @Test
    fun `a concern can't be longer than the server allows`() {
        assertEquals(R.string.concern_error_too_long, ConcernRules.problemWith("a".repeat(ConcernRules.MAX_LENGTH + 1)))
        assertNull(ConcernRules.problemWith("a".repeat(ConcernRules.MAX_LENGTH)))
    }

    @Test
    fun `feedback needs a rating from one to five`() {
        assertEquals(R.string.feedback_error_rating, FeedbackRules.problemWith(0, ""))
        assertEquals(R.string.feedback_error_rating, FeedbackRules.problemWith(6, ""))
        assertNull(FeedbackRules.problemWith(5, ""))
    }

    @Test
    fun `a feedback comment has a length limit`() {
        assertEquals(R.string.feedback_error_too_long, FeedbackRules.problemWith(4, "a".repeat(FeedbackRules.MAX_COMMENT + 1)))
    }

    @Test
    fun `the admin sees the average to one decimal and only the comments with words`() {
        val summary = FeedbackSummary.of(
            listOf(
                ActivityFeedback(rating = 5, comment = "Well organised"),
                ActivityFeedback(rating = 4, comment = ""),
                ActivityFeedback(rating = 4, comment = "  ")
            )
        )
        assertEquals(3, summary.count)
        assertEquals(4.3, summary.average, 0.0001)
        assertEquals(listOf("Well organised"), summary.comments.map { it.comment })
    }

    @Test
    fun `no feedback yet averages nothing`() {
        val summary = FeedbackSummary.of(emptyList())
        assertEquals(0, summary.count)
        assertEquals(0.0, summary.average, 0.0)
    }

    @Test
    fun `an emergency contact needs a name and a phone number that works`() {
        assertNull(EmergencyContactRules.problemWith("Thandi", "082 123 4567"))
        assertEquals(R.string.emergency_contact_error_name, EmergencyContactRules.problemWith("", "082 123 4567"))
        assertEquals(R.string.error_invalid_phone, EmergencyContactRules.problemWith("Thandi", "12345"))
    }

    @Test
    fun `clearing both fields removes the emergency contact`() {
        assertNull(EmergencyContactRules.problemWith("", "  "))
    }
}
