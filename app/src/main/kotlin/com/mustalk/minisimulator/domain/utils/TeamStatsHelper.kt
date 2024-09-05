package com.mustalk.minisimulator.domain.utils

/**
 * Helper object for calculating team statistics.
 *
 * @author by MusTalK on 16/07/2024
 */
object TeamStatsHelper {
    /**
     * Calculates the goal difference for a team.
     *
     * @param goalsFor The number of goals the team has scored.
     * @param goalsAgainst The number of goals the team has conceded.
     * @return The goal difference (goalsFor - goalsAgainst).
     */
    fun calculateGoalDifference(
        goalsFor: Int,
        goalsAgainst: Int,
    ): Int = goalsFor - goalsAgainst

    /**
     * Calculates the total points for a team.
     *
     * @param wins The number of matches the team has won.
     * @param draws The number of matches the team has drawn.
     * @return The total points (wins * POINTS_PER_WIN + draws).
     */
    fun calculatePoints(
        wins: Int,
        draws: Int,
    ): Int = wins * DomainConstants.POINTS_PER_WIN + draws

    /**
     * Checks if a team has played any matches.
     *
     * @param matchesPlayed The number of matches the team has played.
     * @return True if the team has played matches (matchesPlayed > 0), false otherwise.
     */
    fun hasPlayedMatches(matchesPlayed: Int): Boolean = matchesPlayed > 0
}
