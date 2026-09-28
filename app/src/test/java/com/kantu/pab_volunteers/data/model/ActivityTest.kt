package com.kantu.pab_volunteers.data.model

import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The rules that decide whether a volunteer can still take a place on an activity. */
class ActivityTest {

    private fun activity(
        total: Int = 10,
        filled: Int = 0,
        start: String = "09:00",
        end: String = "12:00",
        dateMillis: Long = DateUtils.now()
    ) = Activity(
        id = "act1",
        title = "Community Kitchen",
        programme = "Community Feeding",
        dateMillis = dateMillis,
        startTime = start,
        endTime = end,
        totalSpots = total,
        filledSpots = filled
    )

    @Test
    fun `spots remaining is what is left`() {
        assertEquals(7, activity(total = 10, filled = 3).spotsRemaining)
    }

    @Test
    fun `a full activity has no spots left`() {
        assertEquals(0, activity(total = 5, filled = 5).spotsRemaining)
    }

    @Test
    fun `spots remaining never goes below zero`() {
        // Over-filled data should read as full, not as a negative number on screen.
        assertEquals(0, activity(total = 5, filled = 8).spotsRemaining)
    }

    @Test
    fun `an activity with no spots set is full`() {
        assertEquals(0, activity(total = 0, filled = 0).spotsRemaining)
    }

    @Test
    fun `a later slot today has not finished yet`() {
        val endsLate = activity(start = "00:00", end = "23:59")
        assertTrue(endsLate.endsAtMillis > DateUtils.now())
    }

    @Test
    fun `yesterday has finished`() {
        val yesterday = activity(dateMillis = DateUtils.now() - DateUtils.DAY_MILLIS)
        assertTrue(yesterday.endsAtMillis <= DateUtils.now())
    }

    @Test
    fun `back to front times do not make an activity look finished`() {
        // 12:00 to 04:00 is a typo for 16:00, and must not retire the activity early.
        val today = DateUtils.startOfDay(DateUtils.now())
        val typo = activity(start = "12:00", end = "04:00", dateMillis = today)
        assertEquals(today + DateUtils.DAY_MILLIS, typo.endsAtMillis)
    }

    @Test
    fun `date and time label uses the stored text when there is no timestamp`() {
        val old = Activity(date = "2026-09-27", startTime = "09:00", endTime = "12:00")
        assertEquals("2026-09-27 · 09:00 - 12:00", old.dateTimeLabel)
    }

    @Test
    fun `a new activity starts as a draft`() {
        assertEquals(Activity.STATUS_DRAFT, Activity().status)
        assertFalse(Activity().status == Activity.STATUS_PUBLISHED)
    }
}
