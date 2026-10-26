package com.kantu.pab_volunteers.ui.intro

import com.kantu.pab_volunteers.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which button belongs to which page of the first-run introduction (K8). */
class IntroPagesTest {

    @Test
    fun `there are three pages, each with its own words`() {
        assertEquals(3, IntroPages.pages.size)
        assertEquals(3, IntroPages.pages.map { it.title }.toSet().size)
        assertEquals(3, IntroPages.pages.map { it.body }.toSet().size)
    }

    @Test
    fun `the last page is the one that finishes`() {
        assertFalse(IntroPages.isLast(0))
        assertFalse(IntroPages.isLast(1))
        assertTrue(IntroPages.isLast(2))
    }

    @Test
    fun `the forward button says Next until the end, then Get started`() {
        assertEquals(R.string.intro_next, IntroPages.forwardLabel(0))
        assertEquals(R.string.intro_next, IntroPages.forwardLabel(1))
        assertEquals(R.string.intro_get_started, IntroPages.forwardLabel(2))
        assertNotEquals(IntroPages.forwardLabel(0), IntroPages.forwardLabel(2))
    }

    @Test
    fun `Skip disappears on the last page, where it would do nothing new`() {
        assertTrue(IntroPages.showSkip(0))
        assertTrue(IntroPages.showSkip(1))
        assertFalse(IntroPages.showSkip(2))
    }

    @Test
    fun `next moves on one page and stops at the end`() {
        assertEquals(1, IntroPages.pageAfter(0))
        assertEquals(2, IntroPages.pageAfter(1))
        assertEquals(2, IntroPages.pageAfter(2))
    }

    @Test
    fun `a page beyond the last is still treated as the last`() {
        assertTrue(IntroPages.isLast(99))
        assertFalse(IntroPages.showSkip(99))
        assertEquals(R.string.intro_get_started, IntroPages.forwardLabel(99))
    }
}
