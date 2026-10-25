package com.kantu.pab_volunteers.ui.volunteer.community

import com.kantu.pab_volunteers.data.model.Announcement
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** Community's month headings: grouping, order, and folding. */
class AnnouncementMonthsTest {

    private val sa = TimeZone.getTimeZone("Africa/Johannesburg")

    private fun on(year: Int, month: Int, day: Int, hour: Int = 10): Long =
        Calendar.getInstance(sa).apply { clear(); set(year, month - 1, day, hour, 0) }.timeInMillis

    private fun post(id: String, at: Long) = Announcement(id = id, title = id, date = at)

    private val october1 = post("oct-1", on(2026, 10, 3))
    private val october2 = post("oct-2", on(2026, 10, 20))
    private val september = post("sep", on(2026, 9, 12))

    @Test
    fun `each month gets a heading, newest month and newest post first`() {
        val rows = AnnouncementMonths.rows(listOf(september, october1, october2), emptySet(), sa)
        assertEquals(
            listOf("month:2026-10", "oct-2", "oct-1", "month:2026-09", "sep"),
            rows.map { if (it is CommunityRow.Month) "month:${it.key}" else (it as CommunityRow.Post).announcement.id }
        )
        assertEquals(2, (rows.first() as CommunityRow.Month).count)
    }

    @Test
    fun `a folded month keeps its heading and count but hides its posts`() {
        val rows = AnnouncementMonths.rows(listOf(september, october1, october2), setOf("2026-10"), sa)
        assertEquals(3, rows.size)
        val october = rows.first() as CommunityRow.Month
        assertEquals(true, october.collapsed)
        assertEquals(2, october.count)
    }

    @Test
    fun `only the newest month starts open`() {
        assertEquals(setOf("2026-09"), AnnouncementMonths.startCollapsed(listOf(october2, october1, september), sa))
    }

    @Test
    fun `just before midnight on the last day still counts as that month in South Africa`() {
        assertEquals("2026-10", AnnouncementMonths.keyFor(on(2026, 10, 31, hour = 23), sa))
    }

    @Test
    fun `a re-published announcement counts from when it was published again`() {
        val republished = post("old", on(2026, 8, 1)).copy(publishedAt = on(2026, 10, 25))
        assertEquals("2026-10", AnnouncementMonths.keyFor(AnnouncementMonths.postedAt(republished), sa))
    }
}
