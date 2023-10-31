package com.hepimusic.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.Resources
import android.net.Uri
import android.view.View
import com.hepimusic.main.common.callbacks.AnimatorListener
import java.lang.Math.abs

/**
 *  Creates an alpha (0 to 1) and vertical transition (0,0 to [translationY]) [Animator] for this view
 *
 *   @param [translationY] The position to animate to
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeInSlideUp(translationY: Float = 300F, duration: Long = 1000, startDelay: Long = 0): Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0F, 1F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, translationY, 0F)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
}


/**
 *  Creates an alpha (0 to 1) and vertical transition ([translationY] to 0,0) [Animator] for this view
 *
 *   @param [translationY] The position to animate to.Any value passed will be negated
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeInSlideDown(translationY: Float = -300F, duration: Long = 1000, startDelay: Long = 0): Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0F, 1F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -abs(translationY), 0F)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
}

/**
 *  Creates an alpha (1 to 0) and vertical transition (0,0 to [translationY]) [Animator] for this view
 *
 *   @param [translationY] The position to animate to. Any value passed will be negated
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeOutSlideUp(translationY: Float = -300F, duration: Long = 1000, startDelay: Long = 0): Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1F, 0F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F, -abs(translationY))
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
}

/**
 *  Creates an alpha (1 to 0) and vertical transition ([translationY] to 0,0) [Animator] for this view
 *
 *   @param [translationY] The position to animate to.
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeOutSlideDown(translationY: Float = 300F, duration: Long = 1000, startDelay: Long = 0): Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1F, 0F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F, translationY)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
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

/**
 *  Creates an alpha (0 to 1) and horizontal transition ([translationX] to 0) [Animator] for this view
 *
 *   @param [translationX] The position to animate to.
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeInSlideInHorizontally(
    translationX: Float = 300F,
    duration: Long = 1000,
    startDelay: Long = 0
):
        Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0F, 1F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, translationX, 0F)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    val  oldVisibility = this.visibility
    animator.addListener(object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            super.onAnimationStart(animation)
            this@fadeInSlideInHorizontally.visibility = View.VISIBLE
            this@fadeInSlideInHorizontally.isEnabled = true
        }

        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            this@fadeInSlideInHorizontally.isEnabled = false
            this@fadeInSlideInHorizontally.visibility = oldVisibility
        }
    })
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
}

/**
 *  Creates an alpha (1 to 0) and vertical transition ([translationX] to 0,0) [Animator] for this view
 *
 *   @param [translationX] The position to animate to.
 *   @param [duration] How long the animation should run
 *   @param [startDelay] How long to wait before running the animation
 *
 *   @return the built animator
 */
fun View.fadeOutSlideOutHorizontally(
    translationX: Float = 300F,
    duration: Long = 1000,
    startDelay: Long = 0,
    visibility: Int = View.GONE
):
        Animator {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1F, 0F)
    val translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0F, translationX)
    val animator = ObjectAnimator.ofPropertyValuesHolder(this, alpha, translateY)
    animator.addListener(object : AnimatorListener {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            this@fadeOutSlideOutHorizontally.visibility = visibility
            this@fadeOutSlideOutHorizontally.isEnabled = false
        }

    })
    animator.duration = duration
    animator.startDelay = startDelay
    return animator
}

/**
 * Helper extension to convert a potentially null [String] to a [Uri] falling back to [Uri.EMPTY]
 */
fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY

/**
 * Converts an Int in pixels to dp(density independent pixels)
 */
val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Converts an Int in dp(density independent pixels) to pixels
 */
val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Converts an Float in pixels to dp(density independent pixels)
 */
val Float.dp: Float get() = (this / Resources.getSystem().displayMetrics.density)

/**
 * Converts an Float in dp(density independent pixels) to pixels
 */
val Float.px: Float get() = (this * Resources.getSystem().displayMetrics.density)