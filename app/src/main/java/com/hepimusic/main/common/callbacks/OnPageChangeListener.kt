package com.hepimusic.main.common.callbacks

import androidx.viewpager.widget.ViewPager

interface OnPageChangeListener: ViewPager.OnPageChangeListener {
    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {}
}