package com.mustalk.minisimulator.presentation.matchresults

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mustalk.minisimulator.utils.FakeData
import com.mustalk.minisimulator.utils.FakeData.FAKE_NUM_TEAMS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the [MatchResultsViewModel].
 *
 * @author by MusTalK on 20/07/2024
 */

@ExperimentalCoroutinesApi
class MatchResultsViewModelTest {
    /**
     * Ensures that all background tasks related to Architecture Components* execute synchronously on the main thread during tests.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MatchResultsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MatchResultsViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies that calling generatePairedMatches with an empty list of matches
     * emits an empty list of paired matches.
     */

    @Test
    fun `generatePairedMatches with empty input emits empty list`() =
        runBlocking {
            viewModel.generatePairedMatches(emptyList(), 0)

            assertThat(viewModel.pairedMatches.value, Matchers.empty())
        }

    /**
     * Verifies that calling generatePairedMatches with a valid list of matches
     * emits a list of paired matches with the correct number of rounds and matches per round.
     */
    @Test
    fun `generatePairedMatches with valid input emits paired matches`() =
        runBlocking {
            val matches = FakeData.fakeMatches
            val numTeams = FAKE_NUM_TEAMS
            val matchesPerRound = numTeams / 2

            viewModel.generatePairedMatches(matches, numTeams)

            val pairedMatches = viewModel.pairedMatches.value!!

            assertThat(pairedMatches, hasSize(matches.size / matchesPerRound)) // Assert correct number of rounds

            repeat(matches.size / matchesPerRound) { roundIndex ->
                assertThat(pairedMatches[roundIndex], hasSize(matchesPerRound)) // Each round has matchesPerRound matches
            }
        }

    /**
     * Verifies that calling generatePairedMatches with an odd number of matches
     * correctly handles the remainder by creating an additional round with the remaining match.
     */
    @Test
    fun `generatePairedMatches with odd number of matches handles remainder`() =
        runBlocking {
            val matches = FakeData.fakeOddMatches
            val numTeams = FakeData.FAKE_NUM_TEAMS

            val matchesPerRound = numTeams / 2

            viewModel.generatePairedMatches(matches, numTeams)

            val pairedMatches = viewModel.pairedMatches.value!!

            // Assert correct number of rounds (full rounds + potential remainder round)
            assertThat(pairedMatches, hasSize(matches.size / matchesPerRound + if (matches.size % matchesPerRound == 0) 0 else 1))

            repeat(matches.size / matchesPerRound) { roundIndex ->
                assertThat(pairedMatches[roundIndex], hasSize(matchesPerRound)) // Full rounds have matchesPerRound matches
            }

            if (matches.size % matchesPerRound != 0) {
                assertThat(pairedMatches[pairedMatches.size - 1], hasSize(matches.size % matchesPerRound)) // Remainder round has remaining matches
            }
        }
}
