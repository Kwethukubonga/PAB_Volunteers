package com.kantu.pab_volunteers.ui.volunteer.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentProfileBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.settings.SettingsActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.observeMessages

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.rowEmail.ivRowIcon.setImageResource(R.drawable.ic_email)
        binding.rowEmail.tvRowLabel.setText(R.string.label_email_caps)
        binding.rowPhone.ivRowIcon.setImageResource(R.drawable.ic_phone)
        binding.rowPhone.tvRowLabel.setText(R.string.label_phone_caps)
        binding.rowArea.ivRowIcon.setImageResource(R.drawable.ic_location)
        binding.rowArea.tvRowLabel.setText(R.string.label_area_caps)

        binding.statMonths.tvStatLabel.setText(R.string.stat_months)
        binding.statActivities.tvStatLabel.setText(R.string.stat_completed)
        binding.statUpcoming.tvStatLabel.setText(R.string.stat_upcoming)

        binding.btnEditProfile.setOnClickListener {
            VolunteerNavGraph.toEditProfile(findNavController())
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.btnSignOut.setOnClickListener { confirmSignOut() }

        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { bindUser(it) } }
        viewModel.completedCount.observe(viewLifecycleOwner) {
            binding.statActivities.tvStatValue.text = it.toString()
        }
        viewModel.upcomingCount.observe(viewLifecycleOwner) {
            binding.statUpcoming.tvStatValue.text = it.toString()
        }
    }

    private fun bindUser(user: User) {
        binding.tvAvatarInitial.text = user.firstName.take(1).uppercase()
        binding.tvUserName.text = user.fullName
        binding.tvVolunteerId.text = getString(R.string.volunteer_id_label, user.volunteerId)
        binding.rowEmail.tvRowValue.text = user.email
        binding.rowPhone.tvRowValue.text = user.phone
        binding.rowArea.tvRowValue.text = user.area
        binding.statMonths.tvStatValue.text = DateUtils.monthsSince(user.joinedDate).toString()

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

    private fun confirmSignOut() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.sign_out_confirm_title)
            .setPositiveButton(R.string.action_sign_out) { _, _ ->
                FirebaseAuthManager.signOut()
                AppNavGraph.goToWelcome(requireActivity())
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
