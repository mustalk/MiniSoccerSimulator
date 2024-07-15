@file:Suppress("IllegalIdentifier")

package com.mustalk.minisimulator.presentation.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mustalk.minisimulator.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author by MusTalK on 15/07/2024
 *
 * A placeholder UI Test for [MainActivity]
 */

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    /**
     * Hilt rule for injecting dependencies into the test.
     *
     * This rule is essential for setting up the Hilt environment for the UI test,
     * allowing you to inject dependencies into your test class.
     */
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    /**
     * The ActivityScenarioRule launches the MainActivity before each test.
     */
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * Placeholder test to verify the initial setup of the MainActivity.
     *
     * Given: The MainActivity is launched.
     * When: The view is loaded.
     * Then: The Toolbar title should be displayed.
     */
    @Test
    fun placeholderTest() {
        // Given: The MainActivity is launched

        // When: The view is loaded
        // No action needed, as the ActivityScenarioRule launches the activity

        // Then: The Toolbar title should be displayed
        onView(withId(R.id.appTitle)).check(matches(isDisplayed()))
    }
}
