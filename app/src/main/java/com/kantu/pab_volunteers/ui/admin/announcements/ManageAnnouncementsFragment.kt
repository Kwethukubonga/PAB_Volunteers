package com.kantu.pab_volunteers.ui.admin.announcements

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
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.FragmentManageAnnouncementsBinding
import com.kantu.pab_volunteers.navigation.AdminNavGraph
import com.kantu.pab_volunteers.ui.admin.AdminViewModel
import com.kantu.pab_volunteers.utils.observeMessages

class ManageAnnouncementsFragment : Fragment() {

    private var _binding: FragmentManageAnnouncementsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    private lateinit var adapter: ManageAnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageAnnouncementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = ManageAnnouncementAdapter(
            items = emptyList(),
            onTogglePublish = { viewModel.toggleAnnouncementPublished(it) },
            onEdit = { AdminNavGraph.toAnnouncementEditor(findNavController(), it.id) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        binding.btnAdd.setOnClickListener {
            AdminNavGraph.toAnnouncementEditor(findNavController())
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.announcements.observe(viewLifecycleOwner) { announcements ->
            adapter.submitList(announcements)
            binding.rvItems.isVisible = announcements.isNotEmpty()
            binding.layoutEmpty.isVisible = announcements.isEmpty()
            val published = announcements.count { it.status == Announcement.STATUS_PUBLISHED }
            binding.tvSubtitle.text = getString(
                R.string.published_drafts_summary, published, announcements.size - published
            )
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun confirmDelete(announcement: Announcement) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_announcement_confirm_title)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                viewModel.deleteAnnouncement(announcement.id)
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
