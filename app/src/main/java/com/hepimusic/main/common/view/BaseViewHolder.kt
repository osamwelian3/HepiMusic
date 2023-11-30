package com.hepimusic.main.common.view

import android.view.View
import android.widget.CheckBox
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.hepimusic.R
import com.hepimusic.main.common.callbacks.OnItemClickListener

class BaseViewHolder<T>(
    private val binding: ViewDataBinding,
    private val variableId: Int,
    private val itemClickListener: OnItemClickListener? = null,
    private val mediaItemClicked: ((position: Int, sharableView: View?) -> Unit)? = null,
    longClick: Boolean = false
): RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

    init {
        if (itemClickListener != null) {
            itemView.setOnClickListener(this)
            if (longClick) itemView.setOnLongClickListener(this)
            itemView.findViewById<View>(R.id.moreOptions)?.setOnClickListener(this)
            itemView.findViewById<CheckBox>(R.id.checkbox)?.setOnClickListener(this)
        }
        if (mediaItemClicked != null) {
            itemView.setOnClickListener(::mediaClicked)
        }
    }

    fun bind(item: T) {
        binding.setVariable(variableId, item)
        binding.executePendingBindings()
    }

    fun mediaClicked(v: View?) {
        mediaItemClicked?.let {
            it(
                adapterPosition,
                itemView.findViewById(R.id.sharableView)
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.container, R.id.checkbox -> itemClickListener?.onItemClick(
                absoluteAdapterPosition,
                itemView.findViewById(R.id.sharableView)
            )
            R.id.moreOptions -> itemClickListener?.onOverflowMenuClick(adapterPosition)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        return when (v?.id) {
            R.id.container -> {
                itemClickListener?.onItemLongClick(adapterPosition)
                true
            }
            else -> false
        }
    }

}