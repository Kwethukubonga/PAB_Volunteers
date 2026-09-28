package com.kantu.pab_volunteers.data.model

import com.kantu.pab_volunteers.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The sign-up id is what stops one volunteer taking two places on the same activity,
 * and the security rules check the same shape, so it must not drift.
 */
class ActivitySignupTest {

    @Test
    fun `the id is the activity and the volunteer joined together`() {
        assertEquals("act1_user1", ActivitySignup.idFor("act1", "user1"))
    }

    @Test
    fun `the same pair always gives the same id, so a second place cannot be taken`() {
        assertEquals(
            ActivitySignup.idFor("act1", "user1"),
            ActivitySignup.idFor("act1", "user1")
        )
    }

    @Test
    fun `different volunteers on one activity get different ids`() {
        assertTrue(ActivitySignup.idFor("act1", "user1") != ActivitySignup.idFor("act1", "user2"))
    }

    @Test
    fun `one volunteer on different activities gets different ids`() {
        assertTrue(ActivitySignup.idFor("act1", "user1") != ActivitySignup.idFor("act2", "user1"))
    }

    @Test
    fun `the id matches the shape the security rules expect`() {
        // The rules build activityId + '_' + uid and refuse anything else.
        val id = ActivitySignup.idFor("act1", "user1")
        assertTrue(id.matches(Regex(".+_user1")))
    }

    @Test
    fun `a past sign-up can stand in for an activity that is gone`() {
        val signup = ActivitySignup(
            activityId = "act1",
            activityTitle = "Community Kitchen",
            programme = "Community Feeding",
            dateMillis = DateUtils.now() - DateUtils.DAY_MILLIS,
            startTime = "09:00",
            endTime = "12:00"
        )
        val standIn = signup.asActivity()
        assertEquals("act1", standIn.id)
        assertEquals("Community Kitchen", standIn.title)
        // Hours still count even after an admin deletes the activity.
        assertEquals(180, DateUtils.lengthInMinutes(standIn.startTime, standIn.endTime))
    }
}
