package com.hepimusic.main.common.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.hepimusic.main.common.data.Model

class OuterDiffCallback<Q: Model>(private val oldList: List<Q>, private val newList: List<Q>): DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    // We are returning 0 for partial update. For full update subclasses should return null
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? = 0
}