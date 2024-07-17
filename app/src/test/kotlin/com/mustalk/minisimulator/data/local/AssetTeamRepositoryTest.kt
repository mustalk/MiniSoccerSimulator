package com.mustalk.minisimulator.data.local

import android.content.Context
import android.content.res.AssetManager
import com.mustalk.minisimulator.data.utils.exceptions.AssetLoadingException
import com.mustalk.minisimulator.data.utils.exceptions.UncheckedAssetLoadingException
import com.mustalk.minisimulator.utils.FakeJson.INVALID_TEAMS_JSON
import com.mustalk.minisimulator.utils.FakeJson.VALID_2TEAMS_JSON
import com.mustalk.minisimulator.utils.FakeJson.VALID_4TEAMS_JSON
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * @author by MusTalK on 16/07/2024
 *
 * Unit tests for [AssetTeamRepository].
 */
@RunWith(JUnit4::class)
class AssetTeamRepositoryTest {
    // Mocks the Context object
    @Mock
    private lateinit var context: Context

    // Mocks the AssetManager object
    @Mock
    private lateinit var assetManager: AssetManager

    // The repository being tested
    private lateinit var repository: AssetTeamRepository

    // Sets up the test environment before each test
    @Before
    fun setUp() {
        // Initializes mocks
        MockitoAnnotations.openMocks(this)
        // Mocks context.assets to return the mocked AssetManager
        `when`(context.assets).thenReturn(assetManager)
        // Creates an instance of the repository with the mocked context
        repository = AssetTeamRepository(context)
    }

    /**
     * Tests that fetchTeams returns the correct number of teams when provided with valid JSON data.
     */
    @Test
    fun `fetchTeams returns correct number of teams from valid JSON`() =
        runBlocking {
            // Valid JSON string for 4 teams
            val jsonString = VALID_4TEAMS_JSON.trimIndent()

            // Mocks assetManager.open to return the JSON data
            `when`(assetManager.open("teams.json")).thenReturn(jsonString.byteInputStream())

            // Calls the fetchTeams method
            val teams = repository.fetchTeams()

            // Asserts that the returned list contains 4 teams
            assertEquals(4, teams.size)
        }

    /**
     * Tests that fetchTeams correctly parses team data from valid JSON.
     * Verifies the name and strength of the first two teams.
     */
    @Test
    fun `fetchTeams parses team data correctly from valid JSON`() =
        runBlocking {
            // Valid JSON string for 2 teams
            val jsonString = VALID_2TEAMS_JSON.trimIndent()
            // Mocks assetManager.open
            `when`(assetManager.open("teams.json")).thenReturn(jsonString.byteInputStream())

            // Calls fetchTeams
            val teams = repository.fetchTeams()

            // Asserts the name of the first team
            assertEquals("Team A", teams[0].name)
            // Asserts the strength of the first team
            assertEquals(9, teams[0].strength)
            // Asserts the name of the second team
            assertEquals("Team B", teams[1].name)
            // Asserts the strength of the second team
            assertEquals(4, teams[1].strength)
        }

    /**
     * Tests that fetchTeams throws an exception when the specified JSON file is not found.
     * Uses assertThrows to verify that an UncheckedAssetLoadingException is thrown.
     */
    @Test
    fun `fetchTeams throws exception when file not found`() =
        runTest {
            // Mocks assetManager.open to throw an exception when the file is not found
            doThrow(UncheckedAssetLoadingException(AssetLoadingException("File not found")))
                .whenever(assetManager)
                .open("teams.json")

            assertThrows(UncheckedAssetLoadingException::class.java) {
                // Asserts that an exception is thrown
                runBlocking {
                    // Calls fetchTeams within a runBlocking block
                    repository.fetchTeams()
                }
            }
        }

    /**
     * Tests the behavior of fetchTeams when provided with invalid JSON data.
     * Verifies that an empty list is returned in this scenario.
     */
    @Test
    fun `fetchTeams returns empty list for invalid JSON`() =
        runBlocking {
            // Invalid JSON string
            val invalidJsonString = INVALID_TEAMS_JSON.trimIndent()
            // Mocks assetManager.open
            `when`(assetManager.open("teams.json")).thenReturn(invalidJsonString.byteInputStream())

            // Calls fetchTeams
            val teams = repository.fetchTeams()

            // Asserts that the returned list is empty
            assertEquals(0, teams.size)
        }
}
