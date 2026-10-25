package com.kantu.pab_volunteers.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Spaces a phone number as it is typed (see [PhoneNumber.formatAsTyped]) and keeps the cursor
 * after the digit the person just typed, so editing in the middle still works.
 */
class PhoneFormattingWatcher private constructor(private val field: EditText) : TextWatcher {

    private var formatting = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        if (formatting || s == null) return
        val current = s.toString()
        val formatted = PhoneNumber.formatAsTyped(current)
        if (formatted == current) return

        val cursor = field.selectionEnd.coerceIn(0, current.length)
        val digitsBefore = current.substring(0, cursor).count { it.isDigit() }
        formatting = true
        s.replace(0, s.length, formatted)
        formatting = false
        field.setSelection(PhoneNumber.cursorAfterDigits(formatted, digitsBefore).coerceIn(0, formatted.length))
    }

    companion object {
        fun attach(field: EditText) {
            field.addTextChangedListener(PhoneFormattingWatcher(field))
        }
    }
}
