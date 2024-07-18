package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit test for [HasMatchBeenPlayedUseCase]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class HasMatchBeenPlayedUseCaseTest {
    // The use case being tested
    private lateinit var hasMatchBeenPlayedUseCase: HasMatchBeenPlayedUseCase

    @Before
    fun setUp() {
        // Initialize the use case before each test
        hasMatchBeenPlayedUseCase = HasMatchBeenPlayedUseCase()
    }

    @Test
    fun `invoke returns true when home team has played`() {
        // Define a match where only the home team has played
        val homeTeam = Team("Home Team", 0, 0, TeamStats(hasPlayed = true))
        val awayTeam = Team("Away Team", 0, 0, TeamStats(hasPlayed = false))
        val match = Match(homeTeam, awayTeam)

        // Call the use case and get the result
        val result = hasMatchBeenPlayedUseCase(match)

        // Assert that the result is true
        assertTrue(result)
    }

    @Test
    fun `invoke returns true when away team has played`() {
        // Create a match where only the away team has played
        val homeTeam = Team("Home Team", 0, 0, TeamStats(hasPlayed = false))
        val awayTeam = Team("Away Team", 0, 0, TeamStats(hasPlayed = true))
        val match = Match(homeTeam, awayTeam)

        // Call the use case and get the result
        val result = hasMatchBeenPlayedUseCase(match)

        // Assert that the result is true
        assertTrue(result)
    }

    @Test
    fun `invoke returns true when both teams have played`() {
        // Create a match where both teams have played
        val homeTeam = Team("Home Team", 0, 0, TeamStats(hasPlayed = true))
        val awayTeam = Team("Away Team", 0, 0, TeamStats(hasPlayed = true))
        val match = Match(homeTeam, awayTeam)

        // Call the use case and get the result
        val result = hasMatchBeenPlayedUseCase(match)

        // Assertthat the result is true
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when neither team has played`() {
        // Create a match where neither team has played
        val homeTeam = Team("Home Team", 0, 0, TeamStats(hasPlayed = false))
        val awayTeam = Team("Away Team", 0, 0, TeamStats(hasPlayed = false))
        val match = Match(homeTeam, awayTeam)

        // Call the use case and get the result
        val result = hasMatchBeenPlayedUseCase(match)

        // Assert that the result is false
        assertFalse(result)
    }
}
