package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.domain.match.IMatchSimulator
import com.mustalk.minisimulator.domain.team.ITeamStatsUpdater
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit test for [SimulateMatchUseCase]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class SimulateMatchUseCaseTest {
    // Mock dependencies
    @Mock
    private lateinit var matchSimulator: IMatchSimulator

    @Mock
    private lateinit var statsUpdater: ITeamStatsUpdater

    private lateinit var simulateMatchUseCase: SimulateMatchUseCase

    @Before
    fun setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this)

        // Create an instance of the SimulateMatchUseCase with the mock dependencies
        simulateMatchUseCase = SimulateMatchUseCase(matchSimulator, statsUpdater)
    }

    @Test
    fun `invoke simulates match and updates team stats`() {
        // Define sample teams and a match
        val homeTeam = Team("Home Team", 8, 0, TeamStats())
        val awayTeam = Team("Away Team", 7, 0, TeamStats())
        val match = Match(homeTeam, awayTeam)
        // Define example simulated scores
        val simulatedScores = Pair(2, 1)

        // Mock the match simulator to return the simulated scores
        `when`(matchSimulator.simulateResult(homeTeam, awayTeam)).thenReturn(simulatedScores)

        // Mock the stats updater to return the updated teams
        `when`(
            statsUpdater.updateStats(
                homeTeam,
                awayTeam,
                simulatedScores.first,
                simulatedScores.second
            )
        ).thenReturn(Pair(homeTeam, awayTeam))

        // Call the use case to simulate the match
        simulateMatchUseCase(match)

        // Verify that the match simulator was called with the correct teams
        verify(matchSimulator).simulateResult(homeTeam, awayTeam)

        // Verify that the stats updater was called with the correct teams and simulated scores
        verify(statsUpdater).updateStats(
            homeTeam,
            awayTeam,
            simulatedScores.first,
            simulatedScores.second
        )

        // Verify that the match scores were updated with the simulated scores
        assertEquals(simulatedScores.first, match.homeTeamScore)
        assertEquals(simulatedScores.second, match.awayTeamScore)
    }
}
