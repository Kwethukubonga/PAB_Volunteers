package com.kantu.pab_volunteers.ui.volunteer.home

import android.os.Bundle
import android.text.format.DateUtils as AndroidDateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Activity
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.data.model.User
import com.kantu.pab_volunteers.databinding.FragmentHomeBinding
import com.kantu.pab_volunteers.databinding.ItemDayPillBinding
import com.kantu.pab_volunteers.databinding.ItemPlanRowBinding
import com.kantu.pab_volunteers.navigation.VolunteerNavGraph
import com.kantu.pab_volunteers.ui.volunteer.VolunteerMainActivity
import com.kantu.pab_volunteers.ui.volunteer.VolunteerViewModel
import com.kantu.pab_volunteers.utils.AppLanguage
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.observeMessages
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Home as a planner (K9): the next activity, the coming week with a dot on each booked day, the
 * plan for the day picked, one summary line in place of the four stat tiles (E7), and the latest
 * announcement.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerViewModel by activityViewModels()

    private var booked: List<Activity> = emptyList()
    private var selectedDay = DateUtils.startOfDay(DateUtils.now())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeMessages(viewModel.message) { viewModel.consumeMessage() }

        binding.tvDate.text = dayFormat("EEEE d MMMM").format(Date(DateUtils.now()))
        binding.tvSummary.setOnClickListener { selectTab(R.id.scheduleFragment) }
        binding.btnSeeCommunity.setOnClickListener { selectTab(R.id.communityFragment) }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.user.observe(viewLifecycleOwner) { user -> user?.let { bindGreeting(it) } }
        viewModel.mySchedule.observe(viewLifecycleOwner) { rows ->
            booked = rows.map { it.activity }
            renderPlan()
        }
        viewModel.upcomingCount.observe(viewLifecycleOwner) { renderSummary() }
        viewModel.hoursCompleted.observe(viewLifecycleOwner) { renderSummary() }
        viewModel.announcements.observe(viewLifecycleOwner) { renderLatest(it.firstOrNull()) }
        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
    }

    private fun bindGreeting(user: User) {
        binding.tvGreeting.text = getString(greetingForNow(), user.firstName)
    }

    private fun renderPlan() {
        if (_binding == null) return
        val now = DateUtils.now()
        renderNext(WeekPlan.next(booked, now), now)
        renderWeek(now)
        renderDay(now)
    }

    /** The big card: the next activity, or a nudge to find one when nothing is booked. */
    private fun renderNext(next: Activity?, now: Long) {
        if (next == null) {
            binding.tvNextWhen.setText(R.string.home_nothing_booked_label)
            binding.tvNextTitle.setText(R.string.home_nothing_booked)
            binding.tvNextTime.setText(R.string.home_find_something)
            binding.tvNextTime.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            binding.tvNextPlace.isVisible = false
            binding.cardNext.setOnClickListener { selectTab(R.id.activitiesFragment) }
        } else {
            binding.tvNextWhen.text = getString(R.string.home_next_up, dayLabel(next.dateMillis, now))
            binding.tvNextTitle.text = next.title
            binding.tvNextTime.text = getString(R.string.time_range, next.startTime, next.endTime)
            binding.tvNextTime.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_schedule_small, 0, 0, 0)
            binding.tvNextPlace.isVisible = next.location.isNotBlank()
            binding.tvNextPlace.text = next.location
            binding.cardNext.setOnClickListener {
                VolunteerNavGraph.toActivityDetails(findNavController(), next.id)
            }
        }
        binding.cardNext.contentDescription = listOf(
            binding.tvNextWhen.text, binding.tvNextTitle.text, binding.tvNextTime.text,
            if (binding.tvNextPlace.isVisible) binding.tvNextPlace.text else ""
        ).filter { it.isNotBlank() }.joinToString(". ")
    }

    /** Seven tall pills, today first. A dot marks a day with something booked. */
    private fun renderWeek(now: Long) {
        val days = WeekPlan.days(now)
        val withPlans = WeekPlan.daysWithPlans(days, booked)
        if (selectedDay !in days) selectedDay = days.first()

        binding.layoutWeek.removeAllViews()
        days.forEach { day ->
            val pill = ItemDayPillBinding.inflate(layoutInflater, binding.layoutWeek, false)
            val selected = day == selectedDay
            val hasPlans = day in withPlans
            val selectedText = ContextCompat.getColor(requireContext(), R.color.background_cream)
            pill.tvWeekday.text = dayFormat("EEE").format(Date(day))
            pill.tvDayNumber.text = dayFormat("d").format(Date(day))
            pill.dot.visibility = if (hasPlans) View.VISIBLE else View.INVISIBLE
            pill.root.setBackgroundResource(if (selected) R.drawable.bg_day_pill_selected else R.drawable.bg_day_pill)
            if (selected) {
                pill.tvDayNumber.setTextColor(selectedText)
                pill.tvWeekday.setTextColor(selectedText)
                pill.dot.background = pill.dot.background?.mutate()?.apply { setTint(selectedText) }
            }
            pill.root.isSelected = selected
            pill.root.contentDescription = dayFormat("EEEE d MMMM").format(Date(day)) + ". " +
                getString(if (hasPlans) R.string.home_day_booked else R.string.home_day_free)
            pill.root.setOnClickListener {
                selectedDay = day
                renderPlan()
            }
            binding.layoutWeek.addView(pill.root)
        }
    }

    /** What is on the picked day, earliest first. */
    private fun renderDay(now: Long) {
        binding.tvDayHeading.text = when (DateUtils.daysBetween(now, selectedDay)) {
            0 -> getString(R.string.home_day_today)
            1 -> getString(R.string.home_day_tomorrow)
            else -> dayFormat("EEEE d MMMM").format(Date(selectedDay))
        }
        binding.layoutDayPlan.removeAllViews()
        val plans = WeekPlan.plansOn(selectedDay, booked)
        if (plans.isEmpty()) {
            val row = ItemPlanRowBinding.inflate(layoutInflater, binding.layoutDayPlan, false)
            row.tvTitle.setText(R.string.home_day_empty)
            row.tvMeta.setText(R.string.home_find_something)
            row.root.setOnClickListener { selectTab(R.id.activitiesFragment) }
            binding.layoutDayPlan.addView(row.root)
            return
        }
        plans.forEachIndexed { index, activity ->
            if (index > 0) binding.layoutDayPlan.addView(divider())
            val row = ItemPlanRowBinding.inflate(layoutInflater, binding.layoutDayPlan, false)
            row.tvTime.isVisible = true
            row.tvTime.text = activity.startTime
            row.tvTitle.text = activity.title
            row.tvMeta.text = listOf(activity.programme, activity.volunteerRole).filter { it.isNotBlank() }.joinToString(" · ")
            row.root.setOnClickListener { VolunteerNavGraph.toActivityDetails(findNavController(), activity.id) }
            binding.layoutDayPlan.addView(row.root)
        }
    }

    private fun renderSummary() {
        if (_binding == null) return
        val upcoming = viewModel.upcomingCount.value ?: 0
        val hours = viewModel.hoursCompleted.value ?: 0
        binding.tvSummary.text = getString(
            R.string.home_summary, upcoming, resources.getQuantityString(R.plurals.home_hours_completed, hours, hours)
        )
    }

    private fun renderLatest(latest: Announcement?) {
        binding.rowLatest.root.isVisible = latest != null
        binding.tvEmptyAnnouncements.isVisible = latest == null
        if (latest == null) return
        val row = binding.rowLatest
        row.ivTile.isVisible = true
        row.ivTile.setImageResource(R.drawable.ic_community)
        row.ivTile.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent))
        row.tvTitle.text = latest.title
        val posted = latest.publishedAt.takeIf { it > 0 } ?: latest.date
        val ago = AndroidDateUtils.getRelativeTimeSpanString(posted, DateUtils.now(), AndroidDateUtils.MINUTE_IN_MILLIS)
        row.tvMeta.text = getString(
            R.string.home_latest_meta, ago,
            resources.getQuantityString(R.plurals.home_thumbs_up, latest.thumbsUpCount, latest.thumbsUpCount)
        )
        row.root.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card)
        row.root.setOnClickListener { VolunteerNavGraph.toAnnouncementDetails(findNavController(), latest.id) }
    }

    /** "today", "tomorrow" or the weekday, for the card's label. */
    private fun dayLabel(dateMillis: Long, now: Long): String = when (DateUtils.daysBetween(now, dateMillis)) {
        0 -> getString(R.string.relative_today)
        1 -> getString(R.string.relative_tomorrow)
        else -> dayFormat("EEEE").format(Date(dateMillis))
    }

    private fun divider() = View(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.stroke_normal))
        setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
    }

    private fun dayFormat(pattern: String) = SimpleDateFormat(pattern, AppLanguage.locale())

    private fun selectTab(itemId: Int) {
        (requireActivity() as VolunteerMainActivity).selectTab(itemId)
    }

    private fun greetingForNow(): Int = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }

    override fun onResume() {
        super.onResume()
        // A new day may have started while the app was in the background.
        selectedDay = maxOf(selectedDay, DateUtils.startOfDay(DateUtils.now()))
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
