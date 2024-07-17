package com.mustalk.minisimulator.data.remote

import com.mustalk.minisimulator.data.utils.ApiResult
import com.mustalk.minisimulator.domain.entities.teams.Team
import retrofit2.http.GET

/**
 * @author by MusTalK on 16/07/2024
 *
 * An interface defining the API endpoints for fetching [Team] data.
 */
interface ITeamApiService {
    /**
     * Fetches a list of [Team] objects from the API, handling potential errors.
     * @return A [Result] object wrapping either a list of [Team] objects or an exception.
     */
    @GET("teams")
    suspend fun getTeams(): ApiResult<List<Team>>
}
