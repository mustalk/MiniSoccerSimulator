package com.mustalk.minisimulator.domain.team

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.domain.utils.TeamStatsHelper
import javax.inject.Inject

/**
 * @author by MusTalK on 16/07/2024
 *
 * A default implementation of [ITeamStatsUpdater] that updates team statistics based on match results.
 **/
class TeamStatsUpdater
    @Inject
    constructor() : ITeamStatsUpdater {
        /**
         * Updates the statistics of the given home and away teams based on the match result.
         *
         * This method increments matches played, updates goals for and against, and adjusts wins, draws, and losses accordingly.
         *
         * @param homeTeam The home [Team] object.
         * @param awayTeam The away [Team] object.
         * @param homeScore The score of the home team.
         * @param awayScore The score of the away team.
         * @return A [Pair] of the updated [Team] objects.
         **/
        override fun updateStats(
            homeTeam: Team,
            awayTeam: Team,
            homeScore: Int,
            awayScore: Int,
        ): Pair<Team, Team> {
            updateTeamStats(homeTeam.teamStats, homeScore, awayScore)
            updateTeamStats(awayTeam.teamStats, awayScore, homeScore) // Note: Scores are swapped for away team

            // Return the updated teams (no need to create new instances)
            return Pair(homeTeam, awayTeam)
        }

        private fun updateTeamStats(
            teamStats: TeamStats,
            goalsFor: Int,
            goalsAgainst: Int,
        ) {
            with(teamStats) {
                matchesPlayed++
                this.goalsFor += goalsFor
                this.goalsAgainst += goalsAgainst

                when {
                    goalsFor > goalsAgainst -> wins++
                    goalsFor < goalsAgainst -> losses++
                    else -> draws++
                }

                // Calculate goalDifference, points and hasPlayed
                goalDifference = TeamStatsHelper.calculateGoalDifference(this.goalsFor, this.goalsAgainst)
                points = TeamStatsHelper.calculatePoints(wins, draws)
                hasPlayed = TeamStatsHelper.hasPlayedMatches(matchesPlayed)
            }
        }
    }
