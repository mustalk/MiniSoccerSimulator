package com.mustalk.minisimulator.presentation.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * @author by MusTalK on 15/07/2024
 *
 * A placeholder unit test for [MainViewModel].
 */

class MainViewModelTest {
    /**
     * Ensures that all background tasks related to Architecture Components execute synchronously on the main thread during tests.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mainViewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for testing
        mainViewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after testing
    }

    @Test
    fun `view model instantiated successfully`() {
        // Given - (Nothing to set up in this case)

        // When - ViewModel is created
        val viewModel = MainViewModel()

        // Then - Assert that the ViewModel is not null
        assertNotNull(viewModel)
    }
}
