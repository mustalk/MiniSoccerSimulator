package com.mustalk.minisimulator.utils

/**
 * Helper function to wait for a specific condition
 *
 * @author by MusTalK on 15/07/2024
 */

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot

fun waitFor(delay: Long): ViewAction =
    object : ViewAction {
        override fun getConstraints(): org.hamcrest.Matcher<View>? = isRoot()

        override fun getDescription(): String = "Wait for $delay milliseconds."

        override fun perform(
            uiController: UiController,
            view: View,
        ) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
