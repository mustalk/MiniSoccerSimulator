package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.domain.standings.GroupStandings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [GetMatchesUseCase]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class GetMatchesUseCaseTest {
    // Mock the GroupStandings class
    @Mock
    private lateinit var groupStandings: GroupStandings

    // The use case being tested
    private lateinit var getMatchesUseCase: GetMatchesUseCase

    @Before
    fun setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this)

        // Create an instance of the GetMatchesUseCase
        getMatchesUseCase = GetMatchesUseCase()
    }

    @Test
    fun `invoke returns matches from group standings`() {
        // Define the expected list of matches
        val expectedMatches =
            listOf(
                // Create some sample Match objects here
                Match(Team("Team A", 0, 0, TeamStats()), Team("Team B", 0, 0, TeamStats())),
                Match(Team("Team C", 0, 0, TeamStats()), Team("Team D", 0, 0, TeamStats()))
            )
        // Mock the groupStandings to return the expected matches
        `when`(groupStandings.getMatches()).thenReturn(expectedMatches)

        // Call the use case to get the actual matches
        val actualMatches = getMatchesUseCase(groupStandings)

        // Assert that the actual matches returned by the use case match the expected matches
        assertEquals(expectedMatches, actualMatches)
    }
}
