package com.kantu.pab_volunteers.ui.volunteer.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentScheduleBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityAdapter
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityRow
import com.kantu.pab_volunteers.utils.EmptyState
import com.kantu.pab_volunteers.utils.ListLayout
import com.kantu.pab_volunteers.utils.observeMessages

/** The volunteer's own places, split into the ones still to come and the ones already done. */
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter
    private lateinit var emptyState: EmptyState

    private var selectedTab = TAB_UPCOMING
    private var upcomingRows: List<ActivityRow> = emptyList()
    private var completedRows: List<ActivityRow> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = ActivityAdapter(emptyList()) { activity ->
            VolunteerNavGraph.toActivityDetails(findNavController(), activity.id)
        }
        binding.rvSchedule.layoutManager = ListLayout.forCards(requireContext())
        binding.rvSchedule.adapter = adapter
        emptyState = EmptyState(binding.layoutEmpty, R.drawable.ic_schedule)

        setUpTabs()
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.mySchedule.observe(viewLifecycleOwner) { rows ->
            upcomingRows = rows
            render()
        }
        viewModel.scheduleCompleted.observe(viewLifecycleOwner) { rows ->
            completedRows = rows
            render()
        }
        viewModel.loadFailed.observe(viewLifecycleOwner) { render() }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun setUpTabs() {
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_upcoming))
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_completed))
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position
                render()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        // Same reason as on Activities: keep the highlighted tab matched to the list shown.
        binding.tabs.getTabAt(selectedTab)?.select()
    }

    private fun render() {
        if (_binding == null) return

        val showingUpcoming = selectedTab == TAB_UPCOMING
        val rows = if (showingUpcoming) upcomingRows else completedRows
        val failed = viewModel.loadFailed.value

        adapter.submitList(rows)
        binding.rvSchedule.isVisible = rows.isNotEmpty()
        when {
            rows.isNotEmpty() -> emptyState.hide()
            failed != null -> emptyState.showFailed(failed) { viewModel.refresh() }
            // Nothing booked yet: straight to where there is something to join.
            showingUpcoming -> emptyState.showEmpty(getString(R.string.empty_no_schedule), R.string.action_browse_activities) {
                (activity as? VolunteerMainActivity)?.selectTab(R.id.activitiesFragment)
            }
            else -> emptyState.showEmpty(getString(R.string.empty_nothing_completed))
        }
        binding.tvSubtitle.text =
            getString(R.string.schedule_summary, upcomingRows.size, completedRows.size)
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
        const val TAB_UPCOMING = 0
    }
}
