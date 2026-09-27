package com.kantu.pab_volunteers.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object DateUtils {

    const val DAY_MILLIS = 24L * 60 * 60 * 1000
    private const val MINUTE_MILLIS = 60L * 1000

    private val dayMonthYear = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    private val dayMonthYearTime = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    private val fullDay = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    fun now(): Long = System.currentTimeMillis()

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return dayMonthYear.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return dayMonthYearTime.format(Date(timestamp))
    }

    fun formatFullDay(timestamp: Long): String = fullDay.format(Date(timestamp))

    // Midnight of the day containing this timestamp.
    fun startOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** Minutes past midnight for a "HH:mm" time, or null when it cannot be read. */
    fun minutesOfDay(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null
        return hour * 60 + minute
    }

    /** How long an activity runs, or zero when the two times do not make sense together. */
    fun lengthInMinutes(startTime: String, endTime: String): Int {
        val start = minutesOfDay(startTime) ?: return 0
        val end = minutesOfDay(endTime) ?: return 0
        return (end - start).coerceAtLeast(0)
    }

    /**
     * The moment an activity finishes, so a morning slot stops counting as upcoming by
     * the afternoon. Times that do not make sense fall back to the end of the day.
     */
    fun endOfActivity(dateMillis: Long, startTime: String, endTime: String): Long {
        val day = startOfDay(dateMillis)
        val end = minutesOfDay(endTime)
        if (end == null || lengthInMinutes(startTime, endTime) <= 0) return day + DAY_MILLIS
        return day + end * MINUTE_MILLIS
    }

    // Short volunteer ID, for example VOL-2026-084.
    fun generateVolunteerId(): String {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        return "VOL-$year-${Random.nextInt(100, 999)}"
    }

    fun monthsSince(joinedDate: Long): Int {
        if (joinedDate <= 0L) return 0
        val diffMillis = now() - joinedDate
        if (diffMillis <= 0) return 0
        val days = diffMillis / (1000L * 60 * 60 * 24)
        return (days / 30).toInt().coerceAtLeast(0)
    }
}
