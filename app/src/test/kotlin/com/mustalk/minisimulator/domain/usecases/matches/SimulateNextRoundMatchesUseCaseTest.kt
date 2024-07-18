package com.mustalk.minisimulator.domain.usecases.matches

import com.mustalk.minisimulator.domain.standings.GroupStandings
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit test for [SimulateNextRoundMatchesUseCase]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class SimulateNextRoundMatchesUseCaseTest {
    // Mock the GroupStandings class
    @Mock
    private lateinit var groupStandings: GroupStandings

    private lateinit var simulateNextRoundMatchesUseCase: SimulateNextRoundMatchesUseCase

    @Before
    fun setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this)

        // Create an instance of the SimulateNextRoundMatchesUseCase
        simulateNextRoundMatchesUseCase = SimulateNextRoundMatchesUseCase()
    }

    @Test
    fun `invoke calls simulateNextRoundMatches on group standings`() {
        // Call the use case with the mocked groupStandings
        simulateNextRoundMatchesUseCase(groupStandings)

        // Verify that the simulateNextRoundMatches method was called on the groupStandings object
        verify(groupStandings).simulateNextRoundMatches()
    }
}
