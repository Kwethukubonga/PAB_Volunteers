package com.kantu.pab_volunteers.ui.volunteer.home

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
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentHomeBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.ui.volunteer.activities.ActivityAdapter
import com.kantu.pab_volunteers.ui.volunteer.community.AnnouncementAdapter
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityAdapter = ActivityAdapter(emptyList()) { activity ->
            VolunteerNavGraph.toActivityDetails(findNavController(), activity.id)
        }
        binding.rvComingUp.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComingUp.adapter = activityAdapter

        announcementAdapter = AnnouncementAdapter(emptyList()) { announcement ->
            VolunteerNavGraph.toAnnouncementDetails(findNavController(), announcement.id)
        }
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnnouncements.adapter = announcementAdapter

        binding.statToday.tvStatLabel.setText(R.string.stat_today)
        binding.statUpcoming.tvStatLabel.setText(R.string.stat_upcoming)
        binding.statCompleted.tvStatLabel.setText(R.string.stat_completed)

        binding.btnSeeSchedule.setOnClickListener {
            findNavController().navigate(R.id.scheduleFragment)
        }
        binding.btnSeeCommunity.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { bindUser(it) } }

        viewModel.mySchedule.observe(viewLifecycleOwner) { rows ->
            val next = rows.take(MAX_PREVIEW)
            activityAdapter.submitList(next)
            binding.rvComingUp.isVisible = next.isNotEmpty()
            binding.tvEmptyComingUp.isVisible = next.isEmpty()
        }

        viewModel.announcements.observe(viewLifecycleOwner) { announcements ->
            val latest = announcements.take(MAX_PREVIEW)
            announcementAdapter.submitList(latest)
            binding.rvAnnouncements.isVisible = latest.isNotEmpty()
            binding.tvEmptyAnnouncements.isVisible = latest.isEmpty()
        }

        viewModel.todayCount.observe(viewLifecycleOwner) {
            binding.statToday.tvStatValue.text = it.toString()
        }
        viewModel.upcomingCount.observe(viewLifecycleOwner) {
            binding.statUpcoming.tvStatValue.text = it.toString()
        }
        viewModel.completedCount.observe(viewLifecycleOwner) {
            binding.statCompleted.tvStatValue.text = it.toString()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it
        }
    }

    private fun bindUser(user: User) {
        binding.tvGreeting.setText(greetingForNow())
        binding.tvUserName.text = user.fullName
        binding.tvVolunteerId.text = getString(R.string.volunteer_id_label, user.volunteerId)
        binding.tvAvatarInitial.text = user.firstName.take(1).uppercase()
    }

    private fun greetingForNow(): Int {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
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

    private companion object {
        const val MAX_PREVIEW = 2
    }
}
