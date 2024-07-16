package com.mustalk.minisimulator.data.remote

import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.data.utils.ApiResult
import com.mustalk.minisimulator.data.utils.exceptions.TeamFetchException
import com.mustalk.minisimulator.data.utils.helpers.TeamLogoHelper
import com.mustalk.minisimulator.domain.entities.teams.Team
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author by MusTalK on 16/07/2024
 *
 * A repository that fetches [Team] data from a remote API.
 *
 * This repository uses Retrofit to interact with the API and provides a list of [Team] objects.
 */
@Singleton
class RemoteTeamRepository
    @Inject
    constructor(
        private val teamApiService: ITeamApiService,
    ) : ITeamRepository {
        /**
         * Fetches a list of [Team] objects from the remote API, handling potential errors.
         *
         * @return A list of [Team] objects with team logos set.
         * @throws TeamFetchException If an error occurs while fetching teams from the API.
         */
        override suspend fun fetchTeams(): List<Team> =
            when (val result = teamApiService.getTeams()) {
                is ApiResult.Success -> TeamLogoHelper.mapTeamLogos(result.data)
                is ApiResult.Error -> throw TeamFetchException("Error fetching teams", result.exception)
            }
    }
