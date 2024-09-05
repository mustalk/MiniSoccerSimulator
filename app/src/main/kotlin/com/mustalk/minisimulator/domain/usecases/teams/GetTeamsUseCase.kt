package com.mustalk.minisimulator.domain.usecases.teams

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for retrieving the list of teams from the [GroupStandings].
 *
 * @author by MusTalK on 16/07/2024
 */
class GetTeamsUseCase
    @Inject
    constructor() {
        /**
         * Retrieves the list of teams.
         *
         * @param groupStandings The current group standings.
         * @return The list of teams.
         */
        operator fun invoke(groupStandings: GroupStandings): List<Team> = groupStandings.getTeams()
    }
