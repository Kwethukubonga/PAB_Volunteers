package com.kantu.pab_volunteers.ui.admin.volunteers

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
import com.kantu.pab_volunteers.databinding.FragmentManageVolunteersBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.ui.admin.AdminViewModel

/** A directory of registered volunteers. Viewing only: nobody can be removed from here. */
class ManageVolunteersFragment : Fragment() {

    private var _binding: FragmentManageVolunteersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: VolunteerAdapter

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

        adapter = VolunteerAdapter(emptyList()) { user ->
            AdminNavGraph.toVolunteerDetails(findNavController(), user.uid)
        }
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.volunteers.observe(viewLifecycleOwner) { volunteers ->
            adapter.submitList(volunteers)
            binding.rvItems.isVisible = volunteers.isNotEmpty()
            binding.layoutEmpty.isVisible = volunteers.isEmpty()
            binding.tvSubtitle.text = resources.getQuantityString(
                R.plurals.registered_volunteers, volunteers.size, volunteers.size
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
