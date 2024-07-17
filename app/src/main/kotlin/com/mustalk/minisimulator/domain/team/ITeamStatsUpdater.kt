package com.mustalk.minisimulator.domain.team

import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * @author by MusTalK on 16/07/2024
 *
 * Defines a contract for updating team statistics based on match results.
 */
interface ITeamStatsUpdater {
    /*** Updates the statistics of the given home and away teams based on the match result.
     *
     * @param homeTeam The home [Team] object.
     * @param awayTeam The away [Team] object.
     * @param homeScore The score of the home team.
     * @param awayScore The score of the away team.
     * @return A [Pair] of the updated [Team] objects.
     */
    fun updateStats(
        homeTeam: Team,
        awayTeam: Team,
        homeScore: Int,
        awayScore: Int,
    ): Pair<Team, Team>
}
