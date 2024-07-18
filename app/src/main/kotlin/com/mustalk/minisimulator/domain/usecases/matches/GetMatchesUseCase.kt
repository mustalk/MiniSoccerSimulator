package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for retrieving the list of matches from the [GroupStandings].
 *
 * @author by MusTalK on 18/07/2024
 */

class GetMatchesUseCase
    @Inject
    constructor() {
        /**
         * Retrieves the list of matches.
         *
         * @param groupStandings The current group standings.* @return The list of matches.
         */
        operator fun invoke(groupStandings: GroupStandings): List<Match> = groupStandings.getMatches()
    }
