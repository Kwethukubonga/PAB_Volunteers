package com.kantu.pab_volunteers.utils

import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.User
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The search boxes on Activities and on the admin's Volunteers list. */
class SearchTest {

    private val kitchen = Activity(
        title = "Community Soup Kitchen",
        programme = "Community Feeding",
        location = "Philisa Centre, Lavender Hill",
        volunteerRole = "Kitchen helper"
    )

    private val nomsa = User(
        firstName = "Nomsa",
        lastName = "Dlamini",
        email = "nomsa@example.com",
        phone = "082 123 4567",
        area = "Steenberg",
        volunteerId = "VOL-2026-ABC123",
        programmeInterests = listOf("Youth Programme")
    )

    @Test
    fun `an empty search shows everything`() {
        assertTrue(Search.matches(kitchen, ""))
        assertTrue(Search.matches(kitchen, "   "))
    }

    @Test
    fun `any case and part of a word matches`() {
        assertTrue(Search.matches(kitchen, "SOUP"))
        assertTrue(Search.matches(kitchen, "lav"))
    }

    @Test
    fun `every word typed has to match, in any order`() {
        assertTrue(Search.matches(kitchen, "lavender soup"))
        assertFalse(Search.matches(kitchen, "soup baby"))
    }

    @Test
    fun `an activity is found by its programme, place or role`() {
        assertTrue(Search.matches(kitchen, "feeding"))
        assertTrue(Search.matches(kitchen, "philisa centre"))
        assertTrue(Search.matches(kitchen, "helper"))
    }

    @Test
    fun `a volunteer is found by name, area, ID or interest`() {
        assertTrue(Search.matches(nomsa, "nomsa dlamini"))
        assertTrue(Search.matches(nomsa, "steenberg"))
        assertTrue(Search.matches(nomsa, "abc123"))
        assertTrue(Search.matches(nomsa, "youth"))
    }

    @Test
    fun `a volunteer who doesn't match is left out`() {
        assertFalse(Search.matches(nomsa, "thabo"))
    }
}
