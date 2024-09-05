package com.mustalk.minisimulator.data.utils.helpers

import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * A utility object for retrieving team logos and mapping them to teams.
 *
 * @author by MusTalK on 16/07/2024
 */
object TeamLogoHelper {
    /**
     * Maps team logos to a list of [Team] objects.
     *
     * @param teams The list of [Team] objects to map logos to.
     * @return A list of [Team] objects with team logos set.
     */
    fun mapTeamLogos(teams: List<Team>): List<Team> =
        teams.map { team ->
            team.copy(teamLogo = getTeamLogo(team.name))
        }

    /**
     * Retrieves some placeholder drawable resource ID for the logo corresponding to the given team name.
     *
     * @param teamName The name of the team.
     * @return The drawable resource ID for the team's logo, or a default drawable if no match is found.
     */
    private fun getTeamLogo(teamName: String): Int =
        when (getTeamName(teamName)) {
            "A" -> R.drawable.ic_team_a
            "B" -> R.drawable.ic_team_b
            "C" -> R.drawable.ic_team_c
            "D" -> R.drawable.ic_team_d
            else -> R.drawable.ic_simulate_icon
        }

    private fun getTeamName(clubName: String): String {
        val words = clubName.trim().split("\\s+".toRegex())
        return if (words.size >= 2) words[1] else ""
    }
}
