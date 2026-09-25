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
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentScheduleBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityAdapter

/** The activities this volunteer has a place on, soonest first. */
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter

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

        adapter = ActivityAdapter(emptyList()) { activity ->
            VolunteerNavGraph.toActivityDetails(findNavController(), activity.id)
        }
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedule.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.mySchedule.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
            binding.rvSchedule.isVisible = rows.isNotEmpty()
            binding.layoutEmpty.isVisible = rows.isEmpty()
        }
        viewModel.todayCount.observe(viewLifecycleOwner) { today ->
            val upcoming = viewModel.upcomingCount.value ?: 0
            binding.tvSubtitle.text = getString(R.string.schedule_summary, today, upcoming)
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
}
