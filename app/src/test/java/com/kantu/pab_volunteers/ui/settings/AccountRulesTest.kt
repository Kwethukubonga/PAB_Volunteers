package com.kantu.pab_volunteers.ui.settings

import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The decisions behind changing a password and deleting an account from Settings (PV-FR11). */
class AccountRulesTest {

    private val now = DateUtils.now()
    private val day = 24L * 60 * 60 * 1000

    private fun signup(dateMillis: Long, start: String = "09:00", end: String = "12:00") =
        ActivitySignup(activityId = "a$dateMillis", dateMillis = dateMillis, startTime = start, endTime = end)

    @Test
    fun `an email account signs in with a password`() {
        assertEquals(SignInMethod.PASSWORD, SignInMethod.from(listOf("firebase", "password")))
    }

    @Test
    fun `a Google account signs in with Google`() {
        assertEquals(SignInMethod.GOOGLE, SignInMethod.from(listOf("firebase", "google.com")))
    }

    @Test
    fun `an account with both counts as a password account`() {
        assertEquals(SignInMethod.PASSWORD, SignInMethod.from(listOf("google.com", "password")))
    }

    @Test
    fun `finished activities count as completed and their hours add up`() {
        val summary = AccountSummary.from(
            listOf(signup(now - 3 * day), signup(now - 2 * day, "09:00", "10:30")),
            now
        )

        assertEquals(2, summary.completed)
        assertEquals(4, summary.hours) // 3 hours and 1.5 hours make 4 whole hours
        assertEquals(0, summary.upcoming)
    }

    @Test
    fun `activities that haven't finished count as upcoming places`() {
        val summary = AccountSummary.from(listOf(signup(now + day), signup(now + 2 * day)), now)

        assertEquals(2, summary.upcoming)
        assertEquals(0, summary.completed)
        assertEquals(0, summary.hours)
    }

    @Test
    fun `someone with no sign-ups loses nothing but their profile`() {
        assertEquals(AccountSummary(0, 0, 0), AccountSummary.from(emptyList(), now))
    }

    @Test
    fun `the confirmation word matches whatever the case and spaces`() {
        assertTrue(DeleteConfirmation.matches(" delete ", "DELETE"))
    }

    @Test
    fun `anything else does not confirm`() {
        assertFalse(DeleteConfirmation.matches("DELET", "DELETE"))
        assertFalse(DeleteConfirmation.matches("", "DELETE"))
    }

    @Test
    fun `a missing confirmation word never confirms`() {
        assertFalse(DeleteConfirmation.matches("", ""))
    }
}
