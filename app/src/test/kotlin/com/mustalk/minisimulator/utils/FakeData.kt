package com.mustalk.minisimulator.utils

import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats

/**
 * Fake data for testing
 *
 * @author by MusTalK on 16/07/2024
 */

object FakeData {
    const val FAKE_NUM_TEAMS = 4

    val fakeTeamsPlayed =
        listOf(
            Team(
                "Team C",
                6,
                2131165348,
                TeamStats(1, 1, 0, 0, 4, 2, 1, 2, 3, true)
            ),
            Team(
                "Team D",
                3,
                2131165349,
                TeamStats(1, 1, 0, 0, 3, 2, 2, 1, 3, true)
            ),
            Team(
                "Team A",
                9,
                2131165346,
                TeamStats(1, 0, 0, 1, 2, 3, 3, -1, 0, true)
            ),
            Team(
                "Team B",
                4,
                2131165347,
                TeamStats(1, 0, 0, 1, 2, 4, 4, -2, 0, true)
            )
        )

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

    val fakeMatches =
        listOf(
            Match(
                homeTeam =
                    Team(
                        "Team A",
                        9,
                        2131165346,
                        TeamStats(1, 0, 1, 0, 3, 3, 2, 0, 1, true)
                    ),
                awayTeam =
                    Team(
                        "Team D",
                        3,
                        2131165349,
                        TeamStats(1, 0, 1, 0, 3, 3, 3, 0, 1, true)
                    )
            ),
            Match(
                homeTeam =
                    Team(
                        "Team B",
                        4,
                        2131165347,
                        TeamStats(1, 1, 0, 0, 2, 1, 1, 1, 3, true)
                    ),
                awayTeam =
                    Team(
                        "Team C",
                        6,
                        2131165348,
                        TeamStats(1, 0, 0, 1, 1, 2, 4, -1, 0, true)
                    )
            )
        )

    val fakeOddMatches =
        // Odd number of matches
        listOf(
            Match(
                homeTeam = Team("Team A", 9, 2131165346, TeamStats()),
                awayTeam = Team("Team B", 4, 2131165347, TeamStats())
            ),
            Match(
                homeTeam = Team("Team C", 6, 2131165348, TeamStats()),
                awayTeam = Team("Team D", 3, 2131165349, TeamStats())
            ),
            Match(
                homeTeam = Team("Team A", 9, 2131165346, TeamStats()),
                awayTeam = Team("Team C", 6, 2131165348, TeamStats())
            ),
            Match(
                homeTeam = Team("Team B", 4, 2131165347, TeamStats()),
                awayTeam = Team("Team D", 3, 2131165349, TeamStats())
            ),
            Match(
                homeTeam = Team("Team A", 9, 2131165346, TeamStats()),
                awayTeam = Team("Team D", 3, 2131165349, TeamStats())
            )
        )
}
