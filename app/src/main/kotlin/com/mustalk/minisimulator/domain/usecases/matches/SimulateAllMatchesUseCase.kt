package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for simulating all the matches in the [GroupStandings].
 *
 * @author by MusTalK on 18/07/2024
 */

class SimulateAllMatchesUseCase
    @Inject
    constructor() {
        /**
         * Simulates all matches, updating the [GroupStandings] accordingly.
         *
         * @param groupStandings The current group standings to simulate matches for.
         */
        operator fun invoke(groupStandings: GroupStandings) = groupStandings.simulateAllMatches()
    }
