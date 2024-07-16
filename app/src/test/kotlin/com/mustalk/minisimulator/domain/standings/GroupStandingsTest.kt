package com.mustalk.minisimulator.domain.standings

import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.utils.FakeData.fakeTeamsNotPlayed
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * @author by MusTalK on 16/07/2024
 *
 * Unit tests for [GroupStandings].
 */

@RunWith(JUnit4::class)
class GroupStandingsTest {
    // Mocks the ITeamRepository
    @Mock
    private lateinit var teamRepository: ITeamRepository

    // The GroupStandings object being tested
    private lateinit var groupStandings: GroupStandings

    @Before
    fun setUp() {
        // Initializes mocks
        MockitoAnnotations.openMocks(this)
        // Creates an instance of GroupStandings with the mocked repository
        groupStandings = GroupStandings(teamRepository)
    }

    /**
     * Tests that the initializeTeams function correctly fetches teams from the repository.
     * Verifies that the fetched teams are stored and accessible through the getTeams method.
     */
    @Test
    fun `initializeTeams fetches teams from repository`() =
        runBlocking {
            // Predefined fake list of teams
            val teams = fakeTeamsNotPlayed

            // Mocks the fetchTeams method to return the predefined teams
            `when`(teamRepository.fetchTeams()).thenReturn(teams)

            // Calls the initializeTeams function
            groupStandings.initializeTeams()

            // Asserts that the fetched teams match the expected teams
            assertEquals(teams, groupStandings.getTeams())
        }

    /**
     * Tests that the getTeams function returns teams sorted correctly by points and then by goal difference.
     * Verifies the order of teams based on their points and goal difference.
     */
    @Test
    fun `getTeams returns teams sorted by points then goal difference`() =
        runBlocking {
            // Creates teams with specific points and goal differences
            val teamA = Team("Team A", 9, 0, TeamStats(points = 6, goalDifference = 3))
            val teamB = Team("Team B", 8, 0, TeamStats(points = 6, goalDifference = 1))
            val teamC = Team("Team C", 7, 0, TeamStats(points = 3))
            // Unsorted list of teams
            val teams = listOf(teamC, teamB, teamA)

            // Mocks fetchTeams
            `when`(teamRepository.fetchTeams()).thenReturn(teams)

            // Initializes teams
            groupStandings.initializeTeams()
            // Gets the sorted teams
            val sortedTeams = groupStandings.getTeams()

            // Asserts the correct order of teams based on points and goal difference
            assertEquals(teamA, sortedTeams[0]) // Highest points, highest goal difference
            assertEquals(teamB, sortedTeams[1])
            assertEquals(teamC, sortedTeams[2]) // Lowest points
        }
}
