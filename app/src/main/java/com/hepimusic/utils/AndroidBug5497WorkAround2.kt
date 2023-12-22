package com.hepimusic.utils

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout

class AndroidBug5497Workaround constructor(val activity: Activity, view: View? = null) {
    private var contentContainer = activity.findViewById(android.R.id.content) as ViewGroup
    private var rootView = contentContainer.getChildAt(0)
    private var rootViewLayout = rootView.layoutParams as FrameLayout.LayoutParams
    private var viewTreeObserver = rootView.viewTreeObserver
    private val listener = ViewTreeObserver.OnGlobalLayoutListener { possiblyResizeChildOfContent() }

    private val contentAreaOfWindowBounds = Rect()
    private var usableHeightPrevious = 0

    // I call this in "onResume()" of my fragment
    fun addListener() {
        contentContainer = activity.findViewById(android.R.id.content) as ViewGroup
        rootView = contentContainer.getChildAt(0)
        rootViewLayout = rootView.layoutParams as FrameLayout.LayoutParams
        viewTreeObserver = rootView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    // I call this in "onPause()" of my fragment
    fun removeListener() {
        viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }

    private fun possiblyResizeChildOfContent() {
        Log.e("ROOT VIEW", rootView.toString()+" "+rootView.accessibilityClassName)
        contentContainer.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds)
        val usableHeightNow = contentAreaOfWindowBounds.bottom - contentAreaOfWindowBounds.top // contentAreaOfWindowBounds.height()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = rootView.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                rootViewLayout.height = usableHeightSansKeyboard - heightDifference
                rootViewLayout.topMargin = 200
                rootView.rootView.layoutParams.height = usableHeightSansKeyboard - heightDifference
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            } else {
                rootViewLayout.height = usableHeightSansKeyboard //usableHeightNow
                rootView.rootView.layoutParams.height = usableHeightSansKeyboard
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            // Change the bounds of the root view to prevent gap between keyboard and content, and top of content positioned above top screen edge.
            // rootView.layout(contentAreaOfWindowBounds.left, contentAreaOfWindowBounds.top, contentAreaOfWindowBounds.right, contentAreaOfWindowBounds.bottom)
            rootView.layoutParams = rootViewLayout
            rootView.rootView.layoutParams = rootView.rootView.layoutParams
            rootView.rootView.requestLayout()
            rootView.requestLayout()

            usableHeightPrevious = usableHeightNow
        }
    }
}
