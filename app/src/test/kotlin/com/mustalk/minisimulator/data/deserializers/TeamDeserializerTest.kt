package com.mustalk.minisimulator.data.deserializers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.domain.entities.teams.TeamStats
import com.mustalk.minisimulator.utils.FakeData.fakeTeamsNotPlayed
import com.mustalk.minisimulator.utils.FakeJson.INVALID_B_TEAM_JSON
import com.mustalk.minisimulator.utils.FakeJson.VALID_4TEAMS_JSON
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for [TeamDeserializer].
 *
 * @author by MusTalK on 16/07/2024
 */

@RunWith(JUnit4::class)
class TeamDeserializerTest {
    private lateinit var gson: Gson

    // Sets up the Gson instance with the TeamDeserializer before each test
    @Before
    fun setUp() {
        gson =
            GsonBuilder()
                .registerTypeAdapter(List::class.java, TeamDeserializer())
                .create()
    }

    /**
     * Tests deserialization of a valid JSON string into a list of Team objects.
     * Verifies that the deserialized teams match the expected fake teams.
     */
    @Test
    fun `deserialize valid json to list of Team objects`() {
        // Valid JSON string for 4 teams
        val jsonString = VALID_4TEAMS_JSON.trimIndent()
        // Expected Team objects
        val expectedTeams = fakeTeamsNotPlayed

        // Deserialize the JSON string into a list of Team objects using Gson, using the TypeToken to get the correct type of the list
        val deserializedTeams =
            gson.fromJson<List<Team>>(
                jsonString,
                object : TypeToken<List<Team>>() {}.type
            )

        // Asserts equality of expected and deserialized teams
        assertEquals(expectedTeams, deserializedTeams)
    }

    /**
     * Tests deserialization of a JSON string with missing fields.
     * Verifies that the deserializer handles missing fields gracefully and creates Team objects with default values.
     */
    @Test
    fun `deserialize json with missing fields`() {
        // JSON string with missing fields for Team B
        val jsonString = INVALID_B_TEAM_JSON.trimIndent()

        // Expected Team object with default values for missing fields
        val expectedTeams =
            listOf(
                Team(
                    name = "Team B",
                    strength = 0,
                    teamStats = TeamStats(),
                    teamLogo = 0
                )
            )

        // Deserialize the JSON string into a list of Team objects using Gson, using the TypeToken to get the correct type of the list
        val deserializedTeams =
            gson.fromJson<List<Team>>(
                jsonString,
                object : TypeToken<List<Team>>() {}.type
            )

        // Asserts equality of expected and deserialized teams
        assertEquals(expectedTeams, deserializedTeams)
    }
}
