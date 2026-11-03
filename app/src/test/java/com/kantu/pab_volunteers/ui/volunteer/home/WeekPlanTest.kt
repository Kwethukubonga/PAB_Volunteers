package com.kantu.pab_volunteers.ui.volunteer.home

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Home planner (K9): the coming week, the next activity and the plan for a day. */
class WeekPlanTest {

    private val now = DateUtils.startOfDay(DateUtils.now()) + 10L * 60 * 60 * 1000 // 10:00 today
    private val today = DateUtils.startOfDay(now)

    private fun activity(id: String, dayOffset: Int, start: String, end: String = "23:00") = Activity(
        id = id, title = id, dateMillis = DateUtils.addDays(today, dayOffset) + 5_000, startTime = start, endTime = end
    )

    @Test
    fun `the strip is seven days starting today`() {
        val days = WeekPlan.days(now)
        assertEquals(7, days.size)
        assertEquals(today, days.first())
        assertEquals(DateUtils.addDays(today, 6), days.last())
    }

    @Test
    fun `days with something booked get a dot`() {
        val booked = listOf(activity("a", 0, "12:00"), activity("b", 3, "09:00"))
        val dots = WeekPlan.daysWithPlans(WeekPlan.days(now), booked)
        assertEquals(setOf(today, DateUtils.addDays(today, 3)), dots)
    }

    @Test
    fun `a day's plan is earliest first`() {
        val booked = listOf(activity("late", 0, "15:00"), activity("early", 0, "11:00"), activity("other day", 1, "08:00"))
        assertEquals(listOf("early", "late"), WeekPlan.plansOn(today, booked).map { it.id })
    }

    @Test
    fun `the next activity is the soonest that hasn't finished`() {
        val booked = listOf(
            activity("done this morning", 0, "07:00", "09:00"),
            activity("tomorrow", 1, "09:00"),
            activity("this afternoon", 0, "14:00")
        )
        assertEquals("this afternoon", WeekPlan.next(booked, now)?.id)
    }

    @Test
    fun `nothing booked means no next activity`() {
        assertNull(WeekPlan.next(emptyList(), now))
        assertTrue(WeekPlan.daysWithPlans(WeekPlan.days(now), emptyList()).isEmpty())
    }
}
