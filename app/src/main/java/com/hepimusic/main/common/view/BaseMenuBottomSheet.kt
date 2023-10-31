package com.hepimusic.main.common.view

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.hepimusic.R

abstract class BaseMenuBottomSheet: BaseBottomSheetDialogFragment(), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonsContainer = view.findViewById<LinearLayout>(R.id.buttonsContainer)
        for (i in 0 until buttonsContainer.childCount) {
            buttonsContainer.getChildAt(i).setOnClickListener(this)
        }
    }
}