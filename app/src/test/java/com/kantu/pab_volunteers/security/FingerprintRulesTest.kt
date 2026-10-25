package com.kantu.pab_volunteers.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Whose fingerprint sign-in it is, and what the Welcome screen calls them. */
class FingerprintRulesTest {

    @Test
    fun `it is set up for the account it was saved for, whatever the capitals`() {
        assertTrue(FingerprintRules.isSetUpFor("thandi@example.com", "Thandi@Example.com "))
    }

    @Test
    fun `someone else signing in on the same phone sees it off`() {
        assertFalse(FingerprintRules.isSetUpFor("thandi@example.com", "nomsa@example.com"))
    }

    @Test
    fun `nothing saved, or nobody signed in, is never set up`() {
        assertFalse(FingerprintRules.isSetUpFor(null, "thandi@example.com"))
        assertFalse(FingerprintRules.isSetUpFor("thandi@example.com", null))
        assertFalse(FingerprintRules.isSetUpFor("", ""))
    }

    @Test
    fun `the Welcome screen uses the first name, or the email without one`() {
        assertEquals("Thandi", FingerprintRules.nameToShow(FingerprintAccount("thandi@example.com", " Thandi ", usesGoogle = false)))
        assertEquals("thandi@example.com", FingerprintRules.nameToShow(FingerprintAccount("thandi@example.com", "", usesGoogle = true)))
    }
}
