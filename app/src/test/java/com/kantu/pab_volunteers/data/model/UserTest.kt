package com.kantu.pab_volunteers.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Who someone is, and what they are allowed to see. */
class UserTest {

    @Test
    fun `full name joins the two names`() {
        val user = User(firstName = "Nomsa", lastName = "Dlamini")
        assertEquals("Nomsa Dlamini", user.fullName)
    }

    @Test
    fun `full name does not leave a stray space when a name is missing`() {
        assertEquals("Nomsa", User(firstName = "Nomsa").fullName)
        assertEquals("Dlamini", User(lastName = "Dlamini").fullName)
        assertEquals("", User().fullName)
    }

    @Test
    fun `a new account is a volunteer, not an admin`() {
        val user = User()
        assertEquals(User.ROLE_VOLUNTEER, user.role)
        assertFalse(user.isAdmin)
    }

    @Test
    fun `only the admin role counts as admin`() {
        assertTrue(User(role = User.ROLE_ADMIN).isAdmin)
        assertFalse(User(role = "Admin").isAdmin)
        assertFalse(User(role = "superuser").isAdmin)
        assertFalse(User(role = "").isAdmin)
    }

    @Test
    fun `a new account has not finished its profile`() {
        assertFalse(User().profileComplete)
    }

    @Test
    fun `a new account has no interests or favourites`() {
        val user = User()
        assertTrue(user.programmeInterests.isEmpty())
        assertTrue(user.favouriteActivityIds.isEmpty())
    }
}
