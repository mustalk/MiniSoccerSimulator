package com.mustalk.minisimulator.domain.entities.teams

/**
 * @author by MusTalK on 16/07/2024
 *
 * Represents a team that holds information about the team's name, strength, logo, and match statistics.
 */
data class Team(
    val name: String,
    val strength: Int,
    var teamLogo: Int = 0,
    var teamStats: TeamStats = TeamStats(),
)
