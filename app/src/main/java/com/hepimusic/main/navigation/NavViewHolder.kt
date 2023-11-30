package com.hepimusic.main.navigation

import android.util.TypedValue
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.hepimusic.R
import com.hepimusic.databinding.ItemNavBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.dragSwipe.ItemTouchHelperViewHolder
import com.hepimusic.main.dragSwipe.OnStartDragListener

class NavViewHolder(
    private val itemBinding: ItemNavBinding,
    private val dragStartListener: OnStartDragListener,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.ViewHolder(itemBinding.root), ItemTouchHelperViewHolder {

    init {
        itemView.clipToOutline = true
        itemBinding.rippleView.setOnClickListener {
            onItemClickListener.onItemClick(adapterPosition)
        }

        itemView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dragStartListener.onStartDrag(this)
            }
            false
        }
    }

    fun bind(navItem: NavItem?) {
        itemBinding.item = navItem
        itemBinding.executePendingBindings()
    }

    override fun onItemSelected() {
        itemBinding.rippleView.setBackgroundResource(R.drawable.nav_item_dragging)
    }

    override fun onItemClear() {
        val outValue = TypedValue()
        itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        itemBinding.rippleView.setBackgroundResource(outValue.resourceId)
    }
}