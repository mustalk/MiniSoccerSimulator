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
 * Unit test for [SimulateAllMatchesUseCase]
 *
 * @author by MusTalK on 18/07/2024
 */

@RunWith(JUnit4::class)
class SimulateAllMatchesUseCaseTest {
    // Mocks the GroupStandings class
    @Mock
    private lateinit var groupStandings: GroupStandings

    // The use case being tested
    private lateinit var simulateAllMatchesUseCase: SimulateAllMatchesUseCase

    @Before
    fun setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this)

        // Create an instance of the SimulateAllMatchesUseCase
        simulateAllMatchesUseCase = SimulateAllMatchesUseCase()
    }

    @Test
    fun `invoke calls simulateAllMatches on group standings`() {
        // Call the use case with the mocked groupStandings
        simulateAllMatchesUseCase(groupStandings)

        // Verify that the simulateAllMatches method was called on the groupStandings object
        verify(groupStandings).simulateAllMatches()
    }
}
