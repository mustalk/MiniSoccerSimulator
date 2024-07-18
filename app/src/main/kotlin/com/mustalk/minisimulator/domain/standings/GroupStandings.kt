package com.mustalk.minisimulator.domain.standings

import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.data.utils.exceptions.AssetLoadingException
import com.mustalk.minisimulator.data.utils.exceptions.TeamFetchException
import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.domain.match.IMatchGenerator
import com.mustalk.minisimulator.domain.usecases.matches.SimulateMatchUseCase
import javax.inject.Inject

/**
 * Represents the standings of a group of teams, managing their matches and results.
 *
 * Provides functionality to:
 * - Retrieve the teams sorted by their performance (points, goal difference, etc.) [getTeams]
 * - Retrieve the previous team positions. [getPreviousTeamPositions]
 * - Generate all possible matches between the teams using a [IMatchGenerator].
 * - Retrieve the matches. [getMatches]
 * - Simulate all possible matches between the teams. [simulateAllMatches]
 * - Simulate the next round of matches (two matches per round). [simulateNextRoundMatches]
 * - Reset the match results and team statistics. [resetMatches]
 *
 * @author by MusTalK on 16/07/2024
 */
class GroupStandings
    @Inject
    constructor(
        private val teamRepository: ITeamRepository,
        private val matchGenerator: IMatchGenerator,
        private val simulateMatchUseCase: SimulateMatchUseCase,
    ) : IGroupStandings {
        private var teams: List<Team> = emptyList()

        private var previousTeamPositions: MutableMap<String, Int> = mutableMapOf()
        private var matches: MutableList<Match> = mutableListOf()
        private var nextMatchSimulatorIndex = 0

        /**
         * Initializes the teams by fetching them from the repository.
         */
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

        /**
         * Retrieves the previous team positions.
         */
        override fun getPreviousTeamPositions(): MutableMap<String, Int> = previousTeamPositions

        /**
         * Retrieves the available matches for the group standings.
         */
        override fun getMatches(): List<Match> = matches

        /**
         * Simulates all matches between the available teams.
         */
        override fun simulateAllMatches() {
            resetMatches()
            generateAllMatches(teams)
            matches.forEach {
                simulateMatchUseCase(it) // Use the use case to simulate
            }
        }

        /**
         * Simulates the next round of matches and updates team positions.
         */
        override fun simulateNextRoundMatches() {
            val numberOfTeams = teams.size
            // The total number of matches for n teams in a round-robin tournament is given by: n * (n - 1) / 2
            val totalMatches = numberOfTeams * (numberOfTeams - 1) / 2

            // Check if we've simulated all matches, and start over again
            if (nextMatchSimulatorIndex >= totalMatches) {
                resetMatches()
            }

            // Generate the next round of matches
            generateNextRoundMatches(teams)

            if (nextMatchSimulatorIndex < matches.size) {
                val matchesToSimulate =
                    matches.subList(
                        nextMatchSimulatorIndex,
                        minOf(nextMatchSimulatorIndex + matches.size, matches.size)
                    )
                matchesToSimulate.forEach {
                    simulateMatchUseCase(it)
                }
                nextMatchSimulatorIndex += matchesToSimulate.size
            }

            // Update team positions after simulating matches
            updateTeamPositions()
        }

        /**
         * Resets the match simulator index, previous team positions, scores, and team stats.
         */
        override fun resetMatches() {
            // Reset match simulator index
            nextMatchSimulatorIndex = 0

            // Reset previous team positions
            previousTeamPositions = mutableMapOf()

            // Reset scores for each match
            matches = mutableListOf()

            // Reset stats for each team (create new Team objects with default stats)
            teams =
                teams.map {
                    Team(
                        name = it.name,
                        strength = it.strength,
                        teamLogo = it.teamLogo,
                        teamStats = TeamStats()
                    )
                }
        }

        // Generate all possible matches between the teams
        private fun generateAllMatches(teams: List<Team>) {
            matches.addAll(matchGenerator.generateMatches(teams))
        }

        // Generate the next round of matches
        private fun generateNextRoundMatches(teams: List<Team>) {
            matches.addAll(matchGenerator.generateNextRoundMatches(teams))
        }

        // Update team positions
        private fun updateTeamPositions() {
            val updatedTeams = getTeams() // Get sorted list of teams
            updatedTeams.forEachIndexed { index, team ->
                team.teamStats.teamPosition = index + 1 // Update teamPosition in Team object
            }
        }
    }
