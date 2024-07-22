package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.utils.FakeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for [MatchGenerator]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class MatchGeneratorTest {
    // The MatchGenerator being tested
    private lateinit var matchGenerator: IMatchGenerator

    @Before
    fun setUp() {
        // Initialize the MatchGenerator before each test
        matchGenerator = MatchGenerator()
    }

    @Test
    fun `generateNextRoundMatches returns correct pairings for first round`() {
        // Define a list of fake teams
        val teams = FakeData.fakeTeamsNotPlayed

        // Define the expected pairings for the first round
        val expectedPairings =
            setOf(
                // Team A vs Team D
                Pair(teams[0], teams[3]),
                // Team B vs Team C
                Pair(teams[1], teams[2])
            )

        // Generate the next round matches
        val actualMatches = matchGenerator.generateNextRoundMatches(teams)

        // Assert that the generated matches contain all expected pairings (in any order)
        assertEquals(expectedPairings.size, actualMatches.size)
        assertTrue(
            actualMatches.all { match ->
                expectedPairings.contains(Pair(match.homeTeam, match.awayTeam)) ||
                    expectedPairings.contains(Pair(match.awayTeam, match.homeTeam))
            }
        )
    }

    @Test
    fun `generateNextRoundMatches generates correct number of matches and rounds`() {
        // Define a list of teams
        val teams = FakeData.fakeTeamsNotPlayed
        val numTeams = teams.size

        // Calculate the expected number of rounds and matches per round
        val expectedNumberOfRounds = numTeams - 1 // For a round-robin tournament
        val expectedMatchesPerRound = numTeams / 2

        // Generate all rounds and collect the matches
        val generatedMatches = mutableListOf<Match>()
        repeat(expectedNumberOfRounds) {
            generatedMatches.addAll(matchGenerator.generateNextRoundMatches(teams))
        }

        // Assert the correct number of rounds and total matches were generated
        assertEquals(expectedNumberOfRounds, generatedMatches.size / expectedMatchesPerRound)
        assertEquals(expectedMatchesPerRound * expectedNumberOfRounds, generatedMatches.size)
    }

    @Test
    fun `generateNextRoundMatches resets and returns correct pairings for first round after all rounds`() {
        // Define a list of teams
        val teams = FakeData.fakeTeamsNotPlayed

        // Define the expected pairings for the first round (which should repeat after all rounds)
        val expectedPairings =
            setOf(
                // Team A vs Team D
                Pair(teams[0], teams[3]),
                // Team B vs Team C
                Pair(teams[1], teams[2])
            )

        // Generate all rounds (3 rounds for 4 teams) to cycle through the pairings
        repeat(3) { matchGenerator.generateNextRoundMatches(teams) }

        // Generate the next round, which should be the first round again
        val actualMatchesRound1Again = matchGenerator.generateNextRoundMatches(teams)

        // Assert that the generated matches for the repeated first round contain all expected pairings
        assertEquals(expectedPairings.size, actualMatchesRound1Again.size)
        assertTrue(
            actualMatchesRound1Again.all { match ->
                expectedPairings.contains(Pair(match.homeTeam, match.awayTeam)) ||
                    expectedPairings.contains(Pair(match.awayTeam, match.homeTeam))
            }
        )
    }

    @Test
    fun `generateMatches generates all possible pairings`() {
        // Define a list of teams
        val teams = FakeData.fake3TeamsNotPlayed

        // Define the expected pairings for all possible matches
        val expectedPairings =
            setOf(
                Pair(teams[0], teams[1]),
                Pair(teams[0], teams[2]),
                Pair(teams[1], teams[2])
            )

        // Generate all possible matches
        val generatedMatches = matchGenerator.generateMatches(teams)

        // Assert that the generated matches contain all expected pairings (in any order)
        assertEquals(expectedPairings.size, generatedMatches.size)
        assertTrue(
            generatedMatches.all { match ->
                expectedPairings.contains(Pair(match.homeTeam, match.awayTeam)) ||
                    expectedPairings.contains(Pair(match.awayTeam, match.homeTeam))
            }
        )
    }
}
