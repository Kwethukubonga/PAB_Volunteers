package com.kantu.pab_volunteers.ui.volunteer.home

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.utils.DateUtils

/**
 * The Home screen as a planner (K9): the next activity, a strip of the coming week with a dot on
 * each day something is booked, and the plan for the day picked in the strip.
 */
object WeekPlan {

    const val DAYS = 7

    /** Midnight of today and the six days after it. */
    fun days(now: Long): List<Long> = (0 until DAYS).map { DateUtils.addDays(now, it) }

    /** What is booked on [day], earliest first. */
    fun plansOn(day: Long, booked: List<Activity>): List<Activity> {
        val start = DateUtils.startOfDay(day)
        return booked.filter { DateUtils.startOfDay(it.dateMillis) == start }.sortedBy { it.startsAtMillis }
    }

    /** The days in [days] that have something booked, for the dots in the strip. */
    fun daysWithPlans(days: List<Long>, booked: List<Activity>): Set<Long> {
        val bookedDays = booked.map { DateUtils.startOfDay(it.dateMillis) }.toSet()
        return days.filter { DateUtils.startOfDay(it) in bookedDays }.toSet()
    }

    /** The next activity that hasn't finished yet, or null when nothing is booked. */
    fun next(booked: List<Activity>, now: Long): Activity? =
        booked.filter { it.endsAtMillis > now }.minByOrNull { it.startsAtMillis }
}
