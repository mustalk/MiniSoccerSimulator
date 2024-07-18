package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.entities.matches.Match
import javax.inject.Inject

/**
 * Use case class for checking if a match has been played.
 *
 * @author by MusTalK on 18/07/2024
 */

class HasMatchBeenPlayedUseCase
    @Inject
    constructor() {
        /**
         * Checks if the match has been played.
         *
         * @param match The match to check.
         * @return True if the match has been played, false otherwise.
         */
        operator fun invoke(match: Match): Boolean = match.homeTeam.teamStats.hasPlayed || match.awayTeam.teamStats.hasPlayed
    }
