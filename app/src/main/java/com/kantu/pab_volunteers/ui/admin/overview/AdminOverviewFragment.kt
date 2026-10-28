package com.kantu.pab_volunteers.ui.admin.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentAdminOverviewBinding
import com.kantu.pab_volunteers.databinding.ItemSettingsRowBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.ui.admin.AdminMainActivity
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.ui.admin.volunteers.VolunteerAdapter
import com.kantu.pab_volunteers.utils.observeMessages

class AdminOverviewFragment : Fragment() {

    private var _binding: FragmentAdminOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: VolunteerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = VolunteerAdapter(emptyList()) { user ->
            AdminNavGraph.toVolunteerDetails(findNavController(), user.uid)
        }
        binding.rvRecentVolunteers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentVolunteers.adapter = adapter

        bindGlance(binding.rowVolunteers, R.drawable.ic_admin_volunteers, R.string.nav_volunteers) {
            (activity as? AdminMainActivity)?.selectTab(R.id.manageVolunteersFragment)
        }
        bindGlance(binding.rowActivities, R.drawable.ic_activities, R.string.nav_activities) {
            (activity as? AdminMainActivity)?.selectTab(R.id.manageActivitiesFragment)
        }
        // Every sign-up belongs to an activity, and opening one is the only way to read it,
        // so this lands on the activities list rather than sitting there as the one row in the
        // card that does nothing when it is tapped.
        bindGlance(binding.rowSignups, R.drawable.ic_people, R.string.overview_signups) {
            (activity as? AdminMainActivity)?.selectTab(R.id.manageActivitiesFragment)
        }

        setUpQuickAction(
            binding.actionCreateActivity.root,
            R.drawable.ic_add,
            R.string.action_create_new_activity,
            R.string.action_create_new_activity_desc
        ) { AdminNavGraph.toActivityEditor(findNavController()) }

        setUpQuickAction(
            binding.actionPostAnnouncement.root,
            R.drawable.ic_admin_posts,
            R.string.action_post_announcement,
            R.string.action_post_announcement_desc
        ) { AdminNavGraph.toAnnouncementEditor(findNavController()) }

        setUpQuickAction(
            binding.actionConcerns.root,
            R.drawable.ic_flag,
            R.string.concerns_title,
            R.string.concerns_action_desc
        ) { AdminNavGraph.toConcerns(findNavController()) }

        binding.tvAdminName.text = getString(
            R.string.signed_in_as,
            FirebaseAuthManager.currentUser?.email.orEmpty()
        )

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        observeViewModel()
    }

    /** One "at a glance" row: what it counts, the number in words, and a chevron when it opens a tab. */
    private fun bindGlance(row: ItemSettingsRowBinding, icon: Int, title: Int, onClick: (() -> Unit)?) {
        row.rowIcon.setImageResource(icon)
        row.rowTitle.setText(title)
        row.rowChevron.isVisible = onClick != null
        row.root.isClickable = onClick != null
        row.root.isFocusable = onClick != null
        if (onClick != null) row.root.setOnClickListener { onClick() }
    }

    private fun setUpQuickAction(
        root: View,
        iconRes: Int,
        titleRes: Int,
        subtitleRes: Int,
        onClick: () -> Unit
    ) {
        root.findViewById<android.widget.ImageView>(R.id.ivActionIcon).setImageResource(iconRes)
        root.findViewById<android.widget.TextView>(R.id.tvActionTitle).setText(titleRes)
        root.findViewById<android.widget.TextView>(R.id.tvActionSubtitle).setText(subtitleRes)
        root.setOnClickListener { onClick() }
    }

    private fun observeViewModel() {
        viewModel.volunteers.observe(viewLifecycleOwner) { volunteers ->
            binding.rowVolunteers.rowSupporting.text =
                resources.getQuantityString(R.plurals.overview_volunteers_value, volunteers.size, volunteers.size)
        }
        viewModel.recentVolunteers.observe(viewLifecycleOwner) { newestFirst ->
            val recent = newestFirst.take(MAX_RECENT)
            adapter.submitList(recent)
            binding.rvRecentVolunteers.isVisible = recent.isNotEmpty()
            binding.tvEmptyVolunteers.isVisible = recent.isEmpty()
        }
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            val published = activities.count { it.status == Activity.STATUS_PUBLISHED }
            binding.rowActivities.rowSupporting.text = getString(
                R.string.overview_activities_value, published, activities.size - published
            )
        }
        viewModel.totalSignups.observe(viewLifecycleOwner) {
            binding.rowSignups.rowSupporting.text =
                resources.getQuantityString(R.plurals.overview_signups_value, it, it)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val MAX_RECENT = 3
    }
}
