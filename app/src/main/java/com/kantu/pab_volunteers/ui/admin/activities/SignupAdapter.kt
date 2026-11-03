package com.kantu.pab_volunteers.ui.admin.activities

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.data.model.ActivitySignup
import com.kantu.pab_volunteers.databinding.ItemVolunteerBinding
import com.kantu.pab_volunteers.utils.AvatarColors
import com.kantu.pab_volunteers.utils.DateUtils
import com.kantu.pab_volunteers.utils.diffUpdate

/**
 * Shows who is coming to an activity. There is no action to take anyone off the list, but
 * tapping someone opens their details, the same as the Volunteers list does: an admin looking
 * at who is coming is usually looking for a way to reach one of them.
 */
class SignupAdapter(
    private var items: List<ActivitySignup>,
    private val onClick: (ActivitySignup) -> Unit
) : RecyclerView.Adapter<SignupAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemVolunteerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVolunteerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val signup = items[position]
        holder.binding.tvInitial.text = signup.volunteerName.take(1).uppercase()
        // Keyed on the account, not the sign-up, so the same person is the same colour here
        // as in the Volunteers list and on their own profile (K11).
        holder.binding.tvInitial.backgroundTintList =
            ColorStateList.valueOf(AvatarColors.forId(signup.userId))
        holder.binding.tvName.text = signup.volunteerName
        holder.binding.tvArea.text = DateUtils.formatDate(signup.signedUpDate)
        holder.itemView.setOnClickListener { onClick(signup) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ActivitySignup>) {
        val old = items
        items = newItems
        diffUpdate(old, newItems) { a, b -> a.id == b.id }
    }
}
