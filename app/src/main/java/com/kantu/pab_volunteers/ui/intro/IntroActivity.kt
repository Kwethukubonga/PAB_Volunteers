package com.kantu.pab_volunteers.ui.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kantu.pab_volunteers.databinding.ActivityIntroBinding
import com.kantu.pab_volunteers.databinding.ItemIntroPageBinding
import com.kantu.pab_volunteers.navigation.AppNavGraph

/**
 * Three pages explaining what the app is for, shown once before the Welcome screen and
 * again whenever someone asks for it from Settings, About (K8).
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    /** Opened from Settings it goes back there; opened on first run it carries on to Welcome. */
    private val askedFor: Boolean get() = intent.getBooleanExtra(EXTRA_ASKED_FOR, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = PagesAdapter()
        TabLayoutMediator(binding.dots, binding.pager) { _, _ -> }.attach()

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = showButtonsFor(position)
        })
        showButtonsFor(0)

        binding.btnSkip.setOnClickListener { done() }
        binding.btnForward.setOnClickListener {
            val page = binding.pager.currentItem
            if (IntroPages.isLast(page)) done() else binding.pager.currentItem = IntroPages.pageAfter(page)
        }
    }

    private fun showButtonsFor(page: Int) {
        binding.btnForward.setText(IntroPages.forwardLabel(page))
        binding.btnSkip.isVisible = IntroPages.showSkip(page)
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
            holder.binding.ivIcon.setImageResource(page.icon)
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
