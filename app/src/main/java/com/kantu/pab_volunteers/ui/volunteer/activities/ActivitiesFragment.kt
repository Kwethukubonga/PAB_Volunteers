package com.kantu.pab_volunteers.ui.volunteer.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentActivitiesBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.EmptyState
import com.kantu.pab_volunteers.utils.ListLayout
import com.kantu.pab_volunteers.utils.Search
import com.kantu.pab_volunteers.utils.observeMessages

/** Opportunities to join, with a second tab for the ones a volunteer has saved. */
class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: ActivityAdapter
    private lateinit var emptyState: EmptyState

    private var showingFavourites = false
    private var openRows: List<ActivityRow> = emptyList()
    private var favouriteRows: List<ActivityRow> = emptyList()
    private var query = ""

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
        binding.rvActivities.layoutManager = ListLayout.forCards(requireContext())
        binding.rvActivities.adapter = adapter

        setUpTabs()

        binding.search.etSearch.hint = getString(R.string.search_activities_hint)
        emptyState = EmptyState(binding.layoutEmpty, R.drawable.ic_activities)
        binding.search.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty()
            render()
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.openActivities.observe(viewLifecycleOwner) { rows ->
            openRows = rows
            render()
        }
        viewModel.favourites.observe(viewLifecycleOwner) { rows ->
            favouriteRows = rows
            render()
        }
        viewModel.loadFailed.observe(viewLifecycleOwner) { render() }
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
        // The fragment outlives its view when a detail screen is opened, so re-select the tab
        // it was on. Otherwise the first tab lights up over the other tab's list.
        binding.tabs.getTabAt(if (showingFavourites) 1 else 0)?.select()
    }

    private fun render() {
        if (_binding == null) return
        val allRows = if (showingFavourites) favouriteRows else openRows
        val rows = allRows.filter { Search.matches(it.activity, query) }
        adapter.submitList(rows)
        binding.rvActivities.isVisible = rows.isNotEmpty()
        binding.tvSubtitle.text = if (showingFavourites) {
            resources.getQuantityString(R.plurals.favourites_saved, allRows.size, allRows.size)
        } else {
            resources.getQuantityString(R.plurals.opportunities_open, allRows.size, allRows.size)
        }

        val failed = viewModel.loadFailed.value
        when {
            rows.isNotEmpty() -> emptyState.hide()
            // Nothing matched the search, as opposed to nothing there at all.
            allRows.isNotEmpty() -> emptyState.showEmpty(
                getString(R.string.search_no_activities, query.trim()), R.string.action_clear_search
            ) { binding.search.etSearch.text?.clear() }
            failed != null -> emptyState.showFailed(failed) { viewModel.refresh() }
            showingFavourites -> emptyState.showEmpty(getString(R.string.empty_favourites), R.string.action_find_opportunities) {
                binding.tabs.getTabAt(0)?.select()
            }
            else -> emptyState.showEmpty(getString(R.string.empty_no_open_activities))
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
