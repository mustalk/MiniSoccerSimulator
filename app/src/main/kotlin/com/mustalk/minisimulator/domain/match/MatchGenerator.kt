package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import javax.inject.Inject
import kotlin.random.Random

/**
 * A default implementation of [IMatchGenerator] that generates a set of matches
 * for a group of teams using a round-robin algorithm, simulating round matches
 * and ensuring all teams play against each other.
 *
 * @author by MusTalK on 18/07/2024
 */

class MatchGenerator
    @Inject
    constructor() : IMatchGenerator {
        private var currentRoundIndex = 0
        private var totalMatchesGenerated = 0

        /**
         * Generates the next round of matches based on the provided list of teams.
         * The matches are generated ensuring all teams play each other
         * in a round-robin format.
         *
         * @param teams The list of teams to generate matches for.
         * @return A list of matches for the next round.
         */
        override fun generateNextRoundMatches(teams: List<Team>): List<Match> {
            // The total number of matches for n teams in a round-robin tournament is given by: n * (n - 1) / 2
            val numberOfTeams = teams.size
            val numberOfRounds = numberOfTeams - 1
            val totalMatches = numberOfTeams * numberOfRounds / 2

            // Check if we've simulated all rounds, and start over again
            if (totalMatchesGenerated >= totalMatches) {
                resetMatches()
            }

            val rounds = generateAllRounds(teams)
            val matchesForCurrentRound = rounds[currentRoundIndex]

            // Increment the round index and total matches generated
            currentRoundIndex = (currentRoundIndex + 1) % numberOfRounds
            totalMatchesGenerated += matchesForCurrentRound.size

            return matchesForCurrentRound
        }

        /**
         * Resets the match generation process.
         */
        private fun resetMatches() {
            currentRoundIndex = 0
            totalMatchesGenerated = 0
        }

        /**
         * Generates a fixed list of all matches for a group of teams, ensuring all possible pairings.
         *
         * @param teams The list of [Team] objects.
         * @return A list of [Match] objects representing the generated matches.
         */
        override fun generateMatches(teams: List<Team>): List<Match> {
            val matches = mutableListOf<Match>()
            val numberOfTeams = teams.size

            // Generate all possible pairings (round-robin format)
            for (i in 0 until numberOfTeams) {
                for (j in i + 1 until numberOfTeams) {
                    matches.add(Match(teams[i], teams[j]))
                }
            }

            // Shuffle the matches to add randomness to the order
            matches.shuffle(Random(System.currentTimeMillis()))

            return matches
        }

        // Generates all rounds of matches for the given teams using a round-robin algorithm.
        private fun generateAllRounds(teams: List<Team>): List<List<Match>> {
            val numberOfTeams = teams.size
            val rounds = mutableListOf<MutableList<Match>>()

            // Round-robin algorithm
            for (round in 0 until numberOfTeams - 1) {
                val roundMatches = mutableListOf<Match>()
                for (match in 0 until numberOfTeams / 2) {
                    val home = (round + match) % (numberOfTeams - 1)
                    val away = (numberOfTeams - 1 - match + round) % (numberOfTeams - 1)
                    // Last team stays in the same position; adjust for zero-based index
                    if (match == 0) {
                        roundMatches.add(Match(teams[home], teams[numberOfTeams - 1]))
                    } else {
                        roundMatches.add(Match(teams[home], teams[away]))
                    }
                }
                rounds.add(roundMatches)
            }

            return rounds
        }
    }
