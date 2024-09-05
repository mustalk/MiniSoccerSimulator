package com.mustalk.minisimulator.data.remote

import android.content.Context
import android.content.res.AssetManager
import com.mustalk.minisimulator.data.deserializers.TeamDeserializer
import com.mustalk.minisimulator.data.utils.DataConstants
import com.mustalk.minisimulator.data.utils.helpers.NetworkHelper
import com.mustalk.minisimulator.utils.FakeJson.VALID_4TEAMS_JSON
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayInputStream

/**
 * Unit tests for [RemoteTeamRepository].
 *
 * @author by MusTalK on 16/07/2024
 */

@RunWith(JUnit4::class)
class RemoteTeamRepositoryTest {
    // The repository being tested
    private lateinit var remoteTeamRepository: RemoteTeamRepository

    // Mocked Android context
    private lateinit var mockContext: Context

    // Sets up the test environment before each test
    @Before
    fun setup() {
        // Valid JSON data for teams
        val jsonString = VALID_4TEAMS_JSON.trimIndent()

        // Creates a mock Context object
        mockContext = mock(Context::class.java)

        // Mocks AssetManager to provide the JSON data
        val mockAssetManager = mock(AssetManager::class.java)
        `when`(mockContext.assets).thenReturn(mockAssetManager)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        `when`(mockAssetManager.open("teams.json")).thenReturn(inputStream)

        // Creates a Retrofit instance with a mocked OkHttpClient and a custom deserializer
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(NetworkHelper.getInterceptor(mockContext))
                .build()
        val retrofit = NetworkHelper.getRetrofit(client, TeamDeserializer(), DataConstants.BASE_URL)
        val teamApiService = retrofit.create(ITeamApiService::class.java)

        // Initializes the repository with the mocked API service
        remoteTeamRepository = RemoteTeamRepository(teamApiService)
    }

    /**
     * Tests fetching teams from the remote API.
     * Verifies that the repository returns the expected list of teams from the mocked API response.
     */
    @Test
    fun `fetchTeams returns mocked list of teams from remote API`() =
        runBlocking {
            // Calls the fetchTeams method
            val teams = remoteTeamRepository.fetchTeams()

            // Asserts that the returned list contains 4 teams
            assertEquals(4, teams.size)
            // Asserts the name of the first team
            assertEquals("Team A", teams[0].name)
            // Asserts the strength of the first team
            assertEquals(9, teams[0].strength)
        }
}
