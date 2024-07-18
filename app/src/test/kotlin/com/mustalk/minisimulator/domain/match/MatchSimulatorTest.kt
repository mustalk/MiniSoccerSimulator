package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * UnitTest for [MatchSimulator]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class MatchSimulatorTest {
    private lateinit var matchSimulator: IMatchSimulator

    @Before
    fun setUp() {
        // Initialize the MatchSimulator before each test
        matchSimulator = MatchSimulator()
    }

    @Test
    fun `simulateResult generates scores within team strength bounds`() {
        // Define two teams with different strengths
        val homeTeam = Team("HomeTeam", strength = 9, teamLogo = 0, teamStats = TeamStats())
        val awayTeam = Team("Away Team", strength = 7, teamLogo = 0, teamStats = TeamStats())

        // Run multiple simulations to increase confidence in the results
        repeat(10) {
            val (homeScore, awayScore) = matchSimulator.simulateResult(homeTeam, awayTeam)

            // Assert that the generated scores are within the bounds of each team's strength
            assertTrue(homeScore in 0..homeTeam.strength)
            assertTrue(awayScore in 0..awayTeam.strength)
        }
    }

    @Test
    fun `simulateResult handles zero strength teams`() {
        // Create a team with zero strength and another with non-zero strength
        val zeroStrengthTeam = Team("Zero Team", strength = 0, teamLogo = 0, teamStats = TeamStats())
        val otherTeam = Team("Other Team", strength = 5, teamLogo = 0, teamStats = TeamStats())

        // Simulate a match between these teams
        val (homeScore, awayScore) = matchSimulator.simulateResult(zeroStrengthTeam, otherTeam)

        // Assert that the zero strength team always scores 0
        assertEquals(0, homeScore)

        // Assert that the other team's score is within its strength bounds
        assertTrue(awayScore in 0..otherTeam.strength)
    }
}
