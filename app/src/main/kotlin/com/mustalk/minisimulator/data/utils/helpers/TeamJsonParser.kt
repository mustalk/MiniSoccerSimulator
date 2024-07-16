package com.mustalk.minisimulator.data.utils.helpers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mustalk.minisimulator.data.deserializers.TeamDeserializer
import com.mustalk.minisimulator.domain.entities.teams.Team

/**
 * @author by MusTalK on 16/07/2024
 *
 * A helper class to parse JSON data into a list of [Team] objects, using a custom deserializer [TeamDeserializer].
 */
object TeamJsonParser {
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(List::class.java, TeamDeserializer())
            .create()

    /**
     * Parses a JSON string into a list of [Team] objects.
     *
     * @param jsonString The JSON string to parse.
     * @return A list of [Team] objects parsed from the JSON string.
     */
    fun parseTeamsFromJson(jsonString: String): List<Team> = gson.fromJson(jsonString, object : TypeToken<List<Team>>() {}.type)
}
