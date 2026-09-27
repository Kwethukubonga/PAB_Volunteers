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
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityAdapter
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityRow
import com.kantu.pab_volunteers.utils.observeMessages

/** The volunteer's own places, split into today, upcoming and completed. */
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter

    private var selectedTab = TAB_TODAY
    private var todayRows: List<ActivityRow> = emptyList()
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
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedule.adapter = adapter

        setUpTabs()
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.scheduleToday.observe(viewLifecycleOwner) { rows ->
            todayRows = rows
            render()
        }
        viewModel.mySchedule.observe(viewLifecycleOwner) { rows ->
            upcomingRows = rows
            render()
        }
        viewModel.scheduleCompleted.observe(viewLifecycleOwner) { rows ->
            completedRows = rows
            render()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun setUpTabs() {
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_today))
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
    }

    private fun render() {
        if (_binding == null) return

        val rows = when (selectedTab) {
            TAB_TODAY -> todayRows
            TAB_UPCOMING -> upcomingRows
            else -> completedRows
        }
        val emptyMessage = when (selectedTab) {
            TAB_TODAY -> R.string.empty_nothing_today
            TAB_UPCOMING -> R.string.empty_no_schedule
            else -> R.string.empty_nothing_completed
        }

        adapter.submitList(rows)
        binding.rvSchedule.isVisible = rows.isNotEmpty()
        binding.layoutEmpty.isVisible = rows.isEmpty()
        binding.tvEmptyMessage.setText(emptyMessage)
        binding.tvSubtitle.text =
            getString(R.string.schedule_summary, todayRows.size, upcomingRows.size)
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
        const val TAB_TODAY = 0
        const val TAB_UPCOMING = 1
    }
}
