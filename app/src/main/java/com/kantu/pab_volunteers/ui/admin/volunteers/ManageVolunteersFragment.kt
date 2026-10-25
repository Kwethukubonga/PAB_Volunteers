package com.kantu.pab_volunteers.ui.admin.volunteers

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
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.FragmentManageVolunteersBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.EmptyState
import com.kantu.pab_volunteers.utils.ListLayout
import com.kantu.pab_volunteers.utils.Search
import com.kantu.pab_volunteers.utils.observeMessages

/** A directory of registered volunteers. Viewing only: nobody can be removed from here. */
class ManageVolunteersFragment : Fragment() {

    private var _binding: FragmentManageVolunteersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: VolunteerAdapter
    private lateinit var emptyState: EmptyState
    private var volunteers: List<User> = emptyList()
    private var query = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageVolunteersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = VolunteerAdapter(emptyList()) { user ->
            AdminNavGraph.toVolunteerDetails(findNavController(), user.uid)
        }
        binding.rvItems.layoutManager = ListLayout.forCards(requireContext())
        binding.rvItems.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.search.etSearch.hint = getString(R.string.search_volunteers_hint)
        binding.search.etSearch.doAfterTextChanged {
            query = it?.toString().orEmpty()
            render()
        }

        emptyState = EmptyState(binding.layoutEmpty, R.drawable.ic_admin_volunteers)
        viewModel.listsFailed.observe(viewLifecycleOwner) { render() }
        viewModel.volunteers.observe(viewLifecycleOwner) {
            volunteers = it
            render()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun render() {
        if (_binding == null) return
        val shown = volunteers.filter { Search.matches(it, query) }
        adapter.submitList(shown)
        binding.rvItems.isVisible = shown.isNotEmpty()
        binding.tvSubtitle.text = resources.getQuantityString(
            R.plurals.registered_volunteers, volunteers.size, volunteers.size
        )
        val failed = viewModel.listsFailed.value
        when {
            shown.isNotEmpty() -> emptyState.hide()
            // Nothing matched the search, as opposed to nobody registered yet.
            volunteers.isNotEmpty() -> emptyState.showEmpty(
                getString(R.string.search_no_volunteers, query.trim()), R.string.action_clear_search
            ) { binding.search.etSearch.text?.clear() }
            failed != null -> emptyState.showFailed(failed) { viewModel.refresh() }
            else -> emptyState.showEmpty(getString(R.string.empty_no_volunteers))
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
