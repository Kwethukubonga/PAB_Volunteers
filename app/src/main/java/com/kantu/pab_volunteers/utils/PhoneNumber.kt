package com.kantu.pab_volunteers.utils

/**
 * Checks a phone number against the South African numbering plan set by ICASA.
 *
 * A South African cellphone number is 10 digits: 0, then a two digit network code, then a
 * seven digit number, for example 082 123 4567. It can also be written +27 82 123 4567.
 * Only network codes ICASA has given to the cellphone networks are accepted, so something
 * like 088 888 8888 is refused. Landlines, toll free (080) and share call (086) numbers are
 * refused too, because the team needs a number they can call, SMS or WhatsApp.
 *
 * Volunteers from abroad can still use an international number starting with + and their
 * country code, for example +49 151 2345 6789.
 */
object PhoneNumber {

    // Network codes after the leading 0. People can move their number to another network,
    // so the code only shows which network the number started on.
    private val MOBILE_CODES = setOf(
        // Vodacom
        "72", "76", "79", "82",
        // MTN
        "73", "78", "83",
        // Cell C
        "74", "84",
        // Mainly Telkom
        "81",
        // Newer blocks shared between Vodacom, MTN, Cell C and Telkom
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "71"
    )

    private val LOCAL_SHAPE = Regex("^0\\d{9}$")
    private val INTERNATIONAL = Regex("^\\+[1-9]\\d{7,14}$")

    fun isValid(input: String): Boolean {
        val compact = input.filterNot { it.isWhitespace() }
        val local = when {
            compact.startsWith("+27") -> "0" + compact.removePrefix("+27")
            compact.startsWith("0") -> compact
            else -> return INTERNATIONAL.matches(compact)
        }
        if (!LOCAL_SHAPE.matches(local)) return false
        if (local.substring(1, 3) !in MOBILE_CODES) return false
        // The right shape, but plainly made up, like 082 222 2222.
        return local.substring(3).toSet().size > 1
    }

    /** Trims it and keeps single spaces, so it is stored the way it reads. */
    fun tidy(input: String): String = input.trim().replace(Regex("\\s+"), " ")
}
