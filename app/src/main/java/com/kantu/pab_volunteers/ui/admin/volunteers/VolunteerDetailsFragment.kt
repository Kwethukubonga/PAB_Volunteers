package com.kantu.pab_volunteers.ui.admin.volunteers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentVolunteerDetailsBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.ui.safety.ClearanceState
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.observeMessages

/**
 * A volunteer's profile as the admin sees it. The only action is recording their background
 * check, after seeing the documents in person; nothing else about the person can be changed here.
 */
class VolunteerDetailsFragment : Fragment() {

    private var _binding: FragmentVolunteerDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolunteerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.rowEmail.ivRowIcon.setImageResource(R.drawable.ic_email)
        binding.rowEmail.tvRowLabel.setText(R.string.label_email_caps)
        binding.rowPhone.ivRowIcon.setImageResource(R.drawable.ic_phone)
        binding.rowPhone.tvRowLabel.setText(R.string.label_phone_caps)
        binding.rowArea.ivRowIcon.setImageResource(R.drawable.ic_location)
        binding.rowArea.tvRowLabel.setText(R.string.label_area_caps)
        binding.rowJoined.ivRowIcon.setImageResource(R.drawable.ic_calendar)
        binding.rowJoined.tvRowLabel.setText(R.string.label_joined)

        val uid = arguments?.getString(Constants.EXTRA_VOLUNTEER_ID).orEmpty()
        viewModel.volunteers.observe(viewLifecycleOwner) {
            viewModel.volunteerById(uid)?.let { bind(it) }
        }
        viewModel.clearance.observe(viewLifecycleOwner) { bindClearance(it) }
        viewModel.loadClearance(uid)

        binding.btnApproveClearance.setOnClickListener {
            val volunteer = viewModel.volunteerById(uid) ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.clearance_admin_approve)
                .setMessage(getString(R.string.clearance_admin_approve_confirm, volunteer.fullName, ClearanceState.VALID_MONTHS))
                .setPositiveButton(R.string.clearance_admin_approve) { _, _ -> viewModel.approveClearance(volunteer) }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
        binding.btnRemoveClearance.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.clearance_admin_remove)
                .setMessage(R.string.clearance_admin_remove_confirm)
                .setPositiveButton(R.string.clearance_admin_remove) { _, _ -> viewModel.removeClearance(uid) }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
        // Android can reopen this screen before the list has loaded.
        viewModel.refreshIfEmpty()
    }

    private fun bind(user: User) {
        binding.tvInitial.text = user.firstName.take(1).uppercase()
        binding.tvName.text = user.fullName
        binding.tvVolunteerId.text = getString(R.string.volunteer_id_label, user.volunteerId)
        binding.rowEmail.tvRowValue.text = user.email
        binding.rowPhone.tvRowValue.text = user.phone
        binding.rowArea.tvRowValue.text = user.area
        binding.rowJoined.tvRowValue.text = DateUtils.formatDate(user.joinedDate)

        binding.chipGroupInterests.removeAllViews()
        user.programmeInterests.forEach { interest ->
            binding.chipGroupInterests.addView(
                Chip(requireContext()).apply {
                    text = interest
                    isClickable = false
                    isCheckable = false
                }
            )
        }
    }

    private fun bindClearance(clearance: Clearance?) {
        val state = ClearanceState.of(clearance, DateUtils.now())
        binding.tvClearance.text = when (state) {
            ClearanceState.NONE -> getString(R.string.clearance_admin_none)
            ClearanceState.REQUESTED -> getString(R.string.clearance_admin_requested, DateUtils.formatDate(clearance?.requestedAt ?: 0L))
            ClearanceState.CLEARED -> getString(
                R.string.clearance_admin_cleared,
                DateUtils.formatDate(clearance?.expiresAt ?: 0L),
                clearance?.checkedBy.orEmpty()
            )
            ClearanceState.EXPIRED -> getString(R.string.clearance_expired)
        }
        binding.btnApproveClearance.isVisible = state != ClearanceState.CLEARED
        binding.btnRemoveClearance.isVisible = state == ClearanceState.CLEARED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
