package com.mustalk.minisimulator.utils

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats

/**
 * Fake data for testing
 *
 * @author by MusTalK on 16/07/2024
 */

object FakeData {
    val fakeTeamsNotPlayed =
        listOf(
            Team("Team A", 9, 0, TeamStats()),
            Team("Team B", 4, 0, TeamStats()),
            Team("Team C", 6, 0, TeamStats()),
            Team("Team D", 3, 0, TeamStats())
        )

    val fake3TeamsNotPlayed =
        listOf(
            Team("Team A", 9, 0, TeamStats()),
            Team("Team B", 4, 0, TeamStats()),
            Team("Team C", 6, 0, TeamStats())
        )
}
