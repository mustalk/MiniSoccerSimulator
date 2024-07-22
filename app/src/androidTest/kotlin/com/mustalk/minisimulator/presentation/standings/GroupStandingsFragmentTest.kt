package com.mustalk.minisimulator.presentation.standings

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.utils.FakeData
import com.mustalk.minisimulator.utils.launchFragmentInHiltContainer
import com.mustalk.minisimulator.utils.waitFor
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [GroupStandingsFragment]
 *
 * @author by MusTalK on 22/07/2024
 */

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class GroupStandingsFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /**
     * Tests that the empty view is displayed when no standings data is available.
     */
    @Test
    fun testEmptyStandingsDisplayed() {
        // Launch the fragment without providing any standings data
        launchFragmentInHiltContainer<GroupStandingsFragment>()

        // Verify that the empty view is displayed
        onView(withId(R.id.emptyViewContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.standingsRecyclerView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Tests that the standings are displayed correctly in the RecyclerView when data is provided.
     */
    @Test
    fun testStandingsDisplayed() =
        runTest {
            // Fake standings data
            val teams = FakeData.fakeTeams

            // Launch the fragment with the sample standings data
            launchFragmentInHiltContainer<GroupStandingsFragment> { fragment ->
                fragment.groupStandingsViewModel.updateStandings(teams)
            }

            // Wait for the RecyclerView to be displayed
            onView(isRoot()).perform(waitFor(300))

            // Verify that the RecyclerView is displayed and has the expected number of items
            onView(withId(R.id.standingsRecyclerView)).check(matches(isDisplayed()))
            onView(withId(R.id.standingsRecyclerView)).check(matches(hasChildCount(teams.size)))

            // Verify that the empty view is hidden
            onView(withId(R.id.emptyViewContainer)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
}
