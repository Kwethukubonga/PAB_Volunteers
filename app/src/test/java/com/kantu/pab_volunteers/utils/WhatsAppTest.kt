package com.kantu.pab_volunteers.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * wa.me only accepts a number in international form with nothing but digits, so a number
 * written the way it appears on Philisa's own page has to be converted before it is used.
 */
class WhatsAppTest {

    @Test
    fun `a local number swaps its leading zero for the country code`() {
        assertEquals("https://wa.me/27215650668", WhatsApp.chatUrl("021 565 0668"))
        assertEquals("https://wa.me/27821234567", WhatsApp.chatUrl("082 123 4567"))
    }

    @Test
    fun `a number already written internationally keeps its country code`() {
        assertEquals("https://wa.me/27215650668", WhatsApp.chatUrl("+27 21 565 0668"))
        assertEquals("https://wa.me/4915123456789", WhatsApp.chatUrl("+49 151 2345 6789"))
    }

    @Test
    fun `brackets, dashes and spaces are dropped`() {
        assertEquals("https://wa.me/27215650668", WhatsApp.chatUrl("(021) 565-0668"))
    }

    @Test
    fun `an empty number gives a link with nothing after it rather than something broken`() {
        assertEquals("https://wa.me/", WhatsApp.chatUrl(""))
        assertEquals("https://wa.me/", WhatsApp.chatUrl("   "))
    }
}
