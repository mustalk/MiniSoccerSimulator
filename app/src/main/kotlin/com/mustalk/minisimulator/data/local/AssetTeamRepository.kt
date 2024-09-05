package com.mustalk.minisimulator.data.local

import android.content.Context
import com.google.gson.JsonParseException
import com.mustalk.minisimulator.data.utils.exceptions.AssetLoadingException
import com.mustalk.minisimulator.data.utils.helpers.AssetJsonReader
import com.mustalk.minisimulator.data.utils.helpers.TeamJsonParser
import com.mustalk.minisimulator.data.utils.helpers.TeamLogoHelper
import com.mustalk.minisimulator.domain.entities.teams.Team
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

/**
 * A repository that provides a list of [Team] objects from assets.
 *
 * This repository reads team data from a JSON file located in the assets folder
 * and parses it into a list of Team objects.
 *
 * @author by MusTalK on 16/07/2024
 */
class AssetTeamRepository
    @Inject
    constructor(
        @ApplicationContext
        var context: Context,
    ) : ITeamRepository {
        /**
         * Fetches a list of [Team] objects from a JSON file in assets.
         *
         * @return A list of [Team] objects parsed from the JSON file.
         * @throws IOException If an error occurs while reading the JSON file.
         * @throws JsonParseException If an error occurs while parsing the JSON data.
         */
        override suspend fun fetchTeams(): List<Team> =
            try {
                val jsonString = AssetJsonReader.readJsonFromAssets(context, "teams.json")
                val teams = TeamJsonParser.parseTeamsFromJson(jsonString)
                TeamLogoHelper.mapTeamLogos(teams)
            } catch (e: IOException) {
                // Handle file reading errors (e.g., file not found)
                throw AssetLoadingException("Error reading teams data", e)
            } catch (e: JsonParseException) {
                // Handle JSON parsing errors (e.g., invalid JSON format)
                throw AssetLoadingException("Error parsing teams data", e)
            }
    }
