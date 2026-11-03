package com.kantu.pab_volunteers.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/** The browser link used when the phone has no maps app. */
class DirectionsTest {

    @Test
    fun `the place is sent as one search, with spaces and commas encoded`() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=55+Strauss+Road%2C+Lavender+Hill",
            Directions.webUrl(" 55 Strauss Road, Lavender Hill ")
        )
    }
}
