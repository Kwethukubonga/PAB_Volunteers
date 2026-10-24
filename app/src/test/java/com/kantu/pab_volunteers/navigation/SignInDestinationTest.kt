package com.kantu.pab_volunteers.navigation

import com.kantu.pab_volunteers.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where someone lands after signing in (PV-FR02 and PV-FR12). */
class SignInDestinationTest {

    @Test
    fun `an admin is taken to the admin area`() {
        val admin = User(role = User.ROLE_ADMIN, profileComplete = true)

        assertEquals(SignInDestination.ADMIN_HOME, SignInDestination.forUser(admin))
    }

    @Test
    fun `an admin goes to the admin area even before finishing a profile`() {
        val admin = User(role = User.ROLE_ADMIN, profileComplete = false)

        assertEquals(SignInDestination.ADMIN_HOME, SignInDestination.forUser(admin))
    }

    @Test
    fun `a volunteer with a finished profile is taken to the volunteer home`() {
        val volunteer = User(role = User.ROLE_VOLUNTEER, profileComplete = true)

        assertEquals(SignInDestination.VOLUNTEER_HOME, SignInDestination.forUser(volunteer))
    }

    @Test
    fun `a new account is taken to profile setup first`() {
        assertEquals(SignInDestination.PROFILE_SETUP, SignInDestination.forUser(User()))
    }

    @Test
    fun `an unknown role is treated as a volunteer, never an admin`() {
        val odd = User(role = "superuser", profileComplete = true)

        assertEquals(SignInDestination.VOLUNTEER_HOME, SignInDestination.forUser(odd))
    }
}
