package com.mustalk.minisimulator.presentation.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mustalk.minisimulator.domain.standings.GroupStandings
import com.mustalk.minisimulator.domain.usecases.matches.SimulateAllMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.matches.SimulateNextRoundMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.teams.InitializeTeamsUseCase
import com.mustalk.minisimulator.presentation.utils.exceptions.UnexpectedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever

/**
 * Unit tests for [MainViewModel].
 *
 * @author by MusTalK on 15/07/2024
 */
@ExperimentalCoroutinesApi
class MainViewModelTest {
    /**
     * Ensures that all background tasks related to Architecture Components execute synchronously on the main thread during tests.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var groupStandings: GroupStandings

    @Mock
    private lateinit var initializeTeamsUseCase: InitializeTeamsUseCase

    @Mock
    private lateinit var simulateAllMatchesUseCase: SimulateAllMatchesUseCase

    @Mock
    private lateinit var simulateNextRoundMatchesUseCase: SimulateNextRoundMatchesUseCase

    private lateinit var mainViewModel: MainViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for testing
        mainViewModel =
            MainViewModel(
                groupStandings,
                initializeTeamsUseCase,
                simulateAllMatchesUseCase,
                simulateNextRoundMatchesUseCase
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after testing
    }

    /**
     * Verifies that fetching teams triggers the use case and updates LiveData.
     */
    @Test
    fun `fetchTeams triggers initializeTeamsUseCase and updates LiveData`() =
        runBlocking {
            mainViewModel.fetchTeams()

            // Verify group standings are updated in LiveData
            assert(mainViewModel.groupStandingsLiveData.value == groupStandings)
        }

    /**
     * Verifies that simulating all matches triggers the use case and updates LiveData.
     */
    @Test
    fun `simulateAllMatches triggers use case and updates LiveData`() =
        runBlocking {
            mainViewModel.simulateAllMatches()

            // Verify group standings are updated in LiveData
            assert(mainViewModel.groupStandingsLiveData.value == groupStandings)
        }

    /**
     * Verifies that simulating the next round triggers the use case and updates LiveData.
     */
    @Test
    fun `simulateNextRoundMatches triggers use case and updates LiveData`() =
        runBlocking {
            mainViewModel.simulateNextRoundMatches()

            // Verify group standings are updated in LiveData
            assert(mainViewModel.groupStandingsLiveData.value == groupStandings)
        }

    /**
     * Verifies error handling when fetching teams fails.
     */
    @Test
    fun `fetchTeams handles errors and updates errorMessage LiveData`() =
        runBlocking {
            // Mock the use case to throw an exception
            val exception = UnexpectedException("Test Exception")

            doThrow(exception).whenever(initializeTeamsUseCase).invoke(groupStandings)

            mainViewModel.fetchTeams()

            // Verify error message is updated in LiveData
            assert(mainViewModel.errorMessage.value == "Test Exception")
        }
}
