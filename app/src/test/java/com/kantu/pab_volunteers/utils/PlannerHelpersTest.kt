package com.kantu.pab_volunteers.utils

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.notifications.ReminderLead
import com.kantu.pab_volunteers.notifications.ReminderRules
import com.kantu.pab_volunteers.security.AppLockRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Dates for the planner and calendar, avatar colours, reminders and the app lock. */
class PlannerHelpersTest {

    private val hour = 60L * 60 * 1000
    private val today = DateUtils.startOfDay(DateUtils.now())

    @Test
    fun `an activity starts at its start time on its day`() {
        assertEquals(today + 9 * hour + 30 * 60 * 1000, DateUtils.startOfActivity(today + 5_000, "09:30"))
    }

    @Test
    fun `an unreadable start time counts from the start of the day`() {
        assertEquals(today, DateUtils.startOfActivity(today, "soon"))
    }

    @Test
    fun `days between counts calendar days, whatever the time`() {
        assertEquals(0, DateUtils.daysBetween(today + 1, today + 23 * hour))
        assertEquals(1, DateUtils.daysBetween(today + 23 * hour, DateUtils.addDays(today, 1) + 1))
        assertEquals(-2, DateUtils.daysBetween(today, DateUtils.addDays(today, -2)))
        assertEquals(3, RelativeDay.daysFrom(today, DateUtils.addDays(today, 3) + hour))
    }

    @Test
    fun `the same person always gets the same colour`() {
        assertEquals(AvatarColors.forId("volunteer_uid"), AvatarColors.forId("volunteer_uid"))
        assertTrue(AvatarColors.forId("volunteer_uid") in AvatarColors.PALETTE)
    }

    @Test
    fun `every avatar colour keeps white text readable`() {
        AvatarColors.PALETTE.forEach { colour ->
            val ratio = (1.0 + 0.05) / (luminance(colour) + 0.05)
            assertTrue("colour ${Integer.toHexString(colour)} gives $ratio", ratio >= 4.5)
        }
    }

    private fun luminance(colour: Int): Double {
        fun channel(value: Int): Double {
            val c = value / 255.0
            return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel((colour shr 16) and 0xFF) + 0.7152 * channel((colour shr 8) and 0xFF) +
            0.0722 * channel(colour and 0xFF)
    }

    private fun joined(id: String, startsInHours: Long): Activity {
        val start = DateUtils.now() + startsInHours * hour
        val minutes = ((start - DateUtils.startOfDay(start)) / 60_000).toInt()
        return Activity(
            id = id, dateMillis = DateUtils.startOfDay(start),
            startTime = "%02d:%02d".format(minutes / 60, minutes % 60), endTime = "23:59"
        )
    }

    @Test
    fun `a reminder is due inside the chosen time before the start`() {
        val now = DateUtils.now()
        val dayAway = listOf(joined("in 20 hours", 20))
        // Twenty hours away is inside "a day before" but not yet inside "an hour before".
        assertEquals(listOf("in 20 hours"), ReminderRules.due(dayAway, now, ReminderLead.DAY, emptySet()).map { it.id })
        assertTrue(ReminderRules.due(dayAway, now, ReminderLead.HOUR, emptySet()).isEmpty())
    }

    @Test
    fun `no reminder when they're off, already sent, or the activity has started`() {
        val now = DateUtils.now()
        val activity = joined("a", 20)
        assertTrue(ReminderRules.due(listOf(activity), now, ReminderLead.OFF, emptySet()).isEmpty())
        assertTrue(ReminderRules.due(listOf(activity), now, ReminderLead.DAY, setOf("a")).isEmpty())
        assertTrue(ReminderRules.due(listOf(activity), activity.startsAtMillis + 1, ReminderLead.DAY, emptySet()).isEmpty())
    }

    @Test
    fun `the lock comes back only after the grace period away`() {
        val away = 1_000_000L
        assertFalse(AppLockRules.shouldLock(true, away, away + AppLockRules.GRACE_MILLIS - 1))
        assertTrue(AppLockRules.shouldLock(true, away, away + AppLockRules.GRACE_MILLIS))
    }

    @Test
    fun `the lock never comes back when it is off or the app hasn't been away`() {
        assertFalse(AppLockRules.shouldLock(false, 1L, Long.MAX_VALUE))
        assertFalse(AppLockRules.shouldLock(true, 0L, Long.MAX_VALUE))
    }
}
