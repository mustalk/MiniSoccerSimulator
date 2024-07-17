package com.mustalk.minisimulator.domain.standings

import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.data.utils.exceptions.AssetLoadingException
import com.mustalk.minisimulator.data.utils.exceptions.TeamFetchException
import com.mustalk.minisimulator.domain.entities.teams.Team
import javax.inject.Inject

/**
 * @author by MusTalK on 16/07/2024
 *
 * Represents the standings of a group of teams, managing their matches and results.
 *
 * Provides functionality to:
 * - Retrieve the teams sorted by their performance (points, goal difference, etc.) [getTeams]
 */
class GroupStandings
    @Inject
    constructor(
        private val teamRepository: ITeamRepository,
    ) : IGroupStandings {
        private var teams: List<Team> = emptyList()

        suspend fun initializeTeams() {
            try {
                teams = teamRepository.fetchTeams()
            } catch (e: TeamFetchException) {
                println("Error fetching teams: ${e.message}")
            } catch (e: AssetLoadingException) {
                println("Error fetching teams: ${e.message}")
            }
        }

        /**
         * Retrieves the teams sorted by their performance (points, goal difference, etc.)
         */
        override fun getTeams(): List<Team> {
            // Check if teams are initialized
            return if (teams.isNotEmpty()) {
                teams.sortedWith(
                    compareByDescending<Team> { it.teamStats.points }
                        .thenByDescending { it.teamStats.goalDifference }
                        .thenByDescending { it.teamStats.goalsFor }
                        .thenBy { it.teamStats.goalsAgainst }
                )
            } else {
                // Return an empty list if teams are not initialized
                emptyList()
            }
        }
    }
