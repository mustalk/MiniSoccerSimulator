@file:Suppress("IllegalIdentifier")

package com.mustalk.minisimulator.presentation.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.utils.waitFor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [MainActivity].
 *
 * @author by MusTalK on 15/07/2024
 */

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Verifies that the initial screen displays the Match Results title.
     */
    @Test
    fun testInitialScreenShowsMatchResultsTitle() {
        onView(withId(R.id.appTitle)).check(matches(withText(R.string.title_round_match_results)))
    }

    /**
     * Verifies that the MatchResultsFragment is initially displayed.
     */
    @Test
    fun testMatchResultsFragmentAndEmptyViewAreInitiallyDisplayed() {
        // Check that MatchResultsFragment and the emptyView are displayed initially
        onView(withId(R.id.matchResultsFragment)).check(matches(isDisplayed()))

        onView(withId(R.id.emptyViewContainer)).check(matches(isDisplayed()))
    }

    /**
     * Verifies that clicking the Simulate Matches button populates the RecyclerView with matches data.
     */
    @Test
    fun testClickingSimulateMatchesButtonPopulatesRecyclerView() {
        onView(withId(R.id.btnSimulateMatches)).perform(click())

        // Wait for the RecyclerView to be displayed
        onView(isRoot()).perform(waitFor(1000))

        // Check if the RecyclerView has at least one item
        onView(withId(R.id.resultsRecyclerView)).check(matches(hasMinimumChildCount(1)))
    }

    /**
     * Verifies that the Group Standings button is initially visible.
     */
    @Test
    fun testInitialScreenShowsGroupStandingsButton() {
        onView(withId(R.id.btnStandings)).check(matches(isDisplayed()))
    }

    /**
     * Verifies that the Match Results button is initially hidden.
     */
    @Test
    fun testInitialScreenHidesMatchResultsButton() {
        onView(withId(R.id.btnMatchResults)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Verifies that clicking the Group Standings button displays the GroupStandingsFragment.
     */
    @Test
    fun testClickingGroupStandingsButtonShowsGroupStandingsFragment() {
        // Navigate to the GroupStandingsFragment
        onView(withId(R.id.btnStandings)).perform(click())

        // Wait for the view to be displayed
        onView(isRoot()).perform(waitFor(1000))

        // Check if a view specific to GroupStandingsFragment is displayed
        onView(withId(R.id.groupStandingsFragment)).check(matches(isDisplayed()))
    }
}
