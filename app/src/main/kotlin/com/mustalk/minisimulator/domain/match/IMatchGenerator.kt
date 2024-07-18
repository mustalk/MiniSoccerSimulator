package com.mustalk.minisimulator.domain.match

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * Defines a contract for generating matches between a list of [Team] objects.
 *
 * @author by MusTalK on 18/07/2024
 */

interface IMatchGenerator {
    /**
     * Generates a list of [Match] objects for all possible pairings between the given teams.
     *
     * @param teams The list of [Team] objects to generate matches for.
     * @return A list of [Match] objects representing all possible pairings.
     */
    fun generateMatches(teams: List<Team>): List<Match>

    /**
     * Generates a list of [Match] objects for the next round of matches based on the provided list of teams.
     * @param teams The list of teams to generate matches for.
     * @return A list of [Match] objects representing the next round of matches.
     */
    fun generateNextRoundMatches(teams: List<Team>): List<Match>
}
