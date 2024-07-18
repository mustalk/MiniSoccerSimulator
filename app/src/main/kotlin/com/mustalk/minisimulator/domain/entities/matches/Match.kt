package com.mustalk.minisimulator.domain.entities.matches

import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * Represents a match between two [Team] objects.
 *
 * Holds the home and away teams, the simulated scores.
 *
 * @author by MusTalK on 18/07/2024
 */

data class Match(
    val homeTeam: Team,
    val awayTeam: Team,
) {
    var homeTeamScore: Int = 0
    var awayTeamScore: Int = 0
}
