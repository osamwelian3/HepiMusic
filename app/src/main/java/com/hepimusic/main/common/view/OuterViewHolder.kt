package com.hepimusic.main.common.view

import android.content.Context
import android.util.Log
import android.view.View
import androidx.collection.SparseArrayCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.amplifyframework.datastore.generated.model.Song
import com.hepimusic.R
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.callbacks.OuterDiffCallback
import com.hepimusic.main.common.data.Model

class OuterViewHolder<Q: Model>(
    private val binding: ViewBinding,
    private val context: Context,
    private val layoutId: Int,
    private val variableId: Int,
    private val itemClickListener: OnItemClickListener? = null,
    private val mediaitemClicked: ((position: Int, sharableView: View?) -> Unit)? = null,
    private val animSet: Set<Int>? = setOf(R.anim.up_from_bottom, R.anim.down_from_top),
    private val longClick: Boolean = false,
    private var variables: SparseArrayCompat<Any>? = null,
    private val displayIn: RecyclerView
): RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    init {
        if (itemClickListener != null) {
            itemView.setOnClickListener(this)
            itemView.findViewById<RecyclerView>(R.id.outerRV).setOnClickListener(this)
            itemView.findViewById<RecyclerView>(R.id.outerRV).setOnTouchListener { v, _ ->
                Log.e("TOUCH LISTENER", adapterPosition.toString())
                itemClickListener?.onItemClick(adapterPosition)
                true
            }
        }
    }
    fun bind(items: List<Q>) {
        /*binding.setVariable(variableId, item)
        binding.executePendingBindings()*/
        val rv = itemView.findViewById<RecyclerView>(R.id.outerRV)
        val adapter = BaseAdapter(items, context, layoutId, variableId, null, mediaitemClicked, animSet, longClick, variables)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        Log.e("TOUCH LISTENER", adapterPosition.toString())
        when (v?.id) {
            R.id.outerRV -> itemClickListener?.onItemClick(
                adapterPosition
            )
        }
    }


}