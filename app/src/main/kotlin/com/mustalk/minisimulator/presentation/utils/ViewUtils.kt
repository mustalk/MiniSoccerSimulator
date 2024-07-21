package com.mustalk.minisimulator.presentation.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.mustalk.minisimulator.R

/**
 * A utility object providing common view-related functions.
 *
 * @author by MusTalK on 20/07/2024
 */
object ViewUtils {
    private const val FLIP_HALF_ROTATION_DEGREES = 90f
    private const val FLIP_ANIMATION_DURATION_MILLIS = 150L
    private const val SMOOTH_SCROLL_SPEED_MULTIPLIER = 6f

    /**
     * Sets up edge-to-edge display for the given view.
     *
     * This function applies window insets to the view's padding, ensuring that the view adjusts its
     * padding based on the system bars (status bar, navigation bar) to achieve an edge-to-edge layout.
     *
     * @param view The view to be adjusted for edge-to-edge display.
     */
    fun setupEdgeToEdgeDisplay(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Animates a view with a flip animation.
     *
     * This function creates a flip animation effect by rotating the view along the Y-axis. The animation
     * flips the view 90 degrees and then flips it back to its original position.
     *
     * @param view The view to be animated.
     */
    fun animateViewFlip(view: View) {
        val oa1 = ObjectAnimator.ofFloat(view, View.ROTATION_Y, 0f, FLIP_HALF_ROTATION_DEGREES)
        val oa2 = ObjectAnimator.ofFloat(view, View.ROTATION_Y, -FLIP_HALF_ROTATION_DEGREES, 0f)
        oa1.interpolator = AccelerateDecelerateInterpolator()
        oa2.interpolator = AccelerateDecelerateInterpolator()
        oa1.duration = FLIP_ANIMATION_DURATION_MILLIS
        oa2.duration = FLIP_ANIMATION_DURATION_MILLIS

        oa1.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    oa2.start()
                }
            }
        )
        oa1.start()
    }

    /**
     * Smoothly scrolls a RecyclerView to the end of the list.
     *
     * This function uses a custom `LinearSmoothScroller` to scroll the `RecyclerView` smoothly to a specific position.
     * The scrolling speed can be adjusted by modifying the speed multiplier.
     *
     * @param context The context used to create the `LinearSmoothScroller`.
     * @param recyclerView The `RecyclerView` to be scrolled.
     * @param position The target position to scroll to.
     */
    fun smoothScrollToEndOfList(
        context: Context,
        recyclerView: RecyclerView,
        position: Int,
    ) {
        val smoothScroller =
            object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_END // Snap to the end of the list
                }

                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
                    super.calculateSpeedPerPixel(displayMetrics) * SMOOTH_SCROLL_SPEED_MULTIPLIER
            }
        smoothScroller.targetPosition = position
        recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
    }

    /**
     * Displays a toast message.
     *
     * This function shows a short-duration toast message to the user.
     *
     * @param context The context used to create the toast.
     * @param message The message to be displayed in the toast.
     */
    fun showToast(
        context: Context,
        message: String,
    ) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Animates a view with a rotation animation.
     *
     * This function applies a rotation animation to the view, loading the animation resource
     * from the provided context.
     *
     * @param context The context used to load the animation resource.
     * @param view The view to be animated.
     */
    fun animateViewRotation(
        context: Context,
        view: View,
    ) {
        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate)
        view.apply {
            startAnimation(rotateAnimation)
        }
    }
}
