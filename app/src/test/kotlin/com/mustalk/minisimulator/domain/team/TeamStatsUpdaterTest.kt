package com.mustalk.minisimulator.domain.team

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author by MusTalK on 16/07/2024
 *
 * Unit tests for [TeamStatsUpdater].
 */

@RunWith(JUnit4::class)
class TeamStatsUpdaterTest {
    // The TeamStatsUpdater object being tested
    private lateinit var teamStatsUpdater: ITeamStatsUpdater

    @Before
    fun setUp() {
        // Creates an instance of the TeamStatsUpdater
        teamStatsUpdater = TeamStatsUpdater()
    }

    /**
     * Tests that the updateStats function correctly updates team statistics for a home win scenario.
     * Verifies the updated values for matches played, wins, draws, losses, goals for, goals against, goal difference, points, and hasPlayed flag.
     */
    @Test
    fun `updateStats updates stats correctly for home win`() {
        // Home team with initial stats
        val homeTeam = Team("Home Team", 8, 0, TeamStats())
        // Away team with initial stats
        val awayTeam = Team("Away Team", 7, 0, TeamStats())

        val homeScore = 2
        val awayScore = 1

        // Updates the stats based on the match result
        val (updatedHomeTeam, updatedAwayTeam) = teamStatsUpdater.updateStats(homeTeam, awayTeam, homeScore, awayScore)

        // Assertions for home team (win)
        assertEquals(1, updatedHomeTeam.teamStats.matchesPlayed)
        assertEquals(1, updatedHomeTeam.teamStats.wins)
        assertEquals(0, updatedHomeTeam.teamStats.draws)
        assertEquals(0, updatedHomeTeam.teamStats.losses)
        assertEquals(2, updatedHomeTeam.teamStats.goalsFor)
        assertEquals(1, updatedHomeTeam.teamStats.goalsAgainst)
        assertEquals(1, updatedHomeTeam.teamStats.goalDifference)
        assertEquals(3, updatedHomeTeam.teamStats.points)
        assertEquals(true, updatedHomeTeam.teamStats.hasPlayed)

        // Assertions for away team (loss)
        assertEquals(1, updatedAwayTeam.teamStats.matchesPlayed)
        assertEquals(0, updatedAwayTeam.teamStats.wins)
        assertEquals(0, updatedAwayTeam.teamStats.draws)
        assertEquals(1, updatedAwayTeam.teamStats.losses)
        assertEquals(1, updatedAwayTeam.teamStats.goalsFor)
        assertEquals(2, updatedAwayTeam.teamStats.goalsAgainst)
        assertEquals(-1, updatedAwayTeam.teamStats.goalDifference)
        assertEquals(0, updatedAwayTeam.teamStats.points)
        assertEquals(true, updatedAwayTeam.teamStats.hasPlayed)
    }

    /**
     * Tests that the updateStats function correctly updates team statistics for an away win scenario.
     * Verifies the updated values for matches played, wins, draws, losses, goals for, goals against, goal difference, points, and hasPlayed flag.
     */
    @Test
    fun `updateStats updates stats correctly for away win`() {
        val homeTeam = Team("Home Team", 8, 0, TeamStats())
        val awayTeam = Team("Away Team", 7, 0, TeamStats())
        val homeScore = 0
        val awayScore = 3

        val (updatedHomeTeam, updatedAwayTeam) = teamStatsUpdater.updateStats(homeTeam, awayTeam, homeScore, awayScore)

        // Assertions for home team (loss)
        assertEquals(1, updatedHomeTeam.teamStats.matchesPlayed)
        assertEquals(0, updatedHomeTeam.teamStats.wins)
        assertEquals(0, updatedHomeTeam.teamStats.draws)
        assertEquals(1, updatedHomeTeam.teamStats.losses)
        assertEquals(0, updatedHomeTeam.teamStats.goalsFor)
        assertEquals(3, updatedHomeTeam.teamStats.goalsAgainst)
        assertEquals(-3, updatedHomeTeam.teamStats.goalDifference)
        assertEquals(0, updatedHomeTeam.teamStats.points)
        assertEquals(true, updatedHomeTeam.teamStats.hasPlayed)

        // Assertions for away team (win)
        assertEquals(1, updatedAwayTeam.teamStats.matchesPlayed)
        assertEquals(1, updatedAwayTeam.teamStats.wins)
        assertEquals(0, updatedAwayTeam.teamStats.draws)
        assertEquals(0, updatedAwayTeam.teamStats.losses)
        assertEquals(3, updatedAwayTeam.teamStats.goalsFor)
        assertEquals(0, updatedAwayTeam.teamStats.goalsAgainst)
        assertEquals(3, updatedAwayTeam.teamStats.goalDifference)
        assertEquals(3, updatedAwayTeam.teamStats.points)
        assertEquals(true, updatedAwayTeam.teamStats.hasPlayed)
    }

    /**
     * Tests that the updateStats function correctly updates team statistics for a draw scenario.
     * Verifies the updated values for matches played, wins, draws, losses, goals for, goals against, goal difference, points, and hasPlayed flag.
     */
    @Test
    fun `updateStats updates stats correctly for a draw`() {
        val homeTeam = Team("Home Team", 8, 0, TeamStats())
        val awayTeam = Team("Away Team", 7, 0, TeamStats())
        val homeScore = 1
        val awayScore = 1

        val (updatedHomeTeam, updatedAwayTeam) = teamStatsUpdater.updateStats(homeTeam, awayTeam, homeScore, awayScore)

        // Assertions for both teams (draw)
        assertEquals(1, updatedHomeTeam.teamStats.matchesPlayed)
        assertEquals(0, updatedHomeTeam.teamStats.wins)
        assertEquals(1, updatedHomeTeam.teamStats.draws)
        assertEquals(0, updatedHomeTeam.teamStats.losses)
        assertEquals(1, updatedHomeTeam.teamStats.goalsFor)
        assertEquals(1, updatedHomeTeam.teamStats.goalsAgainst)
        assertEquals(0, updatedHomeTeam.teamStats.goalDifference)
        assertEquals(1, updatedHomeTeam.teamStats.points)
        assertEquals(true, updatedHomeTeam.teamStats.hasPlayed)

        assertEquals(1, updatedAwayTeam.teamStats.matchesPlayed)
        assertEquals(0, updatedAwayTeam.teamStats.wins)
        assertEquals(1, updatedAwayTeam.teamStats.draws)
        assertEquals(0, updatedAwayTeam.teamStats.losses)
        assertEquals(1, updatedAwayTeam.teamStats.goalsFor)
        assertEquals(1, updatedAwayTeam.teamStats.goalsAgainst)
        assertEquals(0, updatedAwayTeam.teamStats.goalDifference)
        assertEquals(1, updatedAwayTeam.teamStats.points)
        assertEquals(true, updatedAwayTeam.teamStats.hasPlayed)
    }

    /**
     * Tests that the updateStats function correctly handles multiple matches with varying results.
     * Simulates multiple matches and verifies the accumulated statistics for both teams.
     */
    @Test
    fun `updateStats handles multiple matches correctly`() {
        val homeTeam = Team("Home Team", 8, 0, TeamStats())
        val awayTeam = Team("Away Team", 7, 0, TeamStats())

        // Simulate multiple matches with different results
        teamStatsUpdater.updateStats(homeTeam, awayTeam, 2, 1) // Home win
        teamStatsUpdater.updateStats(homeTeam, awayTeam, 0, 2) // Away win
        teamStatsUpdater.updateStats(homeTeam, awayTeam, 1, 1) // Draw

        // Assertions after multiple matches
        assertEquals(3, homeTeam.teamStats.matchesPlayed)
        assertEquals(1, homeTeam.teamStats.wins)
        assertEquals(1, homeTeam.teamStats.draws)
        assertEquals(1, homeTeam.teamStats.losses)
        assertEquals(3, homeTeam.teamStats.goalsFor)
        assertEquals(4, homeTeam.teamStats.goalsAgainst)
        assertEquals(-1, homeTeam.teamStats.goalDifference)
        assertEquals(4, homeTeam.teamStats.points)
        assertEquals(true, homeTeam.teamStats.hasPlayed)

        assertEquals(3, awayTeam.teamStats.matchesPlayed)
        assertEquals(1, awayTeam.teamStats.wins)
        assertEquals(1, awayTeam.teamStats.draws)
        assertEquals(1, awayTeam.teamStats.losses)
        assertEquals(4, awayTeam.teamStats.goalsFor)
        assertEquals(3, awayTeam.teamStats.goalsAgainst)
        assertEquals(1, awayTeam.teamStats.goalDifference)
        assertEquals(4, awayTeam.teamStats.points)
        assertEquals(true, awayTeam.teamStats.hasPlayed)
    }
}
