package com.kantu.pab_volunteers.ui.volunteer.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Clearance
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentProfileBinding
import com.kantu.pab_volunteers.databinding.ItemMilestoneBinding
import com.kantu.pab_volunteers.databinding.ItemPlanRowBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.safety.ClearanceState
import com.kantu.pab_volunteers.ui.settings.SettingsActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityRow
import com.kantu.pab_volunteers.utils.AvatarColors
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.RelativeDay
import com.kantu.pab_volunteers.utils.observeMessages

/**
 * The volunteer's own page: who they are, their clearance, milestones, and their places as
 * Upcoming and Past tabs with when each one is (K10), then their details and Settings.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private var showingPast = false
    private var upcoming: List<ActivityRow> = emptyList()
    private var past: List<ActivityRow> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        binding.btnSettings.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }

        setUpTabs()

        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { bindUser(it) } }
        viewModel.clearance.observe(viewLifecycleOwner) { bindClearance(it) }
        viewModel.milestones.observe(viewLifecycleOwner) { milestones ->
            val slots = listOf(binding.milestone1, binding.milestone2, binding.milestone3, binding.milestone4)
            milestones.zip(slots).forEach { (milestone, slot) -> bindMilestone(slot, milestone) }
        }
        viewModel.mySchedule.observe(viewLifecycleOwner) {
            upcoming = it
            renderMyActivities()
        }
        viewModel.scheduleCompleted.observe(viewLifecycleOwner) {
            past = it
            renderMyActivities()
        }
    }

    private fun setUpTabs() {
        binding.tabsMyActivities.addTab(binding.tabsMyActivities.newTab())
        binding.tabsMyActivities.addTab(binding.tabsMyActivities.newTab())
        binding.tabsMyActivities.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showingPast = tab.position == 1
                renderMyActivities()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        // Keep the highlighted tab matched to the list when the view is rebuilt.
        binding.tabsMyActivities.getTabAt(if (showingPast) 1 else 0)?.select()
    }

    private fun bindUser(user: User) {
        binding.tvAvatarInitial.text = user.firstName.take(1).uppercase()
        binding.tvAvatarInitial.background = binding.tvAvatarInitial.background?.mutate()?.apply {
            setTint(AvatarColors.forId(user.uid))
        }
        binding.tvUserName.text = user.fullName
        val months = DateUtils.monthsSince(user.joinedDate)
        binding.tvVolunteerId.text = getString(
            R.string.profile_id_line, user.volunteerId,
            resources.getQuantityString(R.plurals.profile_volunteering_for, months, months)
        )
        binding.rowEmail.tvRowValue.text = user.email
        binding.rowPhone.tvRowValue.text = user.phone
        binding.rowArea.tvRowValue.text = user.area
    }

    /** Upcoming soonest first, past newest first, each with "today", "in 3 days" or "2 weeks ago". */
    private fun renderMyActivities() {
        if (_binding == null) return
        binding.tabsMyActivities.getTabAt(0)?.text = getString(R.string.profile_tab_upcoming, upcoming.size)
        binding.tabsMyActivities.getTabAt(1)?.text = getString(R.string.profile_tab_past, past.size)

        val rows = if (showingPast) past else upcoming
        val now = DateUtils.now()
        binding.layoutMyActivities.removeAllViews()
        if (rows.isEmpty()) {
            val empty = ItemPlanRowBinding.inflate(layoutInflater, binding.layoutMyActivities, false)
            empty.tvTitle.setText(if (showingPast) R.string.empty_nothing_completed else R.string.empty_no_schedule)
            empty.tvMeta.isVisible = false
            empty.root.isClickable = false
            binding.layoutMyActivities.addView(empty.root)
            return
        }
        rows.forEachIndexed { index, row ->
            if (index > 0) binding.layoutMyActivities.addView(divider())
            val item = ItemPlanRowBinding.inflate(layoutInflater, binding.layoutMyActivities, false)
            item.ivTile.isVisible = true
            item.ivTile.setImageResource(R.drawable.ic_calendar)
            item.ivTile.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
            item.tvTitle.text = row.activity.title
            item.tvMeta.text = row.activity.programme
            item.tvMeta.isVisible = row.activity.programme.isNotBlank()
            item.tvWhen.isVisible = true
            item.tvWhen.text = RelativeDay.label(requireContext(), now, row.activity.dateMillis)
            item.root.setOnClickListener { VolunteerNavGraph.toActivityDetails(findNavController(), row.activity.id) }
            binding.layoutMyActivities.addView(item.root)
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

        // A pill only once something has happened: Cleared, Waiting or Expired.
        val pill = when (state) {
            ClearanceState.CLEARED -> Triple(R.string.clearance_pill_cleared, R.color.status_success_bg, R.color.status_success_text)
            ClearanceState.REQUESTED -> Triple(R.string.clearance_pill_waiting, R.color.status_info_bg, R.color.status_info_text)
            ClearanceState.EXPIRED -> Triple(R.string.clearance_pill_expired, R.color.status_neutral_bg, R.color.status_neutral_text)
            ClearanceState.NONE -> null
        }
        binding.tvClearancePill.isVisible = pill != null
        if (pill != null) {
            binding.tvClearancePill.setText(pill.first)
            binding.tvClearancePill.background = binding.tvClearancePill.background?.mutate()?.apply {
                setTint(ContextCompat.getColor(requireContext(), pill.second))
            }
            binding.tvClearancePill.setTextColor(ContextCompat.getColor(requireContext(), pill.third))
        }
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

    private fun divider() = View(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.stroke_normal))
        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
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
