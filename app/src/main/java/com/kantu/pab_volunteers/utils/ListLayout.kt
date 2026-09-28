package com.kantu.pab_volunteers.utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kantu.pab_volunteers.R

/**
 * Cards run down the screen one at a time on a phone and sit two abreast on a tablet,
 * where a single card stretched across the whole width just looks wrong. The number of
 * columns comes from a resource, so the screen size decides it rather than the code.
 */
object ListLayout {

    fun forCards(context: Context): RecyclerView.LayoutManager =
        GridLayoutManager(context, context.resources.getInteger(R.integer.list_columns))
}
