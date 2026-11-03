package com.kantu.pab_volunteers.ui.settings

import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.utils.DateUtils

/** How someone signs in, which decides how Settings lets them change a password or confirm it's them. */
enum class SignInMethod {
    PASSWORD,
    GOOGLE;

    companion object {
        private const val PASSWORD_PROVIDER = "password"

        /**
         * From the provider IDs Firebase lists for the account. Someone with a password as well
         * as Google is treated as a password account, since that is the one they can change.
         */
        fun from(providerIds: List<String>): SignInMethod =
            if (PASSWORD_PROVIDER in providerIds) PASSWORD else GOOGLE
    }
}

/** What deleting an account would take away, shown before anything is removed. */
data class AccountSummary(val hours: Int, val upcoming: Int, val completed: Int) {

    companion object {
        /** Hours are counted the same way as on the Profile screen: whole hours of finished activities. */
        fun from(signups: List<ActivitySignup>, now: Long): AccountSummary {
            val (finished, coming) = signups.map { it.asActivity() }.partition { it.endsAtMillis <= now }
            val minutes = finished.sumOf { DateUtils.lengthInMinutes(it.startTime, it.endTime) }
            return AccountSummary(hours = minutes / 60, upcoming = coming.size, completed = finished.size)
        }
    }
}

/** The last step of deleting an account: typing the confirmation word, in the app's language. */
object DeleteConfirmation {

    fun matches(typed: String, word: String): Boolean =
        word.isNotBlank() && typed.trim().equals(word.trim(), ignoreCase = true)
}
