package com.kantu.pab_volunteers.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kantu.pab_volunteers.R

/**
 * Opens a WhatsApp chat with Philisa. A wa.me link opens the app when it is installed and
 * the web version when it is not, so there is one link rather than two paths.
 */
object WhatsApp {

    fun open(context: Context, number: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(chatUrl(number))))
        } catch (e: ActivityNotFoundException) {
            (context as? android.app.Activity)?.let {
                AppMessage.showOver(it, UiText.Res(R.string.error_no_app))
            }
        }
    }

    /**
     * wa.me wants the number in international form with no plus, spaces or brackets, so a
     * local 021 565 0668 has to be sent as 27215650668. Kept apart from the intent so it
     * can be tested.
     */
    fun chatUrl(number: String): String = "https://wa.me/" + internationalDigits(number)

    private fun internationalDigits(number: String): String {
        val digits = number.filter { it.isDigit() }
        return when {
            digits.isEmpty() -> ""
            // Already international: the digits are the country code onwards.
            number.trimStart().startsWith("+") -> digits
            // A South African number written the local way: swap the leading 0 for 27.
            digits.startsWith("0") -> "27" + digits.drop(1)
            else -> digits
        }
    }
}
