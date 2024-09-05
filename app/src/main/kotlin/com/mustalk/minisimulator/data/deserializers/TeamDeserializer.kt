package com.mustalk.minisimulator.data.deserializers

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import java.lang.reflect.Type

/**
 * A custom deserializer for a list of [Team] objects.
 *
 * This deserializer handles the deserialization of JSON arrays into a list of [Team] objects.
 * It iterates through the JSON array, extracts relevant data for each team, and creates a list
 * of [Team] instances.
 *
 * @author by MusTalK on 16/07/2024
 */

class TeamDeserializer : JsonDeserializer<List<Team>> {
    /**
     * Deserializes a JSON element into a list of [Team] objects.
     *
     * @param json The JSON element to deserialize, expected to be a JSON array.
     * @param typeOfT The type of the list, which should be List<[Team]>.
     * @param context The deserialization context.
     * @return A list of [Team] objects deserialized from the JSON array.
     */
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): List<Team> {
        val teams = mutableListOf<Team>()
        // Early return if not a JsonArray
        if (json !is JsonArray) return teams

        json.forEach { element ->
            // Skip non-JsonObjects
            if (element !is JsonObject) return@forEach

            val jsonObject = element.asJsonObject
            // Skip objects without "name"
            if (!jsonObject.has("name")) return@forEach

            val name = jsonObject.get("name").asString
            val strength = jsonObject.get("strength")?.asInt ?: 0

            val team =
                Team(
                    name = name,
                    strength = strength,
                    teamStats = TeamStats(),
                    teamLogo = 0
                )
            teams.add(team)
        }

        return teams
    }
}
