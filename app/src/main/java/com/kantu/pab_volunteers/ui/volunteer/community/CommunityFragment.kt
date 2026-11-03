package com.kantu.pab_volunteers.ui.volunteer.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.firebase.FirebaseAuthManager
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.FragmentCommunityBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.EmptyState
import com.kantu.pab_volunteers.utils.ListLayout
import com.kantu.pab_volunteers.utils.observeMessages

/** Announcements from the Philisa team. */
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var emptyState: EmptyState
    private var announcements: List<Announcement> = emptyList()
    // Months folded away; null until the first load, when all but the newest start folded.
    private var collapsedMonths: MutableSet<String>? = null

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

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        adapter = AnnouncementAdapter(
            items = emptyList(),
            currentUserId = FirebaseAuthManager.currentUser?.uid.orEmpty(),
            onThumbsUp = { announcement, thumbsUp ->
                viewModel.toggleThumbsUp(announcement, thumbsUp)
            },
            onMonthToggled = { key -> toggleMonth(key) }
        ) { announcement ->
            VolunteerNavGraph.toAnnouncementDetails(findNavController(), announcement.id)
        }
        binding.rvAnnouncements.layoutManager = ListLayout.forCards(requireContext()).also { layout ->
            // Month headings run across both columns on a tablet.
            (layout as? GridLayoutManager)?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = if (adapter.isMonth(position)) layout.spanCount else 1
            }
        }
        binding.rvAnnouncements.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        emptyState = EmptyState(binding.layoutEmpty, R.drawable.ic_community)
        viewModel.announcements.observe(viewLifecycleOwner) {
            announcements = it
            if (collapsedMonths == null && it.isNotEmpty()) {
                collapsedMonths = AnnouncementMonths.startCollapsed(it).toMutableSet()
            }
            showRows()
            binding.tvSubtitle.text = resources.getQuantityString(
                R.plurals.announcements_count, announcements.size, announcements.size
            )
            render()
        }
        viewModel.loadFailed.observe(viewLifecycleOwner) { render() }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun toggleMonth(key: String) {
        val collapsed = collapsedMonths ?: mutableSetOf<String>().also { collapsedMonths = it }
        if (!collapsed.remove(key)) collapsed.add(key)
        showRows()
    }

    private fun showRows() {
        adapter.submitList(
            AnnouncementMonths.rows(announcements, collapsedMonths.orEmpty()),
            FirebaseAuthManager.currentUser?.uid.orEmpty()
        )
    }

    private fun render() {
        if (_binding == null) return
        binding.rvAnnouncements.isVisible = announcements.isNotEmpty()
        val failed = viewModel.loadFailed.value
        when {
            announcements.isNotEmpty() -> emptyState.hide()
            failed != null -> emptyState.showFailed(failed) { viewModel.refresh() }
            else -> emptyState.showEmpty(getString(R.string.empty_no_announcements))
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
