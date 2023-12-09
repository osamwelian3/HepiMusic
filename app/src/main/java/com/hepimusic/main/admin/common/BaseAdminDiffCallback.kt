package com.hepimusic.main.admin.common

import androidx.recyclerview.widget.DiffUtil
import com.hepimusic.main.common.data.Model

open class BaseAdminDiffCallback<T>(private val oldList: List<T>, private val newList: List<T>) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].toString() == newList[newItemPosition].toString()

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    // We are returning 0 for partial update. For full update subclasses should return null
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? = 0
}