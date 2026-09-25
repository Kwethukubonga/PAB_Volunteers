package com.kantu.pab_volunteers.ui.admin.volunteers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentVolunteerDetailsBinding
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.Constants
import com.kantu.pab_volunteers.utils.DateUtils

/** A volunteer's profile as the admin sees it. Viewing only, with no actions on the person. */
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
