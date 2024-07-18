package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.match.IMatchSimulator
import com.mustalk.minisimulator.domain.team.ITeamStatsUpdater
import javax.inject.Inject

/**
 * Use case for simulating a single match and updating team statistics.
 *
 * @author by MusTalK on 18/07/2024
 */

class SimulateMatchUseCase
    @Inject
    constructor(
        private val matchSimulator: IMatchSimulator,
        private val statsUpdater: ITeamStatsUpdater,
    ) {
        /**
         * Simulates the given match and updates the statistics of the participating teams.*
         * @param match The match to simulate.
         * @return A Pair of [Team] objects representing the updated home team and away team, respectively.
         */
        operator fun invoke(match: Match): Pair<Team, Team> {
            // Simulate match result
            val (homeScore, awayScore) = matchSimulator.simulateResult(match.homeTeam, match.awayTeam)
            match.homeTeamScore = homeScore
            match.awayTeamScore = awayScore

            // Update team statistics
            return statsUpdater.updateStats(match.homeTeam, match.awayTeam, homeScore, awayScore)
        }
    }
