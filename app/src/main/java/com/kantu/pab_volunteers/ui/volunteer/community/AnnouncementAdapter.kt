package com.kantu.pab_volunteers.ui.volunteer.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.ItemAnnouncementBinding
import com.kantu.pab_volunteers.utils.DateUtils

class AnnouncementAdapter(
    private var items: List<Announcement>,
    private val onClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAnnouncementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = items[position]
        holder.binding.tvTitle.text = announcement.title
        holder.binding.tvBody.text = announcement.messageBody
        holder.binding.tvDate.text = DateUtils.formatDate(announcement.date)
        holder.itemView.setOnClickListener { onClick(announcement) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Announcement>) {
        items = newItems
        notifyDataSetChanged()
    }
}
