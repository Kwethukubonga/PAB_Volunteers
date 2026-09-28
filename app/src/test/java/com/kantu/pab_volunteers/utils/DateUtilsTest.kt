package com.kantu.pab_volunteers.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * The date and time rules decide when an activity moves to Completed and how many hours
 * a volunteer is credited, so they are worth pinning down.
 */
class DateUtilsTest {

    private fun dayAt(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 13, 45, 30)
        calendar.set(Calendar.MILLISECOND, 500)
        return calendar.timeInMillis
    }

    // ---- minutesOfDay -------------------------------------------------

    @Test
    fun `minutesOfDay reads a normal time`() {
        assertEquals(0, DateUtils.minutesOfDay("00:00"))
        assertEquals(9 * 60 + 30, DateUtils.minutesOfDay("09:30"))
        assertEquals(23 * 60 + 59, DateUtils.minutesOfDay("23:59"))
    }

    @Test
    fun `minutesOfDay ignores surrounding spaces`() {
        assertEquals(8 * 60, DateUtils.minutesOfDay("  08:00  "))
    }

    @Test
    fun `minutesOfDay refuses anything that is not a time`() {
        assertNull(DateUtils.minutesOfDay(""))
        assertNull(DateUtils.minutesOfDay("noon"))
        assertNull(DateUtils.minutesOfDay("12"))
        assertNull(DateUtils.minutesOfDay("12:00:00"))
        assertNull(DateUtils.minutesOfDay("ab:cd"))
    }

    @Test
    fun `minutesOfDay refuses hours and minutes out of range`() {
        assertNull(DateUtils.minutesOfDay("24:00"))
        assertNull(DateUtils.minutesOfDay("-1:00"))
        assertNull(DateUtils.minutesOfDay("10:60"))
    }

    // ---- lengthInMinutes ----------------------------------------------

    @Test
    fun `lengthInMinutes measures a normal slot`() {
        assertEquals(240, DateUtils.lengthInMinutes("12:00", "16:00"))
        assertEquals(90, DateUtils.lengthInMinutes("09:00", "10:30"))
    }

    @Test
    fun `lengthInMinutes is zero when the end is not after the start`() {
        // This is the real case that showed up: 4pm entered as 04:00.
        assertEquals(0, DateUtils.lengthInMinutes("12:00", "04:00"))
        assertEquals(0, DateUtils.lengthInMinutes("10:00", "10:00"))
    }

    @Test
    fun `lengthInMinutes is zero when either time is unreadable`() {
        assertEquals(0, DateUtils.lengthInMinutes("", "16:00"))
        assertEquals(0, DateUtils.lengthInMinutes("12:00", "later"))
    }

    // ---- endOfActivity ------------------------------------------------

    @Test
    fun `endOfActivity lands on the end time of that day`() {
        val date = dayAt(2026, Calendar.SEPTEMBER, 27)
        val end = DateUtils.endOfActivity(date, "12:00", "16:00")
        assertEquals(DateUtils.startOfDay(date) + 16 * 60 * 60 * 1000L, end)
    }

    @Test
    fun `endOfActivity falls back to the end of the day when the times disagree`() {
        val date = dayAt(2026, Calendar.SEPTEMBER, 27)
        val end = DateUtils.endOfActivity(date, "12:00", "04:00")
        // Not 4am, or the activity would look finished before it started.
        assertEquals(DateUtils.startOfDay(date) + DateUtils.DAY_MILLIS, end)
    }

    @Test
    fun `endOfActivity falls back to the end of the day when a time is missing`() {
        val date = dayAt(2026, Calendar.SEPTEMBER, 27)
        assertEquals(
            DateUtils.startOfDay(date) + DateUtils.DAY_MILLIS,
            DateUtils.endOfActivity(date, "", "")
        )
    }

    @Test
    fun `an activity earlier today has already finished`() {
        val today = DateUtils.now()
        val end = DateUtils.endOfActivity(today, "00:01", "00:02")
        assertTrue("a slot that ran at midnight should be over by now", end <= today)
    }

    // ---- startOfDay ---------------------------------------------------

    @Test
    fun `startOfDay strips the time`() {
        val midday = dayAt(2026, Calendar.SEPTEMBER, 27)
        val start = DateUtils.startOfDay(midday)
        val calendar = Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `startOfDay is steady for any moment in the same day`() {
        val morning = dayAt(2026, Calendar.SEPTEMBER, 27)
        val later = morning + 6 * 60 * 60 * 1000L
        assertEquals(DateUtils.startOfDay(morning), DateUtils.startOfDay(later))
    }

    // ---- volunteerIdFor -----------------------------------------------

    @Test
    fun `volunteer id is built from the account id`() {
        val id = DateUtils.volunteerIdFor("abc123def456")
        assertTrue(id.startsWith("VOL-"))
        assertTrue(id.endsWith("ABC123"))
    }

    @Test
    fun `the same account always gets the same volunteer id`() {
        assertEquals(DateUtils.volunteerIdFor("zzz999"), DateUtils.volunteerIdFor("zzz999"))
    }

    @Test
    fun `different accounts get different volunteer ids`() {
        val first = DateUtils.volunteerIdFor("aaaaaa111")
        val second = DateUtils.volunteerIdFor("bbbbbb222")
        assertTrue(first != second)
    }

    @Test
    fun `volunteer id drops punctuation from the account id`() {
        assertTrue(DateUtils.volunteerIdFor("a-b_c.d!e").endsWith("ABCDE"))
    }

    // ---- monthsSince --------------------------------------------------

    @Test
    fun `monthsSince is zero for someone who joined today`() {
        assertEquals(0, DateUtils.monthsSince(DateUtils.now()))
    }

    @Test
    fun `monthsSince counts whole months`() {
        val ninetyDaysAgo = DateUtils.now() - 90 * DateUtils.DAY_MILLIS
        assertEquals(3, DateUtils.monthsSince(ninetyDaysAgo))
    }

    @Test
    fun `monthsSince is zero when the join date is missing or in the future`() {
        assertEquals(0, DateUtils.monthsSince(0L))
        assertEquals(0, DateUtils.monthsSince(DateUtils.now() + DateUtils.DAY_MILLIS))
    }

    // ---- formatDate ---------------------------------------------------

    @Test
    fun `formatDate gives nothing for a missing date`() {
        assertEquals("", DateUtils.formatDate(0L))
        assertEquals("", DateUtils.formatDate(-1L))
    }
}
