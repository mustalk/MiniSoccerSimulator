package com.mustalk.minisimulator.domain.usecases.teams

import com.mustalk.minisimulator.domain.standings.GroupStandings
import com.mustalk.minisimulator.utils.FakeData.fake3TeamsNotPlayed
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for [GetTeamsUseCase].
 *
 * @author by MusTalK on 16/07/2024
 */

@RunWith(JUnit4::class)
class GetTeamsUseCaseTest {
    // Mocks the GroupStandings object
    @Mock
    private lateinit var groupStandings: GroupStandings

    // The use case being tested
    private lateinit var getTeamsUseCase: GetTeamsUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Creates an instance of the use case
        getTeamsUseCase = GetTeamsUseCase()
    }

    /**
     * Tests that the invoke function of the use case returns the correct list of teams from the GroupStandings.
     * Verifies that the list of teams returned by the use case matches the expected list.
     */
    @Test
    fun `invoke returns teams from group standings`() {
        // Predefined list of fake teams
        val expectedTeams = fake3TeamsNotPlayed

        // Mocks the getTeams method to return the predefined teams
        `when`(groupStandings.getTeams()).thenReturn(expectedTeams)

        // Calls the use case and gets the actual list of teams
        val actualTeams = getTeamsUseCase(groupStandings)

        // Asserts that the actual teams match the expected teams
        assertEquals(expectedTeams, actualTeams)
    }
}
