package com.mustalk.minisimulator.domain.standings

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
}
