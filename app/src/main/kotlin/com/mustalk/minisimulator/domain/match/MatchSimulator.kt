package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.teams.Team
import javax.inject.Inject

/**
 * An implementation of [IMatchSimulator] that simulates match results randomly,
 * taking into account the strength of each team.
 *
 * @author by MusTalK on 18/07/2024
 */

class MatchSimulator
    @Inject
    constructor() : IMatchSimulator {
        /**
         * Simulates the result of a match by generating random scores for each team,
         * using the team's strength as the upper bound of the random range.
         *
         * @param homeTeam The home [Team] object.
         * @param awayTeam The away [Team] object.
         * @return A [Pair] representing the simulated score, with the first element being the home team's score and the second being the away team's
         * score.
         **/
        override fun simulateResult(
            homeTeam: Team,
            awayTeam: Team,
        ): Pair<Int, Int> {
            val homeScore = (0..homeTeam.strength).random()
            val awayScore = (0..awayTeam.strength).random()
            return Pair(homeScore, awayScore)
        }
    }
