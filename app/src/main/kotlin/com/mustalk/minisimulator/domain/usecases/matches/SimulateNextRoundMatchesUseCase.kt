package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.standings.GroupStandings
import javax.inject.Inject

/**
 * Use case for simulating the next round of matches in the [GroupStandings].
 *
 * @author by MusTalK on 18/07/2024
 */

class SimulateNextRoundMatchesUseCase
    @Inject
    constructor() {
        /**
         * Simulates the next round of matches, updating the [GroupStandings] accordingly.
         *
         * @param groupStandings The current group standings to simulate the next round of matches for.
         */
        operator fun invoke(groupStandings: GroupStandings) = groupStandings.simulateNextRoundMatches()
    }
