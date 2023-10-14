package com.hepimusic.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.hepimusic.main.common.callbacks.AnimatorListener

fun Long.toIntorNull(): Int {
    return this.toInt()
}

fun View.fadeIn(duration: Long = 1000, startDelay: Long = 0): ObjectAnimator {
    return ObjectAnimator.ofFloat(this, View.ALPHA, 0F, 1F).apply {
        addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                this@fadeIn.visibility = View.VISIBLE
                this@fadeIn.isEnabled = true
                removeListener(this)
            }
        })
        setDuration(duration)
        setStartDelay(startDelay)
        start()

    }
}

/**
 *  Fades in this view and fades out the [otherView] view simultaneously
 *  @param  otherView the view to fade out
 *  @param duration duration of the animation
 *  @param visibility The visibility of [otherView] at the end of the animation
 */
fun View.crossFadeWidth(
    otherView: View,
    duration: Long = 1000,
    startDelay: Long = 0,
    visibility: Int = View.GONE
): AnimatorSet {
    val fadeIn = ObjectAnimator.ofFloat(this, View.ALPHA, 0F, 1F)
    val fadeOut = ObjectAnimator.ofFloat(otherView, View.ALPHA, 1F, 0F)

    return AnimatorSet().apply {
        addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                this@crossFadeWidth.isEnabled = true
                otherView.isEnabled = false
                this@crossFadeWidth.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                otherView.visibility = visibility
                this@apply.removeListener(this)
            }
        })
        setDuration(duration)
        setStartDelay(startDelay)
        playTogether(
            fadeIn,
            fadeOut
        )
        start()
    }
}