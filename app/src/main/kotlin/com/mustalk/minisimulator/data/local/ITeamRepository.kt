package com.mustalk.minisimulator.data.local

import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * An interface defining the contract for accessing and managing [Team] data.
 *
 * Provides a method to asynchronously fetch a list of [Team] objects.
 *
 * @author by MusTalK on 16/07/2024
 */
interface ITeamRepository {
    /**
     * Asynchronously fetches a list of [Team] objects.
     * @return A list of [Team] objects.
     * @throws Exception if an error occurs while fetching teams from the API.
     */
    @Throws(Exception::class)
    suspend fun fetchTeams(): List<Team>
}
