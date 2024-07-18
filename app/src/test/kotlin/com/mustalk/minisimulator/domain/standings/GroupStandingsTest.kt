package com.mustalk.minisimulator.domain.standings

import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.domain.match.IMatchGenerator
import com.mustalk.minisimulator.domain.usecases.matches.SimulateMatchUseCase
import com.mustalk.minisimulator.utils.FakeData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [GroupStandings].
 *
 * @author by MusTalK on 16/07/2024
 */

@RunWith(JUnit4::class)
class GroupStandingsTest {
    // Mocks the ITeamRepository
    @Mock
    private lateinit var teamRepository: ITeamRepository

    // Mocks the IMatchGenerator
    @Mock
    private lateinit var matchGenerator: IMatchGenerator

    // Mocks the SimulateMatchUseCase
    @Mock
    private lateinit var simulateMatchUseCase: SimulateMatchUseCase

    // The GroupStandings object being tested
    private lateinit var groupStandings: GroupStandings

    @Before
    fun setUp() {
        // Initializes mocks
        MockitoAnnotations.openMocks(this)
        // Creates an instance of GroupStandings with the mocked repository
        groupStandings = GroupStandings(teamRepository, matchGenerator, simulateMatchUseCase)
    }

    /**
     * Tests that the initializeTeams function correctly fetches teams from the repository.
     * Verifies that the fetched teams are stored and accessible through the getTeams method.
     */
    @Test
    fun `initializeTeams fetches teams from repository`() =
        runBlocking {
            // Predefined fake list of teams
            val teams = FakeData.fakeTeamsNotPlayed

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
            // Define teams with specific points and goal differences
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

    /**
     * Tests that the simulateAllMatches function simulates all matches correctly.
     * Verifies that the SimulateMatchUseCase was invoked for each match.
     */
    @Test
    fun `simulateAllMatches generates and simulates all matches`(): Unit =
        runBlocking {
            // Define a list of 3 teams that haven't played any matches
            val teams = FakeData.fake3TeamsNotPlayed

            // Define expected matches based on the teams
            val allMatches =
                listOf(
                    Match(teams[0], teams[1]),
                    Match(teams[0], teams[2]),
                    Match(teams[1], teams[2])
                )

            // Mock the team repository and match generator
            `when`(teamRepository.fetchTeams()).thenReturn(teams)
            `when`(matchGenerator.generateMatches(teams)).thenReturn(allMatches)

            // Initialize teams and simulate all matches
            groupStandings.initializeTeams()
            groupStandings.simulateAllMatches()

            // Verify that the SimulateMatchUseCase was invoked for each match
            verify(simulateMatchUseCase).invoke(allMatches[0])
            verify(simulateMatchUseCase).invoke(allMatches[1])
            verify(simulateMatchUseCase).invoke(allMatches[2])
        }

    /**
     * Tests that the simulateNextRoundMatches function simulates the next two matches correctly.
     * Verifies that the SimulateMatchUseCase was invoked for each match.
     */
    @Test
    fun `simulateNextRoundMatches simulates the next two matches and updates team positions`() =
        runBlocking {
            // Define a list of teams that haven't played any matches
            val teams = FakeData.fakeTeamsNotPlayed

            // Define the expected matches for the next round
            val nextRoundMatches =
                listOf(
                    Match(teams[0], teams[3]),
                    Match(teams[1], teams[2])
                )

            // Mock the team repository and match generator
            `when`(teamRepository.fetchTeams()).thenReturn(teams)
            `when`(matchGenerator.generateNextRoundMatches(teams)).thenReturn(nextRoundMatches)

            // Initialize teams and simulate the next round of matches
            groupStandings.initializeTeams()
            groupStandings.simulateNextRoundMatches()

            // Verify that the SimulateMatchUseCase was invoked for each match in the next round
            verify(simulateMatchUseCase).invoke(nextRoundMatches[0])
            verify(simulateMatchUseCase).invoke(nextRoundMatches[1])

            // After simulating, team positions should be updated (assuming some points are assigned)
            assertNotEquals(0, groupStandings.getTeams()[0].teamStats.teamPosition)
        }

    /**
     * Tests that the resetMatches function resets the match results, team stats, and previous positions.
     * Verifies that the match results, team stats, and previous positions are cleared.
     */
    @Test
    fun `resetMatches resets match results, team stats, and previous positions`() =
        runBlocking {
            // Define a list of teams with some initial stats (as if matches were played)
            val teams =
                listOf(
                    Team("Team A", 9, 0, TeamStats(matchesPlayed = 1, points = 3)),
                    Team("Team B", 8, 0, TeamStats(matchesPlayed = 1, points = 0))
                )
            `when`(teamRepository.fetchTeams()).thenReturn(teams)

            // Initialize teams and simulate all matches to modify stats
            groupStandings.initializeTeams()
            groupStandings.simulateAllMatches()
            groupStandings.resetMatches()

            // Assertions to verify the reset functionality
            // Matches should be cleared
            assertEquals(0, groupStandings.getMatches().size)
            // Stats should be reset
            assertEquals(0, groupStandings.getTeams()[0].teamStats.matchesPlayed)
            assertEquals(0, groupStandings.getTeams()[1].teamStats.points)
            // Previous positions should be cleared
            assertTrue(groupStandings.getPreviousTeamPositions().isEmpty())
        }
}
