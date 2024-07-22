package com.mustalk.minisimulator.presentation.standings.models

/**
 * Data class for Team Standing Item used for the RecyclerView Adapter in `GroupStandingsFragment`.
 *
 * Holds information about a team's performance, including:
 * - Team position ([teamPosition])
 * - Team logo resource ID ([teamLogo])
 * - Team name ([teamName])
 * - Matches played ([pld])
 * - Wins ([w])
 * - Draws ([d])
 * - Losses ([l])
 * - Goals for and against ([gfGa])
 * - Goal difference ([gd])
 * - Points ([pts])
 *
 * @author by MusTalK on 22/07/2024
 */

data class TeamStandingItem(
    var teamPosition: Int = 0,
    val teamLogo: Int,
    val teamName: String,
    val pld: Int,
    val w: Int,
    val d: Int,
    val l: Int,
    val gfGa: String,
    val gd: String,
    val pts: Int,
)
