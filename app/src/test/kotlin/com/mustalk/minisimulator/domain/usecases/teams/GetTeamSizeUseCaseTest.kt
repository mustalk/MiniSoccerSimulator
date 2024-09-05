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
 * Unit tests for [GetTeamSizeUseCase].
 *
 * @author by MusTalK on 16/07/2024
 */

@RunWith(JUnit4::class)
class GetTeamSizeUseCaseTest {
    // Mocks the GroupStandings object
    @Mock
    private lateinit var groupStandings: GroupStandings

    // The use case being tested
    private lateinit var getTeamSizeUseCase: GetTeamSizeUseCase

    @Before
    fun setUp() {
        // Initializes mocks
        MockitoAnnotations.openMocks(this)
        // Creates an instance of the use case
        getTeamSizeUseCase = GetTeamSizeUseCase()
    }

    /**
     * Tests that the invoke function of the use case returns the correct team size from the GroupStandings.
     * Verifies that the size returned by the use case matches the expected size.
     */
    @Test
    fun `invoke returns correct team size from group standings`() {
        // Predefined list of fake teams
        val teams = fake3TeamsNotPlayed
        // Expected team size
        val expectedSize = teams.size

        // Mocks the getTeams method to return the predefined teams
        `when`(groupStandings.getTeams()).thenReturn(teams)

        // Calls the use case and gets the actual team size
        val actualSize = getTeamSizeUseCase(groupStandings)

        // Asserts that the actual size matches the expected size
        assertEquals(expectedSize, actualSize)
    }
}
