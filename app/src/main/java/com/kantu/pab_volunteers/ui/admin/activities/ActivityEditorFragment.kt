package com.kantu.pab_volunteers.ui.admin.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentActivityEditorBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.ui.profile.programmeOptions
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Creates a new activity when opened with a blank id, otherwise edits the existing one. */
class ActivityEditorFragment : Fragment() {

    private var _binding: FragmentActivityEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    // Stored values, so they stay in plain digits whatever language the app is showing.
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private var dateMillis = 0L
    private var existing: Activity? = null

    private val activityId: String
        get() = arguments?.getString(Constants.EXTRA_ACTIVITY_ID).orEmpty()

    private val isEditing: Boolean
        get() = activityId.isNotBlank()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.etDate.setOnClickListener { pickDate() }
        binding.etStartTime.setOnClickListener { pickTime(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { pickTime(binding.etEndTime) }
        binding.btnSave.setOnClickListener { save() }

        val programmeNames = programmeOptions.map { getString(it.nameRes) }
        binding.etProgramme.setSimpleItems(programmeNames.toTypedArray())

        binding.tvHeading.setText(
            if (isEditing) R.string.edit_activity_title else R.string.new_activity_title
        )

        if (isEditing) {
            // Android can reopen this screen before the list has loaded. Wait for the activity
            // instead of showing an empty form, which would save as a duplicate.
            var filled = false
            viewModel.activities.observe(viewLifecycleOwner) {
                val found = viewModel.activityById(activityId)
                if (found != null && !filled) {
                    filled = true
                    existing = found
                    fillFrom(found)
                }
                updateSaveEnabled()
            }
            viewModel.refreshIfEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            updateSaveEnabled()
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            binding.tvMessage.isVisible = true
            binding.tvMessage.text = message.resolve(requireContext())
            // Cleared once shown, so it is not still there next time the editor opens.
            viewModel.consumeMessage()
        }
        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.consumeSaved()
                Toast.makeText(requireContext(), R.string.activity_saved, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    /** Saving is held back while busy, or while an existing activity is still loading. */
    private fun updateSaveEnabled() {
        val loading = viewModel.isLoading.value == true
        binding.btnSave.isEnabled = !loading && (!isEditing || existing != null)
    }

    private fun fillFrom(activity: Activity) {
        binding.etTitle.setText(activity.title)
        // false stops the dropdown filtering itself down to the single saved value.
        binding.etProgramme.setText(activity.programme, false)
        binding.etDate.setText(activity.date)
        binding.etStartTime.setText(activity.startTime)
        binding.etEndTime.setText(activity.endTime)
        binding.etLocation.setText(activity.location)
        binding.etRole.setText(activity.volunteerRole)
        binding.etTotalSpots.setText(activity.totalSpots.toString())
        binding.etDescription.setText(activity.description)
        binding.switchPublished.isChecked = activity.status == Activity.STATUS_PUBLISHED
        // Older activities only stored the date as text. Without this, saving one would
        // quietly move it to today.
        dateMillis = activity.dateMillis.takeIf { it > 0 }
            ?: runCatching { isoDate.parse(activity.date)?.time }.getOrNull()
            ?: 0L
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        if (dateMillis > 0) calendar.timeInMillis = dateMillis
        val today = DateUtils.startOfDay(DateUtils.now())
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                dateMillis = calendar.timeInMillis
                binding.etDate.setText(isoDate.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Nothing new can be put in the past. An older activity being edited keeps its own day.
            datePicker.minDate = if (dateMillis in 1 until today) dateMillis else today
        }.show()
    }

    private fun pickTime(target: TextInputEditText) {
        // Opens on the time already chosen, or on the current time for an empty field.
        val chosen = DateUtils.minutesOfDay(target.text?.toString().orEmpty())
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                target.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute))
            },
            chosen?.div(60) ?: calendar.get(Calendar.HOUR_OF_DAY),
            chosen?.rem(60) ?: calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun save() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        val programme = binding.etProgramme.text?.toString().orEmpty().trim()
        val date = binding.etDate.text?.toString().orEmpty().trim()
        val startTime = binding.etStartTime.text?.toString().orEmpty().trim()
        val endTime = binding.etEndTime.text?.toString().orEmpty().trim()
        val location = binding.etLocation.text?.toString().orEmpty().trim()
        val role = binding.etRole.text?.toString().orEmpty().trim()
        val spots = binding.etTotalSpots.text?.toString().orEmpty().trim().toIntOrNull() ?: 0
        val description = binding.etDescription.text?.toString().orEmpty().trim()

        val error = when {
            title.isBlank() -> getString(R.string.error_field_required)
            programme.isBlank() -> getString(R.string.error_field_required)
            date.isBlank() -> getString(R.string.error_field_required)
            startTime.isBlank() || endTime.isBlank() -> getString(R.string.error_field_required)
            DateUtils.lengthInMinutes(startTime, endTime) <= 0 ->
                getString(R.string.error_end_before_start)
            location.isBlank() -> getString(R.string.error_field_required)
            role.isBlank() -> getString(R.string.error_field_required)
            spots <= 0 -> getString(R.string.error_total_spots_invalid)
            spots < (existing?.filledSpots ?: 0) ->
                getString(R.string.error_spots_below_taken, existing?.filledSpots ?: 0)
            else -> null
        }
        if (error != null) {
            binding.tvMessage.isVisible = true
            binding.tvMessage.text = error
            return
        }
        binding.tvMessage.isVisible = false

        val current = existing
        if (isEditing && current == null) return
        viewModel.saveActivity(
            Activity(
                id = current?.id.orEmpty(),
                title = title,
                programme = programme,
                date = date,
                dateMillis = if (dateMillis > 0) dateMillis else DateUtils.now(),
                startTime = startTime,
                endTime = endTime,
                location = location,
                volunteerRole = role,
                totalSpots = spots,
                // Places already taken must survive an edit.
                filledSpots = current?.filledSpots ?: 0,
                description = description,
                status = if (binding.switchPublished.isChecked) {
                    Activity.STATUS_PUBLISHED
                } else {
                    Activity.STATUS_DRAFT
                },
                createdBy = current?.createdBy.orEmpty(),
                createdDate = current?.createdDate ?: 0L
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
