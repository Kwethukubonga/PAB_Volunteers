package com.kantu.pab_volunteers.ui.admin.activities

import android.app.AlertDialog
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
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.databinding.FragmentManageActivitiesBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.observeMessages

class ManageActivitiesFragment : Fragment() {

    private var _binding: FragmentManageActivitiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: ManageActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = ManageActivityAdapter(
            items = emptyList(),
            onViewSignups = { AdminNavGraph.toActivitySignups(findNavController(), it.id) },
            onTogglePublish = { viewModel.toggleActivityPublished(it) },
            onEdit = { AdminNavGraph.toActivityEditor(findNavController(), it.id) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        binding.btnAdd.setOnClickListener {
            AdminNavGraph.toActivityEditor(findNavController())
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            adapter.submitList(activities)
            binding.rvItems.isVisible = activities.isNotEmpty()
            binding.layoutEmpty.isVisible = activities.isEmpty()
            val published = activities.count { it.status == Activity.STATUS_PUBLISHED }
            binding.tvSubtitle.text = getString(
                R.string.published_drafts_summary, published, activities.size - published
            )
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun confirmDelete(activity: Activity) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_activity_confirm_title)
            .setMessage(R.string.delete_activity_confirm_message)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                viewModel.deleteActivity(activity.id)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
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
