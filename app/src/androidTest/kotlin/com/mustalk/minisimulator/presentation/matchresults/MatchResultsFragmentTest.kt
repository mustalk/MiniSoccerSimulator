package com.mustalk.minisimulator.presentation.matchresults

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.presentation.matchresults.adapters.MatchResultAdapter
import com.mustalk.minisimulator.utils.FakeData
import com.mustalk.minisimulator.utils.launchFragmentInHiltContainer
import com.mustalk.minisimulator.utils.waitFor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [MatchResultsFragment]
 *
 * @author by MusTalK on 21/07/2024
 */

@HiltAndroidTest
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class MatchResultsFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    /**
     * Test to verify that the fragment and empty view are displayed initially.
     */
    @Test
    fun testEmptyViewAndFragmentAreInitiallyDisplayed() {
        launchFragmentInHiltContainer<MatchResultsFragment>()

        onView(withId(R.id.matchResultsFragment)).check(matches(isDisplayed()))

        onView(withId(R.id.emptyViewContainer)).check(matches(isDisplayed()))
    }

    /**
     * Test to verify that match results are displayed when fake data is provided.
     */
    @Test
    fun testMatchResultsAreDisplayedWhenDataIsAvailable() =
        runTest {
            launchFragmentWithFakeData()

            // Wait for the RecyclerView to be displayed
            onView(isRoot()).perform(waitFor(300))

            onView(withId(R.id.emptyViewContainer)).check(matches(not(isDisplayed())))

            onView(withId(R.id.resultsRecyclerView)).check(matches(isDisplayed()))

            // At least the round match result recyclerView has at least one round item
            onView(withId(R.id.resultsRecyclerView)).check(matches(hasMinimumChildCount(1)))

            // At least the round item recyclerView has at least two match items
            onView(withId(R.id.matchesRecyclerView)).check(matches(hasMinimumChildCount(2)))
        }

    /**
     * Test to verify that clicking a match item displays the RoundWinnersDialog.
     */
    @Test
    fun testClickingMatchItemDisplaysRoundWinnersDialog() =
        runTest {
            launchFragmentWithFakeData()

            // Wait for the RecyclerView to be displayed
            onView(isRoot()).perform(waitFor(300))

            // Click on the first match item
            onView(
                withId(R.id.resultsRecyclerView)
            ).perform(RecyclerViewActions.actionOnItemAtPosition<MatchResultAdapter.RoundViewHolder>(0, click()))

            // Verify that the winnersRecyclerView on the RoundWinnersDialog is displayed
            onView(withId(R.id.winnersRecyclerView)).check(matches(isDisplayed()))
        }

    /**
     * Helper function to launch the fragment and provide fake data using coroutines.
     */
    private fun launchFragmentWithFakeData() {
        launchFragmentInHiltContainer<MatchResultsFragment> { fragment ->
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    // Provide fake data
                    fragment.matchResultsViewModel.generatePairedMatches(FakeData.fakeMatches, FakeData.FAKE_NUM_TEAMS)
                }
            }
        }
    }
}
