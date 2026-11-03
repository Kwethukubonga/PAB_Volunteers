package com.kantu.pab_volunteers.notifications

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** What the background check tells volunteers and admins about (PV-FR10 and PV-FR17). */
class UpdateRulesTest {

    private val now = DateUtils.now()
    private val day = 24L * 60 * 60 * 1000
    private val startedAt = now - day

    private fun activity(
        id: String,
        publishedAt: Long = now,
        dateMillis: Long = now + day,
        total: Int = 10,
        filled: Int = 0,
        status: String = Activity.STATUS_PUBLISHED
    ) = Activity(
        id = id,
        title = "Community Kitchen",
        dateMillis = dateMillis,
        startTime = "09:00",
        endTime = "12:00",
        totalSpots = total,
        filledSpots = filled,
        status = status,
        publishedAt = publishedAt
    )

    private fun announcement(id: String, publishedAt: Long) =
        Announcement(id = id, title = "Food parcels on Friday", publishedAt = publishedAt)

    @Test
    fun `an activity published after checks began is news`() {
        val fresh = UpdateRules.freshActivities(listOf(activity("new")), startedAt, now)

        assertEquals(listOf("new"), fresh.map { it.id })
    }

    @Test
    fun `an activity published before checks began is not news`() {
        val old = activity("old", publishedAt = startedAt - 1)

        assertTrue(UpdateRules.freshActivities(listOf(old), startedAt, now).isEmpty())
    }

    @Test
    fun `an activity that has already finished is not news`() {
        val finished = activity("done", dateMillis = now - 2 * day)

        assertTrue(UpdateRules.freshActivities(listOf(finished), startedAt, now).isEmpty())
    }

    @Test
    fun `only announcements published after checks began are news`() {
        val fresh = UpdateRules.freshAnnouncements(
            listOf(announcement("new", now), announcement("old", startedAt - 1)), startedAt
        )

        assertEquals(listOf("new"), fresh.map { it.id })
    }

    @Test
    fun `something already shown is not shown again`() {
        val items = listOf(activity("seen"), activity("unseen"))

        val toShow = UpdateRules.notShownYet(items, setOf("seen")) { it.id }

        assertEquals(listOf("unseen"), toShow.map { it.id })
    }

    @Test
    fun `an activity with every spot taken counts as full`() {
        val full = UpdateRules.fullActivities(listOf(activity("a", total = 5, filled = 5)), now)

        assertEquals(listOf("a"), full.map { it.id })
    }

    @Test
    fun `an activity with spots left is not full`() {
        assertTrue(UpdateRules.fullActivities(listOf(activity("a", total = 5, filled = 4)), now).isEmpty())
    }

    @Test
    fun `a draft is never reported as full`() {
        val draft = activity("a", total = 5, filled = 5, status = Activity.STATUS_DRAFT)

        assertTrue(UpdateRules.fullActivities(listOf(draft), now).isEmpty())
    }

    @Test
    fun `an activity with no spots set is not reported as full`() {
        assertTrue(UpdateRules.fullActivities(listOf(activity("a", total = 0, filled = 0)), now).isEmpty())
    }

    @Test
    fun `a full activity that has finished is not reported`() {
        val finished = activity("a", total = 5, filled = 5, dateMillis = now - 2 * day)

        assertTrue(UpdateRules.fullActivities(listOf(finished), now).isEmpty())
    }

    // "Only my programmes": which new activities a volunteer hears about (I2).

    private fun inProgramme(id: String, programme: String) =
        activity(id).copy(programme = programme)

    @Test
    fun `only activities in the programmes the volunteer chose`() {
        val all = listOf(
            inProgramme("a", "Senior Programme"),
            inProgramme("b", "Youth Programme"),
            inProgramme("c", "Soup Kitchen")
        )

        val mine = UpdateRules.inChosenProgrammes(all, listOf("Senior Programme", "Soup Kitchen"))

        assertEquals(listOf("a", "c"), mine.map { it.id })
    }

    @Test
    fun `a volunteer who chose no programmes still hears about everything`() {
        val all = listOf(inProgramme("a", "Senior Programme"), inProgramme("b", "Youth Programme"))

        assertEquals(2, UpdateRules.inChosenProgrammes(all, emptyList()).size)
        assertEquals(2, UpdateRules.inChosenProgrammes(all, listOf("", "   ")).size)
    }

    @Test
    fun `the programme is matched whatever the case and spacing`() {
        val all = listOf(inProgramme("a", "  senior programme  "))

        assertEquals(1, UpdateRules.inChosenProgrammes(all, listOf("Senior Programme")).size)
    }

    @Test
    fun `an activity with no programme is left out once a choice has been made`() {
        val all = listOf(inProgramme("a", ""), inProgramme("b", "Senior Programme"))

        assertEquals(listOf("b"), UpdateRules.inChosenProgrammes(all, listOf("Senior Programme")).map { it.id })
    }
}
