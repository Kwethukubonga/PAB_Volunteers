package com.kantu.pab_volunteers.ui.volunteer.activities

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
import com.kantu.pab_volunteers.databinding.FragmentActivitiesBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel

/** Opportunities the volunteer has not joined yet. */
class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActivityAdapter(emptyList()) { activity ->
            VolunteerNavGraph.toActivityDetails(findNavController(), activity.id)
        }
        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.openActivities.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
            binding.rvActivities.isVisible = rows.isNotEmpty()
            binding.layoutEmpty.isVisible = rows.isEmpty()
            binding.tvSubtitle.text = resources.getQuantityString(
                R.plurals.opportunities_open, rows.size, rows.size
            )
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
