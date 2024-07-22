package com.mustalk.minisimulator.presentation.standings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mustalk.minisimulator.presentation.standings.mappers.TeamStandingMapper
import com.mustalk.minisimulator.utils.FakeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GroupStandingsViewModel
 *
 * @author by MusTalK on 22/07/2024
 */

@ExperimentalCoroutinesApi
class GroupStandingsViewModelTest {
    /**
     * Ensures that all background tasks related to Architecture Components* execute synchronously on the main thread during tests.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GroupStandingsViewModel
    private lateinit var teamStandingMapper: TeamStandingMapper

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        teamStandingMapper = TeamStandingMapper()
        viewModel = GroupStandingsViewModel(teamStandingMapper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies that calling updateStandings with an empty list results in:
     * - An empty list of standings.
     * - The hasPlayedMatches flag being set to false.
     */
    @Test
    fun `updateStandings with empty list sets empty standings and hasPlayedMatches to false`() =
        runBlocking {
            viewModel.updateStandings(emptyList())

            assertThat(viewModel.standings.value, Matchers.empty())
            assertThat(viewModel.hasPlayedMatches.value, equalTo(false))
        }

    /**
     * Verifies that calling updateStandings with a list of teams:
     * - Updates the standings with the mapped TeamStandingItem objects.
     * - Sets the hasPlayedMatches flag to true if any team has played matches.
     */
    @Test
    fun `updateStandings with teams updates standings and sets hasPlayedMatches correctly`() =
        runBlocking {
            val teams = FakeData.fakeTeamsPlayed

            viewModel.updateStandings(teams)

            val expectedStandings = teams.map { teamStandingMapper.mapToTeamStandingItem(it) }

            assertThat(viewModel.standings.value, equalTo(expectedStandings))
            assertThat(
                viewModel.hasPlayedMatches.value,
                equalTo(true)
            )
        }

    /**
     * Verifies that calling updateStandings with a list of teams that have not played any matches
     * sets the hasPlayedMatches flag to false.
     */
    @Test
    fun `updateStandings with teams without played matches sets hasPlayedMatches to false`() =
        runBlocking {
            val teams = FakeData.fakeTeamsNotPlayed

            viewModel.updateStandings(teams)

            assertThat(viewModel.hasPlayedMatches.value, equalTo(false))
        }
}
