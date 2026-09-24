package com.kantu.pab_volunteers.ui.volunteer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentHomeBinding
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var todaysAdapter: ActivityAdapter
    private lateinit var comingUpAdapter: ActivityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todaysAdapter = ActivityAdapter(emptyList())
        binding.rvTodaysActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodaysActivities.adapter = todaysAdapter

        comingUpAdapter = ActivityAdapter(emptyList())
        binding.rvComingUp.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComingUp.adapter = comingUpAdapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadDashboard() }

        observeViewModel()
        viewModel.loadDashboard()
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileInitial.text = user.firstName.take(1).uppercase()
                binding.tvUserName.text = user.fullName
                binding.tvVolunteerId.text = getString(R.string.volunteer_id_label, user.volunteerId)
            }
        }
        viewModel.todaysActivities.observe(viewLifecycleOwner) { activities ->
            todaysAdapter.submitList(activities)
            binding.tvEmptyToday.isVisible = activities.isEmpty()
            binding.tvActivitiesToday.text = getString(R.string.activities_scheduled_today, activities.size)
        }
        viewModel.comingUpActivities.observe(viewLifecycleOwner) { activities ->
            comingUpAdapter.submitList(activities)
        }
        viewModel.upcomingCount.observe(viewLifecycleOwner) { count ->
            binding.statAssigned.tvStatValue.text = count.toString()
            binding.statAssigned.tvStatLabel.text = getString(R.string.stat_assigned)
        }
        viewModel.todayCount.observe(viewLifecycleOwner) { count ->
            binding.statToday.tvStatValue.text = count.toString()
            binding.statToday.tvStatLabel.text = getString(R.string.stat_today)
        }
        viewModel.pastCount.observe(viewLifecycleOwner) { count ->
            binding.statCompleted.tvStatValue.text = count.toString()
            binding.statCompleted.tvStatLabel.text = getString(R.string.stat_completed)
        }
        viewModel.impactStats.observe(viewLifecycleOwner) { stats ->
            binding.tvImpactBanner.text = getString(R.string.impact_banner_text, stats?.familiesFed ?: 0)
            binding.statFamiliesFed.tvStatValue.text = (stats?.familiesFed ?: 0).toString()
            binding.statFamiliesFed.tvStatLabel.text = getString(R.string.stat_families_fed)
            binding.statYouthMentored.tvStatValue.text = (stats?.youthMentored ?: 0).toString()
            binding.statYouthMentored.tvStatLabel.text = getString(R.string.stat_youth_mentored)
            binding.statSafeHouseIntakes.tvStatValue.text = (stats?.safeHouseIntakes ?: 0).toString()
            binding.statSafeHouseIntakes.tvStatLabel.text = getString(R.string.stat_safe_house_intakes)
            binding.statSeniorsVisited.tvStatValue.text = (stats?.seniorsVisited ?: 0).toString()
            binding.statSeniorsVisited.tvStatLabel.text = getString(R.string.stat_seniors_visited)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
