package com.kantu.pab_volunteers.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Swaps [old] rows for [new] ones, redrawing only the rows that changed (F14), so a refresh no
 * longer flashes the whole list and rows can animate in and out. [sameItem] says whether two rows
 * are the same thing (the same id); a changed row is one whose contents differ.
 */
fun <T> RecyclerView.Adapter<*>.diffUpdate(old: List<T>, new: List<T>, sameItem: (T, T) -> Boolean) {
    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldPosition: Int, newPosition: Int) = sameItem(old[oldPosition], new[newPosition])
        override fun areContentsTheSame(oldPosition: Int, newPosition: Int) = old[oldPosition] == new[newPosition]
    }).dispatchUpdatesTo(this)
}
