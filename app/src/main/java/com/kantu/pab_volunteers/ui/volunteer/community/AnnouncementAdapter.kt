package com.kantu.pab_volunteers.ui.volunteer.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.data.model.Announcement
import com.kantu.pab_volunteers.databinding.ItemAnnouncementBinding
import com.kantu.pab_volunteers.databinding.ItemMonthHeaderBinding
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.diffUpdate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Announcements under month headings that fold away. The thumbs up only shows where a handler
 * is given.
 */
// onClick stays last so the existing trailing-lambda call sites keep working.
class AnnouncementAdapter(
    private var items: List<CommunityRow>,
    private var currentUserId: String = "",
    private val onThumbsUp: ((Announcement, Boolean) -> Unit)? = null,
    private val onMonthToggled: (String) -> Unit = {},
    private val onClick: (Announcement) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class PostHolder(val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root)

    inner class MonthHolder(val binding: ItemMonthHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    /** True for a month heading, which spans the full width when cards sit two abreast. */
    fun isMonth(position: Int): Boolean = items.getOrNull(position) is CommunityRow.Month

    override fun getItemViewType(position: Int): Int = if (isMonth(position)) TYPE_MONTH else TYPE_POST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_MONTH) {
            MonthHolder(ItemMonthHeaderBinding.inflate(inflater, parent, false))
        } else {
            PostHolder(ItemAnnouncementBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is CommunityRow.Month -> bindMonth(holder as MonthHolder, row)
            is CommunityRow.Post -> bindPost(holder as PostHolder, row.announcement)
        }
    }

    private fun bindMonth(holder: MonthHolder, month: CommunityRow.Month) {
        val context = holder.itemView.context
        val name = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(month.start))
        val count = context.resources.getQuantityString(R.plurals.community_month_count, month.count, month.count)
        holder.binding.tvMonth.text = name
        holder.binding.tvCount.text = count
        // Down while the month is open, pointing on while it is folded away.
        holder.binding.ivChevron.rotation = if (month.collapsed) 0f else 90f
        holder.itemView.contentDescription = "$name, $count"
        ViewCompat.setStateDescription(
            holder.itemView,
            context.getString(if (month.collapsed) R.string.community_month_collapsed else R.string.community_month_expanded)
        )
        holder.itemView.setOnClickListener { onMonthToggled(month.key) }
    }

    private fun bindPost(holder: PostHolder, announcement: Announcement) {
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
            val row = (items[index] as? CommunityRow.Post)?.announcement ?: return@setOnClickListener
            val nowThumbedUp = !row.isThumbedUpBy(currentUserId)

            // Flip straight away so the tap feels instant, the write happens behind it.
            val updated = if (nowThumbedUp) {
                row.thumbsUpBy + currentUserId
            } else {
                row.thumbsUpBy - currentUserId
            }
            items = items.toMutableList().also { list ->
                list[index] = CommunityRow.Post(row.copy(thumbsUpBy = updated))
            }
            bindThumbsUp(holder, nowThumbedUp, updated.size)
            onThumbsUp?.invoke(row, nowThumbedUp)
        }

        holder.itemView.setOnClickListener { onClick(announcement) }
    }

    private fun bindThumbsUp(holder: PostHolder, thumbedUp: Boolean, count: Int) {
        holder.binding.ivThumbsUp.setImageResource(
            if (thumbedUp) R.drawable.ic_thumb_up else R.drawable.ic_thumb_up_outline
        )
        holder.binding.tvThumbsUpCount.text = count.toString()
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<CommunityRow>, userId: String = currentUserId) {
        val old = items
        items = newItems
        diffUpdate(old, newItems) { a, b -> rowKey(a) == rowKey(b) }
        if (userId != currentUserId) {
            // Someone else signed in: every thumbs up may read differently.
            currentUserId = userId
            notifyItemRangeChanged(0, items.size)
        }
    }

    private fun rowKey(row: CommunityRow): String = when (row) {
        is CommunityRow.Month -> "month:${row.key}"
        is CommunityRow.Post -> row.announcement.id
    }

    private companion object {
        const val TYPE_POST = 0
        const val TYPE_MONTH = 1
    }
}
