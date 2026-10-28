package com.kantu.pab_volunteers.ui.intro

import com.kantu.pab_volunteers.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which button belongs to which page of the tour shown after signing up (K8). */
class IntroPagesTest {

    private val last = IntroPages.last

    @Test
    fun `a welcome page and one for each of the four tabs`() {
        assertEquals(5, IntroPages.pages.size)
    }

    @Test
    fun `no page repeats another page's words or picture`() {
        assertEquals(IntroPages.pages.size, IntroPages.pages.map { it.title }.toSet().size)
        assertEquals(IntroPages.pages.size, IntroPages.pages.map { it.body }.toSet().size)
        assertEquals(IntroPages.pages.size, IntroPages.pages.map { it.photo }.toSet().size)
    }

    @Test
    fun `only the final page is the last one`() {
        (0 until last).forEach { assertFalse("page $it", IntroPages.isLast(it)) }
        assertTrue(IntroPages.isLast(last))
    }

    @Test
    fun `the forward button says Next until the end, then Get started`() {
        (0 until last).forEach {
            assertEquals("page $it", R.string.intro_next, IntroPages.forwardLabel(it))
        }
        assertEquals(R.string.intro_get_started, IntroPages.forwardLabel(last))
    }

    @Test
    fun `Skip is there the whole way but not on the last page, where it would do nothing new`() {
        (0 until last).forEach { assertTrue("page $it", IntroPages.showSkip(it)) }
        assertFalse(IntroPages.showSkip(last))
    }

    @Test
    fun `next moves on one page and stops at the end`() {
        (0 until last).forEach { assertEquals(it + 1, IntroPages.pageAfter(it)) }
        assertEquals(last, IntroPages.pageAfter(last))
    }

    @Test
    fun `a page beyond the last is still treated as the last`() {
        assertTrue(IntroPages.isLast(99))
        assertFalse(IntroPages.showSkip(99))
        assertEquals(R.string.intro_get_started, IntroPages.forwardLabel(99))
        assertEquals(last, IntroPages.pageAfter(99))
    }
}
