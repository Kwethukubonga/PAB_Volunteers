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

    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var dateMillis = 0L
    private var existing: Activity? = null

    private val activityId: String
        get() = arguments?.getString(Constants.EXTRA_ACTIVITY_ID).orEmpty()

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

        existing = viewModel.activityById(activityId)
        binding.tvHeading.setText(
            if (existing == null) R.string.new_activity_title else R.string.edit_activity_title
        )
        existing?.let { fillFrom(it) }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnSave.isEnabled = !loading
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.tvMessage.isVisible = !message.isNullOrBlank()
            binding.tvMessage.text = message.orEmpty()
        }
        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.consumeSaved()
                Toast.makeText(requireContext(), R.string.activity_saved, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
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
        dateMillis = activity.dateMillis
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        if (dateMillis > 0) calendar.timeInMillis = dateMillis
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
        ).show()
    }

    private fun pickTime(target: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
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
            location.isBlank() -> getString(R.string.error_field_required)
            role.isBlank() -> getString(R.string.error_field_required)
            spots <= 0 -> getString(R.string.error_total_spots_invalid)
            else -> null
        }
        if (error != null) {
            binding.tvMessage.isVisible = true
            binding.tvMessage.text = error
            return
        }
        binding.tvMessage.isVisible = false

        val current = existing
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
