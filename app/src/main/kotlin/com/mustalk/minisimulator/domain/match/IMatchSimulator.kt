package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * Defines a contract for simulating the result of a match between two [Team] objects.
 *
 * @author by MusTalK on 18/07/2024
 */

interface IMatchSimulator {
    /**
     * Simulates the result of a match between the given home and away teams.
     *
     * @param homeTeam The home [Team] object.
     * @param awayTeam The away [Team] object.
     * @return A [Pair] representing the simulated score, where the first element is the home team's score and the second is the away team's score.
     */
    fun simulateResult(
        homeTeam: Team,
        awayTeam: Team,
    ): Pair<Int, Int>
}
