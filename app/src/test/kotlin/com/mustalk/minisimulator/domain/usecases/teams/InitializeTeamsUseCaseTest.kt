package com.mustalk.minisimulator.domain.usecases.teams

import com.mustalk.minisimulator.domain.standings.GroupStandings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * @author by MusTalK on 16/07/2024
 *
 * Unit tests for [InitializeTeamsUseCase].
 */

@RunWith(JUnit4::class)
// Indicates that experimental coroutines APIs are being used
@ExperimentalCoroutinesApi
class InitializeTeamsUseCaseTest {
    // Mocks the GroupStandings object
    @Mock
    private lateinit var groupStandings: GroupStandings

    // The use case being tested
    private lateinit var initializeTeamsUseCase: InitializeTeamsUseCase

    @Before
    fun setUp() {
        // Initializes mocks
        MockitoAnnotations.openMocks(this)
        // Creates an instance of the use case
        initializeTeamsUseCase = InitializeTeamsUseCase()
    }

    /**
     * Tests that the invoke function of the use case calls the initializeTeams function on the GroupStandings object.
     * Verifies that the initializeTeams function is invoked when the use case is executed.
     * uses runTest to run the coroutine in a test context, as the initializeTeams function is suspendable.
     */
    @Test
    fun `invoke calls initializeTeams on group standings`() =
        runBlocking {
            // Calls the use case
            initializeTeamsUseCase(groupStandings)

            // Verifies that the initializeTeams function was called on the GroupStandings object
            verify(groupStandings).initializeTeams()
        }
}
