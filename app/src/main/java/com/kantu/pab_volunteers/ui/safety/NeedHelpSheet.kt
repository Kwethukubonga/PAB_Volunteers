package com.kantu.pab_volunteers.ui.safety

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.ItemSettingsRowBinding
import com.kantu.pab_volunteers.databinding.SheetNeedHelpBinding
import com.kantu.pab_volunteers.ui.settings.SettingsActivity
import com.kantu.pab_volunteers.utils.ActivityIntents

/**
 * The "I need help" sheet on the day of a joined activity (K12): call 112, call the police on
 * 10111, call Philisa, or text the emergency contact a ready-written message saying where they
 * are. Nothing is sent or dialled until the person taps call or send in the other app.
 */
object NeedHelpSheet {

    fun show(context: Context, activity: Activity, user: User?) {
        val dialog = BottomSheetDialog(context)
        val sheet = SheetNeedHelpBinding.inflate(LayoutInflater.from(context))

        sheet.rowEmergency.bind(R.drawable.ic_phone, context.getString(R.string.need_help_call_112), context.getString(R.string.need_help_call_112_desc)) {
            ActivityIntents.dial(context, EMERGENCY)
        }
        sheet.rowPolice.bind(R.drawable.ic_phone, context.getString(R.string.need_help_call_police), context.getString(R.string.need_help_call_police_desc)) {
            ActivityIntents.dial(context, POLICE)
        }
        sheet.rowPhilisa.bind(
            R.drawable.ic_phone,
            context.getString(R.string.need_help_call_philisa),
            context.getString(R.string.pab_crisis_line_display)
        ) { ActivityIntents.dial(context, context.getString(R.string.pab_crisis_line)) }

        if (user != null && user.hasEmergencyContact) {
            val name = user.emergencyContactName.ifBlank { user.emergencyContactPhone }
            sheet.rowContact.bind(
                R.drawable.ic_sms,
                context.getString(R.string.need_help_text_contact, name),
                context.getString(R.string.need_help_text_contact_desc)
            ) {
                val body = context.getString(R.string.need_help_sms, activity.title, activity.location)
                ActivityIntents.textMessage(context, user.emergencyContactPhone, body)
            }
        } else {
            // No contact yet: the row takes them to Settings to add one.
            sheet.rowContact.bind(
                R.drawable.ic_sms,
                context.getString(R.string.need_help_no_contact),
                context.getString(R.string.need_help_no_contact_desc)
            ) {
                dialog.dismiss()
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        }

        dialog.setContentView(sheet.root)
        dialog.show()
    }

    private fun ItemSettingsRowBinding.bind(@DrawableRes icon: Int, title: String, supporting: String, onClick: () -> Unit) {
        rowIcon.setImageResource(icon)
        rowTitle.text = title
        rowSupporting.text = supporting
        rowSupporting.isVisible = supporting.isNotBlank()
        rowChevron.isVisible = true
        root.isClickable = true
        root.isFocusable = true
        root.setOnClickListener { onClick() }
        ViewCompat.setScreenReaderFocusable(root, true)
    }

    // The national emergency number from any phone, and the SAPS emergency line.
    private const val EMERGENCY = "112"
    private const val POLICE = "10111"
}
