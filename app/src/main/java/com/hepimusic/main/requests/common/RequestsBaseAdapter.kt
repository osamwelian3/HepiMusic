package com.hepimusic.main.requests.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.collection.SparseArrayCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.model.Model
import com.hepimusic.R
import com.hepimusic.main.common.callbacks.OnItemClickListener

class RequestsBaseAdapter<T: Model>(
    private var items: List<T>,
    private val context: Context,
    private val layoutId: Int,
    private val variableId: Int,
    private val itemClickListener: OnItemClickListener? = null,
    private val mediaitemClicked: ((position: Int, sharableView: View?) -> Unit)? = null,
    private val animSet: Set<Int>? = setOf(R.anim.up_from_bottom, R.anim.down_from_top),
    private val longClick: Boolean = false,
    private var variables: SparseArrayCompat<Any>? = null
): RecyclerView.Adapter<RequestsBaseViewHolder<T>>() {

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsBaseViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = DataBindingUtil.inflate<ViewDataBinding>(inflater, layoutId, parent, false)
        variables?.let {
            for (i in 0 until it.size()) {
                itemBinding.setVariable(it.keyAt(i), it.valueAt(i))
            }
        }
        return RequestsBaseViewHolder(itemBinding, variableId, itemClickListener, mediaitemClicked, longClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RequestsBaseViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun onBindViewHolder(holder: RequestsBaseViewHolder<T>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) animateItem(position, holder)
    }

    fun updateItems(items: List<T>, diffCallback: RequestsBaseDiffCallback<T> = RequestsBaseDiffCallback(this.items, items)) {
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)
        diffResult.dispatchUpdatesTo(this@RequestsBaseAdapter)
        this@RequestsBaseAdapter.items = items
    }

    override fun onViewDetachedFromWindow(holder: RequestsBaseViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    private fun animateItem(position: Int, holder: RecyclerView.ViewHolder) {
        if (animSet == null || animSet.isEmpty()) return

        val animation = AnimationUtils.loadAnimation(
            context,
            if (animSet.size == 1) {
                animSet.first()
            } else {
                if (position > lastPosition)
                    animSet.first()
                else
                    animSet.elementAt(1)
            }
        )
        holder.itemView.startAnimation(animation)
        lastPosition = position
    }

    override fun getItemViewType(position: Int): Int = layoutId
}