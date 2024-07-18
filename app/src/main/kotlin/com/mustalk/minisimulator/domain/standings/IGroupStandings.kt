package com.mustalk.minisimulator.domain.standings

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * @author by MusTalK on 16/07/2024
 *
 * Defines a contract for managing the standings
 * Provides methods for accessing team information, match results, and match simulations.
 */
interface IGroupStandings {
    /**
     * Retrieves the list of teams sorted by their performance (points, goal difference, etc.).
     *
     * @return The sorted list of teams.
     */
    fun getTeams(): List<Team>

    /**
     * Retrieves the previous positions of all teams, mapped by team name to their position.
     *
     * @return A map of team names to their previous positions.
     */
    fun getPreviousTeamPositions(): Map<String, Int>

    /**
     * Retrieves the list of matches played within the group.
     *
     * @return The list of matches.
     */
    fun getMatches(): List<Match>

    /**
     * Simulates all matches within the group.
     */
    fun simulateAllMatches()

    /**
     * Simulates the next round of matches (two matches per round).
     */
    fun simulateNextRoundMatches()

    /**
     * Resets the match results and team statistics to their initial state.
     */
    fun resetMatches()
}
