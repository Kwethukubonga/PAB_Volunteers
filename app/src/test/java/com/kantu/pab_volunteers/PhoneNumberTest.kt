package com.kantu.pab_volunteers

import com.kantu.pab_volunteers.utils.PhoneNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberTest {

    @Test
    fun acceptsSouthAfricanCellphoneNumbers() {
        listOf(
            "0821234567",     // Vodacom
            "083 123 4567",   // MTN
            "074 123 4567",   // Cell C
            "081 123 4567",   // Telkom
            "060 123 4567",   // newer shared block
            "071 234 5678",
            "+27 82 123 4567",
            "+27821234567"
        ).forEach { assertTrue(it, PhoneNumber.isValid(it)) }
    }

    @Test
    fun acceptsInternationalNumbers() {
        assertTrue(PhoneNumber.isValid("+49 151 2345 6789"))
        assertTrue(PhoneNumber.isValid("+1 202 555 0143"))
    }

    @Test
    fun refusesCodesNoNetworkUses() {
        listOf("0888888888", "088 123 4567", "085 123 4567", "075 123 4567", "+27 88 123 4567")
            .forEach { assertFalse(it, PhoneNumber.isValid(it)) }
    }

    @Test
    fun refusesLandlineTollFreeAndShareCall() {
        listOf("021 123 4567", "011 123 4567", "0800 123 456", "0861 123 456")
            .forEach { assertFalse(it, PhoneNumber.isValid(it)) }
    }

    @Test
    fun refusesWrongLengthAndMadeUpNumbers() {
        listOf(
            "082 123 456",      // too short
            "082 123 45678",    // too long
            "082 222 2222",     // same digit repeated
            "+27 082 123 4567", // extra 0 after +27
            "821234567",        // missing the 0
            "",
            "phone"
        ).forEach { assertFalse(it, PhoneNumber.isValid(it)) }
    }

    // Formatting as it is typed (the profile phone fields).

    @Test
    fun spacesALocalNumberAsItIsReadOut() {
        assertEquals("082 123 4567", PhoneNumber.formatAsTyped("0821234567"))
        assertEquals("082 123", PhoneNumber.formatAsTyped("082123"))
        assertEquals("08", PhoneNumber.formatAsTyped("08"))
    }

    @Test
    fun spacesAPlus27Number() {
        assertEquals("+27 82 123 4567", PhoneNumber.formatAsTyped("+27821234567"))
        assertEquals("+27", PhoneNumber.formatAsTyped("+27"))
    }

    @Test
    fun stopsALocalNumberAtTenDigits() {
        assertEquals("082 123 4567", PhoneNumber.formatAsTyped("08212345678"))
    }

    @Test
    fun leavesOtherInternationalNumbersAsTyped() {
        assertEquals("+49 151 2345 6789", PhoneNumber.formatAsTyped("+49 151 2345 6789"))
    }

    @Test
    fun aFormattedNumberStillPassesTheCheck() {
        assertTrue(PhoneNumber.isValid(PhoneNumber.formatAsTyped("0821234567")))
        assertTrue(PhoneNumber.isValid(PhoneNumber.formatAsTyped("+27821234567")))
    }

    @Test
    fun keepsTheCursorAfterTheDigitJustTyped() {
        // Four digits typed in "082 1": the cursor goes after the 1, past the space.
        assertEquals(5, PhoneNumber.cursorAfterDigits("082 1", 4))
        assertEquals(0, PhoneNumber.cursorAfterDigits("082 1", 0))
        assertEquals(1, PhoneNumber.cursorAfterDigits("+27 82", 0))
    }
}
