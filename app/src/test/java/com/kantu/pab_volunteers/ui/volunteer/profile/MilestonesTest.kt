package com.kantu.pab_volunteers.ui.volunteer.profile

import com.kantu.pab_volunteers.data.model.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The milestones on the Profile screen, worked out from finished activities. */
class MilestonesTest {

    private fun done(programme: String = "Community Feeding", start: String = "09:00", end: String = "12:00") =
        Activity(programme = programme, startTime = start, endTime = end)

    private fun List<Milestone>.of(kind: Milestone.Kind) = first { it.kind == kind }

    @Test
    fun `a new volunteer has earned nothing yet`() {
        val milestones = Milestone.from(emptyList())

        assertEquals(4, milestones.size)
        assertTrue(milestones.none { it.earned })
    }

    @Test
    fun `the first finished activity earns the first shift`() {
        assertTrue(Milestone.from(listOf(done())).of(Milestone.Kind.FIRST_SHIFT).earned)
    }

    @Test
    fun `ten activities shows progress until the tenth`() {
        val nine = Milestone.from(List(9) { done() }).of(Milestone.Kind.TEN_ACTIVITIES)
        assertFalse(nine.earned)
        assertEquals(9, nine.progress)
        assertTrue(Milestone.from(List(10) { done() }).of(Milestone.Kind.TEN_ACTIVITIES).earned)
    }

    @Test
    fun `hours add up across activities`() {
        // Eight three-hour activities make 24 hours, one more makes 27.
        assertFalse(Milestone.from(List(8) { done() }).of(Milestone.Kind.TWENTY_FIVE_HOURS).earned)
        assertTrue(Milestone.from(List(9) { done() }).of(Milestone.Kind.TWENTY_FIVE_HOURS).earned)
    }

    @Test
    fun `three different programmes are needed, whatever the case`() {
        val two = listOf(done("Community Feeding"), done("community feeding"), done("Youth Programme"))
        assertEquals(2, Milestone.from(two).of(Milestone.Kind.THREE_PROGRAMMES).progress)
        val three = two + done("Baby Packs")
        assertTrue(Milestone.from(three).of(Milestone.Kind.THREE_PROGRAMMES).earned)
    }

    @Test
    fun `progress never shows more than the target`() {
        assertEquals(1, Milestone.from(List(5) { done() }).of(Milestone.Kind.FIRST_SHIFT).progress)
    }
}
