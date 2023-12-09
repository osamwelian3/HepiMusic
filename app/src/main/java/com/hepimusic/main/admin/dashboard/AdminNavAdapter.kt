package com.hepimusic.main.admin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hepimusic.databinding.ItemAdminNavBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.dragSwipe.OnStartDragListener

class AdminNavAdapter(
    private var items: List<AdminNavItem>?,
    private val dragStartListener: OnStartDragListener,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<AdminNavViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminNavViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemAdminNavBinding.inflate(inflater, parent, false)
        return AdminNavViewHolder(itemBinding, dragStartListener, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: AdminNavViewHolder, position: Int) {
        viewHolder.bind(items?.get(position))
    }

    fun updateItems(items: List<AdminNavItem>, fromPosition: Int? = null, toPosition: Int? = null) {
        this.items = items
        if (fromPosition != null && toPosition != null)
            notifyItemMoved(fromPosition, toPosition)
        else notifyDataSetChanged()
    }

}
