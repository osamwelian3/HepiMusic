package com.hepimusic.main.requests.common

import androidx.recyclerview.widget.DiffUtil
import com.amplifyframework.core.model.Model

open class RequestsBaseDiffCallback<T : Model>(private val oldList: List<T>, private val newList: List<T>) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].primaryKeyString == newList[newItemPosition].primaryKeyString

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    // We are returning 0 for partial update. For full update subclasses should return null
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? = 0
}