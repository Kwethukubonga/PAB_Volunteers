package com.kantu.pab_volunteers.ui.volunteer.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentActivityDetailsBinding
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.observeMessages

class ActivityDetailsFragment : Fragment() {

    private var _binding: FragmentActivityDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private val activityId: String
        get() = arguments?.getString(Constants.EXTRA_ACTIVITY_ID).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.rowDateTime.ivRowIcon.setImageResource(R.drawable.ic_calendar)
        binding.rowDateTime.tvRowLabel.setText(R.string.label_date_time)
        binding.rowLocation.ivRowIcon.setImageResource(R.drawable.ic_location)
        binding.rowLocation.tvRowLabel.setText(R.string.label_location)
        binding.rowRole.ivRowIcon.setImageResource(R.drawable.ic_profile)
        binding.rowRole.tvRowLabel.setText(R.string.label_volunteer_role)
        binding.rowSpots.ivRowIcon.setImageResource(R.drawable.ic_people)
        binding.rowSpots.tvRowLabel.setText(R.string.label_spots_remaining)

        binding.btnJoin.setOnClickListener { join() }
        binding.btnLeave.setOnClickListener { confirmLeave() }

        viewModel.selectedActivity.observe(viewLifecycleOwner) { activity ->
            if (activity != null) bind(activity)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnJoin.isEnabled = !loading
            binding.btnLeave.isEnabled = !loading
        }

        viewModel.selectActivity(activityId)
    }

    private fun bind(activity: Activity) {
        val joined = viewModel.isJoined(activityId)

        binding.tvProgramme.text = activity.programme
        binding.tvTitle.text = activity.title
        binding.rowDateTime.tvRowValue.text = activity.dateTimeLabel
        binding.rowLocation.tvRowValue.text = activity.location
        binding.rowRole.tvRowValue.text = activity.volunteerRole
        binding.rowSpots.tvRowValue.text =
            getString(R.string.spots_available, activity.spotsRemaining)
        binding.tvDescription.text = activity.description

        bindStatus(activity, joined)

        val isFull = activity.spotsRemaining <= 0
        binding.btnJoin.isVisible = !joined
        binding.btnJoin.isEnabled = !isFull
        binding.btnJoin.setText(if (isFull) R.string.status_full else R.string.action_join_activity)
        binding.btnLeave.isVisible = joined
    }

    private fun bindStatus(activity: Activity, joined: Boolean) {
        val (labelRes, bgRes, textRes) = when {
            joined -> Triple(
                R.string.status_joined, R.color.status_success_bg, R.color.status_success_text
            )
            activity.spotsRemaining <= 0 -> Triple(
                R.string.status_full, R.color.status_neutral_bg, R.color.status_neutral_text
            )
            else -> Triple(
                R.string.status_open, R.color.status_info_bg, R.color.status_info_text
            )
        }
        binding.tvStatus.setText(labelRes)
        binding.tvStatus.background?.setTint(ContextCompat.getColor(requireContext(), bgRes))
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), textRes))
    }

    private fun join() {
        val activity = viewModel.selectedActivity.value ?: return
        viewModel.join(activity)
    }

    private fun confirmLeave() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.leave_confirm_title)
            .setMessage(R.string.leave_confirm_message)
            .setPositiveButton(R.string.action_yes_leave) { _, _ -> viewModel.leave(activityId) }
            .setNegativeButton(R.string.action_keep_spot, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelection()
        _binding = null
    }
}
