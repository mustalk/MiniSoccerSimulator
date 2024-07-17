package com.mustalk.minisimulator.domain.entities.teams

/**
 * @author by MusTalK on 16/07/2024
 *
 * Represents the statistics of a given team
 */

data class TeamStats(
    var matchesPlayed: Int = 0,
    var wins: Int = 0,
    var draws: Int = 0,
    var losses: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var teamPosition: Int = 0,
    var goalDifference: Int = 0,
    var points: Int = 0,
    var hasPlayed: Boolean = false,
)
