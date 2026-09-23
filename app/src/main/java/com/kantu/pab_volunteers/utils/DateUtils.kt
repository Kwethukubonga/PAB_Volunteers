package com.kantu.pab_volunteers.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object DateUtils {

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
