package com.kantu.pab_volunteers.ui.volunteer.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentProfileBinding
import com.kantu.pab_volunteers.databinding.ItemMilestoneBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.safety.ClearanceState
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

        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { bindUser(it) } }
        viewModel.completedCount.observe(viewLifecycleOwner) {
            binding.statActivities.tvStatValue.text = it.toString()
        }
        viewModel.upcomingCount.observe(viewLifecycleOwner) {
            binding.statUpcoming.tvStatValue.text = it.toString()
        }
        viewModel.clearance.observe(viewLifecycleOwner) { bindClearance(it) }
        viewModel.milestones.observe(viewLifecycleOwner) { milestones ->
            val slots = listOf(binding.milestone1, binding.milestone2, binding.milestone3, binding.milestone4)
            milestones.zip(slots).forEach { (milestone, slot) -> bindMilestone(slot, milestone) }
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

    private fun bindClearance(clearance: Clearance?) {
        val state = ClearanceState.of(clearance, DateUtils.now())
        binding.tvClearance.text = when (state) {
            ClearanceState.NONE -> getString(R.string.clearance_none_profile)
            ClearanceState.REQUESTED -> getString(R.string.clearance_requested)
            ClearanceState.CLEARED ->
                getString(R.string.clearance_cleared_until, DateUtils.formatDate(clearance?.expiresAt ?: 0L))
            ClearanceState.EXPIRED -> getString(R.string.clearance_expired)
        }
        val tint = if (state == ClearanceState.CLEARED) R.color.status_success_text else R.color.text_secondary
        binding.ivClearance.setColorFilter(ContextCompat.getColor(requireContext(), tint))
        binding.cardClearance.contentDescription =
            getString(R.string.settings_clearance) + ". " + binding.tvClearance.text
    }

    /** Earned ones are in the brand colour; the rest are grey with how far along they are. */
    private fun bindMilestone(slot: ItemMilestoneBinding, milestone: Milestone) {
        val title = getString(milestone.kind.title)
        val progress = if (milestone.earned) {
            getString(R.string.milestone_earned)
        } else {
            getString(R.string.milestone_progress, milestone.progress, milestone.kind.target)
        }
        slot.ivMilestone.setImageResource(milestone.kind.icon)
        slot.ivMilestone.setColorFilter(
            ContextCompat.getColor(requireContext(), if (milestone.earned) R.color.accent else R.color.text_secondary)
        )
        slot.ivMilestone.alpha = if (milestone.earned) 1f else 0.6f
        slot.tvMilestoneTitle.text = title
        slot.tvMilestoneProgress.text = progress
        slot.root.contentDescription = "$title, $progress"
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
