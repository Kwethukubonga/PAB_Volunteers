package com.kantu.pab_volunteers.ui.volunteer.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.ItemAnnouncementBinding
import com.kantu.pab_volunteers.utils.DateUtils

/** One row per announcement. The thumbs up only shows where a handler is given. */
// onClick stays last so the existing trailing-lambda call sites keep working.
class AnnouncementAdapter(
    private var items: List<Announcement>,
    private var currentUserId: String = "",
    private val onThumbsUp: ((Announcement, Boolean) -> Unit)? = null,
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

        holder.binding.ivImage.isVisible = announcement.hasImage
        if (announcement.hasImage) {
            Glide.with(holder.itemView)
                .load(announcement.imageUrl)
                .centerCrop()
                .into(holder.binding.ivImage)
        }

        holder.binding.layoutThumbsUp.isVisible = onThumbsUp != null
        bindThumbsUp(holder, announcement.isThumbedUpBy(currentUserId), announcement.thumbsUpCount)
        holder.binding.layoutThumbsUp.setOnClickListener {
            val index = holder.bindingAdapterPosition
            if (index == RecyclerView.NO_POSITION) return@setOnClickListener
            val row = items[index]
            val nowThumbedUp = !row.isThumbedUpBy(currentUserId)

            // Flip straight away so the tap feels instant, the write happens behind it.
            val updated = if (nowThumbedUp) {
                row.thumbsUpBy + currentUserId
            } else {
                row.thumbsUpBy - currentUserId
            }
            items = items.toMutableList().also { list ->
                list[index] = row.copy(thumbsUpBy = updated)
            }
            bindThumbsUp(holder, nowThumbedUp, updated.size)
            onThumbsUp?.invoke(row, nowThumbedUp)
        }

        holder.itemView.setOnClickListener { onClick(announcement) }
    }

    private fun bindThumbsUp(holder: ViewHolder, thumbedUp: Boolean, count: Int) {
        holder.binding.ivThumbsUp.setImageResource(
            if (thumbedUp) R.drawable.ic_thumb_up else R.drawable.ic_thumb_up_outline
        )
        holder.binding.tvThumbsUpCount.text = count.toString()
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Announcement>, userId: String = currentUserId) {
        items = newItems
        currentUserId = userId
        notifyDataSetChanged()
    }
}
