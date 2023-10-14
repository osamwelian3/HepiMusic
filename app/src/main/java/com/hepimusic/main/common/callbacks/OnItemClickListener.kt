package com.hepimusic.main.common.callbacks

import android.view.View

interface OnItemClickListener {
    fun onItemClick(position: Int, sharableView: View? = null)

    fun onOverflowMenuClick(position: Int) {}

    fun onItemLongClick(position: Int) {}
}