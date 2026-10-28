package com.kantu.pab_volunteers.ui.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.databinding.ActivityIntroBinding
import com.kantu.pab_volunteers.databinding.ItemIntroPageBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph

/**
 * Three pages explaining what the app is for, shown once before the Welcome screen and
 * again whenever someone asks for it from Settings, About (K8). Each page is one of
 * Philisa's own photographs, laid out the way the Welcome screen is.
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val dots = mutableListOf<View>()

    /** Opened from Settings it goes back there; opened on first run it carries on to Welcome. */
    private val askedFor: Boolean get() = intent.getBooleanExtra(EXTRA_ASKED_FOR, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = PagesAdapter()
        addDots()

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = showPage(position)
        })
        showPage(0)

        binding.btnSkip.setOnClickListener { done() }
        binding.btnForward.setOnClickListener {
            val page = binding.pager.currentItem
            if (IntroPages.isLast(page)) done() else binding.pager.currentItem = IntroPages.pageAfter(page)
        }
    }

    /** One dot per page, built from the pages themselves so the two cannot drift apart. */
    private fun addDots() {
        val size = resources.getDimensionPixelSize(R.dimen.intro_dot_size)
        val gap = resources.getDimensionPixelSize(R.dimen.intro_dot_gap)
        IntroPages.pages.indices.forEach { _ ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = gap
                    marginEnd = gap
                }
                setBackgroundResource(R.drawable.intro_dot)
            }
            dots += dot
            binding.dots.addView(dot)
        }
    }

    private fun showPage(page: Int) {
        binding.btnForward.setText(IntroPages.forwardLabel(page))
        binding.btnSkip.isVisible = IntroPages.showSkip(page)
        dots.forEachIndexed { index, dot -> dot.isSelected = index == page }
    }

    private fun done() {
        IntroPreference.markSeen(this)
        if (askedFor) finish() else AppNavGraph.goToWelcome(this)
    }

    private class PagesAdapter : RecyclerView.Adapter<PagesAdapter.PageHolder>() {

        class PageHolder(val binding: ItemIntroPageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PageHolder(
            ItemIntroPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun getItemCount(): Int = IntroPages.pages.size

        override fun onBindViewHolder(holder: PageHolder, position: Int) {
            val page = IntroPages.pages[position]
            holder.binding.ivPhoto.setImageResource(page.photo)
            holder.binding.tvTitle.setText(page.title)
            holder.binding.tvBody.setText(page.body)
        }
    }

    companion object {

        private const val EXTRA_ASKED_FOR = "asked_for"

        /** From Settings: show it again and come back here afterwards. */
        fun againFrom(context: Context): Intent =
            Intent(context, IntroActivity::class.java).putExtra(EXTRA_ASKED_FOR, true)
    }
}
