package com.mustalk.minisimulator.domain.usecases.teams

import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for retrieving the number of teams in the [GroupStandings].
 *
 * @author by MusTalK on 16/07/2024
 */
class GetTeamSizeUseCase
    @Inject
    constructor() {
        /**
         * Retrieves the number of teams in the group standings.
         *
         * @param groupStandings The current group standings.
         * @return The number of teams in the group standings.
         */
        operator fun invoke(groupStandings: GroupStandings): Int = groupStandings.getTeams().size
    }
