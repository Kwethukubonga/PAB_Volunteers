package com.kantu.pab_volunteers

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.ui.volunteer.profile.Milestone
import com.kantu.pab_volunteers.utils.PhoneNumber
import com.kantu.pab_volunteers.utils.Search
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the app does when it is given something unreasonable: a name longer than any
 * real name, emoji where letters were expected, a list far bigger than PAB will ever
 * have, or nothing at all. None of this should throw; the screens that call these
 * helpers have no way to recover from an exception part-way through drawing a list.
 */
class EdgeCasesTest {

    private fun activity(title: String = "", programme: String = "", location: String = "") =
        Activity(title = title, programme = programme, location = location)

    // Searching the lists.

    @Test
    fun `search finds one activity among hundreds`() {
        val many = (1..250).map { activity(title = "Soup kitchen $it", location = "Lavender Hill") }
        val hits = many.filter { Search.matches(it, "soup kitchen 207") }

        assertEquals(1, hits.size)
        assertEquals("Soup kitchen 207", hits.first().title)
    }

    @Test
    fun `search copes with a title far longer than anyone would type`() {
        val huge = "Soup kitchen " + "very ".repeat(1_000) + "long"

        assertTrue(Search.matches(activity(title = huge), "soup kitchen"))
        assertTrue(Search.matches(activity(title = huge), "long"))
        assertFalse(Search.matches(activity(title = huge), "knitting"))
    }

    @Test
    fun `search copes with a query far longer than any title`() {
        val soup = activity(title = "Soup kitchen")

        // Repeating a word that does match changes nothing: every word still has to appear.
        assertTrue(Search.matches(soup, "soup " + "kitchen ".repeat(500)))
        // One word that does not appear is enough to rule it out, however long the rest is.
        assertFalse(Search.matches(soup, "soup " + "kitchen ".repeat(500) + "knitting"))
    }

    @Test
    fun `emoji in a name neither match nor break the search`() {
        val volunteer = User(firstName = "Nomsa 🌸", lastName = "Dlamini", area = "Steenberg")

        assertTrue(Search.matches(volunteer, "nomsa"))
        assertTrue(Search.matches(volunteer, "🌸"))
        assertFalse(Search.matches(volunteer, "thandi"))
    }

    @Test
    fun `a query of only spaces shows everything, like an empty one`() {
        assertTrue(Search.matches(activity(title = "Soup kitchen"), "   "))
        assertTrue(Search.matches(activity(title = "Soup kitchen"), "\t\n "))
    }

    @Test
    fun `searching an activity with every field empty matches nothing but an empty query`() {
        assertTrue(Search.matches(activity(), ""))
        assertFalse(Search.matches(activity(), "soup"))
    }

    // Phone numbers, where people paste all sorts of things.

    @Test
    fun `a very long run of digits is refused rather than measured`() {
        assertFalse(PhoneNumber.isValid("0" + "8".repeat(200)))
        assertFalse(PhoneNumber.isValid("+27" + "8".repeat(200)))
    }

    @Test
    fun `emoji and letters in a phone field are refused`() {
        listOf("082 123 456📱", "08two1234567", "☎️", "+27 82 123 45six")
            .forEach { assertFalse(it, PhoneNumber.isValid(it)) }
    }

    @Test
    fun `formatting leaves something unreadable alone instead of mangling it`() {
        assertEquals("not a number", PhoneNumber.formatAsTyped("not a number"))
        assertEquals("", PhoneNumber.formatAsTyped(""))
    }

    @Test
    fun `tidy collapses tabs and new lines, not just spaces`() {
        assertEquals("082 123 4567", PhoneNumber.tidy("  082\t123\n4567  "))
    }

    @Test
    fun `the cursor stays inside the text when more digits are asked for than there are`() {
        val formatted = "082 1"
        assertEquals(formatted.length, PhoneNumber.cursorAfterDigits(formatted, 99))
        assertEquals(0, PhoneNumber.cursorAfterDigits("", 0))
    }

    // Milestones, which add up a volunteer's whole history.

    @Test
    fun `a volunteer who has finished nothing has earned nothing`() {
        val milestones = Milestone.from(emptyList())

        assertEquals(4, milestones.size)
        assertTrue(milestones.none { it.earned })
        assertTrue(milestones.all { it.progress == 0 })
    }

    @Test
    fun `hundreds of finished activities stop at the target rather than running past it`() {
        val many = (1..500).map {
            Activity(programme = "Programme $it", startTime = "09:00", endTime = "12:00")
        }

        Milestone.from(many).forEach {
            assertTrue(it.kind.name, it.earned)
            assertEquals(it.kind.name, it.kind.target, it.progress)
        }
    }

    @Test
    fun `the same programme typed differently still counts once`() {
        val finished = listOf(
            activity(programme = "Senior Programme"),
            activity(programme = "senior programme"),
            activity(programme = "  SENIOR PROGRAMME  "),
            activity(programme = "")
        )

        val programmes = Milestone.from(finished).first { it.kind == Milestone.Kind.THREE_PROGRAMMES }
        assertEquals(1, programmes.progress)
    }

    // Names, which are shown on every list row.

    @Test
    fun `a volunteer with only a first name has no trailing space in their name`() {
        assertEquals("Nomsa", User(firstName = "Nomsa").fullName)
        assertEquals("Dlamini", User(lastName = "Dlamini").fullName)
        assertEquals("", User().fullName)
    }
}
