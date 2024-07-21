package com.mustalk.minisimulator.presentation.matchresults.adapters

import com.mustalk.minisimulator.domain.entities.matches.Match

/**
 * Match click listener interface
 *
 * @author by MusTalK on 20/07/2024
 */

interface OnMatchClickListener {
    fun onMatchClick(
        roundMatches: List<Match>,
        roundNumber: Int,
    )
}
