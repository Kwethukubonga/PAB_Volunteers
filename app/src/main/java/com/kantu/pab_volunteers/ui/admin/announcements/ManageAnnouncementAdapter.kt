package com.kantu.pab_volunteers.ui.admin.announcements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.ItemAdminAnnouncementBinding
import com.kantu.pab_volunteers.utils.DateUtils

class ManageAnnouncementAdapter(
    private var items: List<Announcement>,
    private val onTogglePublish: (Announcement) -> Unit,
    private val onEdit: (Announcement) -> Unit,
    private val onDelete: (Announcement) -> Unit
) : RecyclerView.Adapter<ManageAnnouncementAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAdminAnnouncementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = items[position]
        val context = holder.itemView.context

        holder.binding.tvTitle.text = announcement.title
        holder.binding.tvBody.text = announcement.messageBody
        holder.binding.tvDate.text = DateUtils.formatDate(announcement.date)

        holder.binding.tvThumbsUpCount.text =
            context.getString(R.string.thumbs_up_summary, announcement.thumbsUpCount)

        holder.binding.ivImage.isVisible = announcement.hasImage
        if (announcement.hasImage) {
            Glide.with(holder.itemView)
                .load(announcement.imageUrl)
                .centerCrop()
                .into(holder.binding.ivImage)
        }

        val published = announcement.status == Announcement.STATUS_PUBLISHED
        val (labelRes, bgRes, textRes) = if (published) {
            Triple(R.string.status_published, R.color.status_success_bg, R.color.status_success_text)
        } else {
            Triple(R.string.status_draft, R.color.status_neutral_bg, R.color.status_neutral_text)
        }
        holder.binding.tvStatus.setText(labelRes)
        holder.binding.tvStatus.background?.setTint(ContextCompat.getColor(context, bgRes))
        holder.binding.tvStatus.setTextColor(ContextCompat.getColor(context, textRes))

        holder.binding.btnTogglePublish.setText(
            if (published) R.string.action_unpublish else R.string.action_publish
        )

        holder.binding.btnTogglePublish.setOnClickListener { onTogglePublish(announcement) }
        holder.binding.btnEdit.setOnClickListener { onEdit(announcement) }
        holder.binding.btnDelete.setOnClickListener { onDelete(announcement) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Announcement>) {
        items = newItems
        notifyDataSetChanged()
    }
}
