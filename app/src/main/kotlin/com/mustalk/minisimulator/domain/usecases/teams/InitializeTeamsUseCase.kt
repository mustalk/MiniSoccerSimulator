package com.mustalk.minisimulator.domain.usecases.teams

import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for initializing the teams within the [GroupStandings].
 *
 * @author by MusTalK on 16/07/2024
 */
class InitializeTeamsUseCase
    @Inject
    constructor() {
        /**
         * Initializes the teams within the [GroupStandings], typically by fetching them from a data source.
         *
         * @param groupStandings The group standings to initialize the teams for.
         */
        suspend operator fun invoke(groupStandings: GroupStandings) = groupStandings.initializeTeams()
    }
