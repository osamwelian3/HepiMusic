package com.hepimusic.main.common.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hepimusic.R
import com.hepimusic.databinding.ItemOuterRvBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.callbacks.OuterDiffCallback
import com.hepimusic.main.common.data.Model

class OuterAdapter<Q: Model>(
    private var fullItems: List<Q>,
    private val context: Context,
    private val layoutId: Int,
    private val variableId: Int,
    private val itemClickListener: OnItemClickListener? = null,
    private val mediaitemClicked: ((position: Int, sharableView: View?) -> Unit)? = null,
    private val animSet: Set<Int>? = setOf(R.anim.up_from_bottom, R.anim.down_from_top),
    private val longClick: Boolean = false,
    private var variables: SparseArrayCompat<Any>? = null,
    private var chunkSize: Int = 4,
    private val outerLayoutId: Int,
    private val displayIn: RecyclerView,
    private val itemBoundBinding: ViewBinding
): RecyclerView.Adapter<OuterViewHolder<Q>>() {

    private var lastPosition = -1
    private var items = mutableListOf<List<Q>>()
    /*lateinit var rv: RecyclerView*/

    init {
        for (chunk in fullItems.indices step chunkSize) {
            val endindex = minOf(chunk+chunkSize, fullItems.size)
            items.add(fullItems.subList(chunk, endindex))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OuterViewHolder<Q> {
        val inflater = LayoutInflater.from(parent.context)
        val itView = inflater.inflate(outerLayoutId, parent, false)
        /*val itemBinding = DataBindingUtil.inflate<ViewDataBinding>(inflater, outerLayoutId, parent, false)*/
        val itemBinding = ItemOuterRvBinding.inflate(inflater)
        /*variables?.let {
            for (i in 0 until it.size()) {
                itemBinding?.setVariable(it.keyAt(i), it.valueAt(i))
            }
        }*/
        return OuterViewHolder(
            itemBinding,
            context,
            layoutId,
            variableId,
            itemClickListener,
            mediaitemClicked,
            animSet,
            longClick,
            variables,
            displayIn
        )
    }

    override fun getItemCount(): Int = items.size

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: OuterViewHolder<Q>, position: Int) {
        holder.bind(items[position])
        var chunkCount = 0
        var itemCount = 0
        for (chunk in items) {
            Log.e("CHUNK COUNT", chunkCount.toString())
            for (item in chunk) {
                val itm = item as com.hepimusic.main.songs.Song
                Log.e("ITEM", "Item at Position $itemCount with name ${itm.title}")
                itemCount += 1
            }
            itemCount = 0
            chunkCount += 1
        }

        /*(rv.adapter as BaseAdapter<Q>).updateItems(items[position])*/
    }

    override fun onBindViewHolder(holder: OuterViewHolder<Q>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    fun updateItems(items: List<Q>, diffCallback: OuterDiffCallback<Q> = OuterDiffCallback(fullItems, items)) {
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)
        diffResult.dispatchUpdatesTo(this@OuterAdapter)
        this@OuterAdapter.items.clear()
        for (chunk in items.indices step chunkSize) {
            val endindex = minOf(chunk+chunkSize, items.size)
            this@OuterAdapter.items.add(items.subList(chunk, endindex))
            /*(rv.adapter as BaseAdapter<Q>).updateItems(items.subList(chunk, endindex))*/
        }
        notifyDataSetChanged()
    }

    override fun onViewDetachedFromWindow(holder: OuterViewHolder<Q>) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    override fun getItemViewType(position: Int): Int = outerLayoutId
}