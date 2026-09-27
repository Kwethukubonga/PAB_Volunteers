package com.kantu.pab_volunteers.ui.admin.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentAdminOverviewBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.navigation.AppNavGraph
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

        binding.statVolunteers.tvStatLabel.setText(R.string.stat_total_volunteers)
        binding.statActivities.tvStatLabel.setText(R.string.stat_published_activities)
        binding.statSignups.tvStatLabel.setText(R.string.stat_total_signups)

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
            binding.actionImpactStats.root,
            R.drawable.ic_admin_overview,
            R.string.action_manage_impact_stats,
            R.string.action_manage_impact_stats_desc
        ) { AdminNavGraph.toImpactStats(findNavController()) }

        binding.tvAdminName.text = getString(
            R.string.signed_in_as,
            FirebaseAuthManager.currentUser?.email.orEmpty()
        )

        binding.btnSignOut.setOnClickListener { confirmSignOut() }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        observeViewModel()
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
            binding.statVolunteers.tvStatValue.text = volunteers.size.toString()
            val recent = volunteers.take(MAX_RECENT)
            adapter.submitList(recent)
            binding.rvRecentVolunteers.isVisible = recent.isNotEmpty()
            binding.tvEmptyVolunteers.isVisible = recent.isEmpty()
        }
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            binding.statActivities.tvStatValue.text =
                activities.count { it.status == Activity.STATUS_PUBLISHED }.toString()
        }
        viewModel.totalSignups.observe(viewLifecycleOwner) {
            binding.statSignups.tvStatValue.text = it.toString()
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
