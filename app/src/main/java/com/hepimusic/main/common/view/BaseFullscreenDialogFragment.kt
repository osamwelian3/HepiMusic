package com.hepimusic.main.common.view

import android.os.Bundle
import com.hepimusic.R

open class BaseFullscreenDialogFragment: BaseDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_HepiMusic_FullScreenDialogStyle)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.Theme_HepiMusic_DialogAnimation
    }
}