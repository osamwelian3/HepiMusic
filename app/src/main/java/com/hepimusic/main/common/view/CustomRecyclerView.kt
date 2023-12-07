package com.hepimusic.main.common.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView: RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun callOnClick(): Boolean {
        Log.e("CALL ON CLICK", "On Click Called")
        return super.callOnClick()
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        Log.e("CALL ON TOUCH", "On Touch Called")
        return super.onTouchEvent(e)
    }
}