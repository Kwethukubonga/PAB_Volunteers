package com.kantu.pab_volunteers.ui.volunteer.community

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
import com.kantu.pab_volunteers.databinding.FragmentCommunityBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel

/** Announcements from the Philisa team. */
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: AnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AnnouncementAdapter(emptyList()) { announcement ->
            VolunteerNavGraph.toAnnouncementDetails(findNavController(), announcement.id)
        }
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnnouncements.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.announcements.observe(viewLifecycleOwner) { announcements ->
            adapter.submitList(announcements)
            binding.rvAnnouncements.isVisible = announcements.isNotEmpty()
            binding.layoutEmpty.isVisible = announcements.isEmpty()
            binding.tvSubtitle.text = resources.getQuantityString(
                R.plurals.announcements_count, announcements.size, announcements.size
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
