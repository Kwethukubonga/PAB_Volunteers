package com.kantu.pab_volunteers.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The picture and thumbs up rules an announcement card relies on. */
class AnnouncementTest {

    @Test
    fun `an announcement without a picture says so`() {
        assertFalse(Announcement().hasImage)
        assertFalse(Announcement(imageUrl = "   ").hasImage)
    }

    @Test
    fun `an announcement with a picture says so`() {
        val withPicture = Announcement(imageUrl = "https://res.cloudinary.com/demo/a.jpg")
        assertTrue(withPicture.hasImage)
    }

    @Test
    fun `a new announcement has no thumbs up`() {
        assertEquals(0, Announcement().thumbsUpCount)
    }

    @Test
    fun `thumbs up count follows the list`() {
        val liked = Announcement(thumbsUpBy = listOf("u1", "u2", "u3"))
        assertEquals(3, liked.thumbsUpCount)
    }

    @Test
    fun `a volunteer can tell their own thumbs up apart from everyone else's`() {
        val liked = Announcement(thumbsUpBy = listOf("u1", "u2"))
        assertTrue(liked.isThumbedUpBy("u1"))
        assertFalse(liked.isThumbedUpBy("u3"))
    }

    @Test
    fun `nobody has thumbed up an empty announcement`() {
        assertFalse(Announcement().isThumbedUpBy("u1"))
    }

    @Test
    fun `an empty user id is not treated as a thumbs up`() {
        val liked = Announcement(thumbsUpBy = listOf("u1"))
        assertFalse(liked.isThumbedUpBy(""))
    }

    @Test
    fun `a new announcement starts as a draft`() {
        assertEquals(Announcement.STATUS_DRAFT, Announcement().status)
    }
}
