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
import com.google.android.material.tabs.TabLayout
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentActivitiesBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.observeMessages

/** Opportunities to join, with a second tab for the ones a volunteer has saved. */
class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter

    private var showingFavourites = false
    private var openRows: List<ActivityRow> = emptyList()
    private var favouriteRows: List<ActivityRow> = emptyList()

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

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = ActivityAdapter(
            items = emptyList(),
            onClick = { activity ->
                VolunteerNavGraph.toActivityDetails(findNavController(), activity.id)
            },
            onFavouriteToggled = { activity, favourite ->
                viewModel.setFavourite(activity.id, favourite)
            }
        )
        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = adapter

        setUpTabs()

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.openActivities.observe(viewLifecycleOwner) { rows ->
            openRows = rows
            render()
        }
        viewModel.favourites.observe(viewLifecycleOwner) { rows ->
            favouriteRows = rows
            render()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun setUpTabs() {
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_all_opportunities))
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_favourites))
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showingFavourites = tab.position == 1
                render()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun render() {
        if (_binding == null) return
        val rows = if (showingFavourites) favouriteRows else openRows
        adapter.submitList(rows)
        binding.rvActivities.isVisible = rows.isNotEmpty()
        binding.layoutEmpty.isVisible = rows.isEmpty()

        if (showingFavourites) {
            binding.tvSubtitle.text =
                resources.getQuantityString(R.plurals.favourites_saved, rows.size, rows.size)
            binding.tvEmptyMessage.setText(R.string.empty_favourites)
        } else {
            binding.tvSubtitle.text =
                resources.getQuantityString(R.plurals.opportunities_open, rows.size, rows.size)
            binding.tvEmptyMessage.setText(R.string.empty_no_open_activities)
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
