package com.kantu.pab_volunteers.utils

/**
 * One colour per person for their initial (K11), picked from their account id so the same person
 * always looks the same. Every colour keeps white text at 4.5:1 or better, in light and dark mode.
 */
object AvatarColors {

    val PALETTE = listOf(
        0xFF5B21B6.toInt(), // purple
        0xFF0F766E.toInt(), // teal
        0xFFB91C1C.toInt(), // red
        0xFF1D4ED8.toInt(), // blue
        0xFFBE185D.toInt(), // pink
        0xFF15803D.toInt(), // green
        0xFF4338CA.toInt(), // indigo
        0xFFC2410C.toInt()  // orange
    )

    fun forId(id: String): Int = PALETTE[Math.floorMod(id.hashCode(), PALETTE.size)]
}
