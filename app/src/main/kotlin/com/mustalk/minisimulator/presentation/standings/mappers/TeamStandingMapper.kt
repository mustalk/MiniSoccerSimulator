package com.mustalk.minisimulator.presentation.standings.mappers

import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.presentation.standings.models.TeamStandingItem
import javax.inject.Inject

/**
 * Maps [Team] objects to [TeamStandingItem] objects, used for displaying in a standings list.
 *
 * @author by MusTalK on 22/07/2024
 */

class TeamStandingMapper
    @Inject
    constructor() {
        fun mapToTeamStandingItem(team: Team): TeamStandingItem {
            val stats = team.teamStats
            return TeamStandingItem(
                teamPosition = stats.teamPosition,
                teamLogo = team.teamLogo,
                teamName = team.name,
                pld = stats.matchesPlayed,
                w = stats.wins,
                d = stats.draws,
                l = stats.losses,
                gfGa = "${stats.goalsFor}-${stats.goalsAgainst}",
                gd = stats.goalDifference.toString(),
                pts = stats.points
            )
        }
    }
