package com.mustalk.minisimulator.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustalk.minisimulator.data.utils.exceptions.AssetLoadingException
import com.mustalk.minisimulator.data.utils.exceptions.TeamFetchException
import com.mustalk.minisimulator.data.utils.exceptions.UncheckedAssetLoadingException
import com.mustalk.minisimulator.data.utils.exceptions.UncheckedTeamFetchException
import com.mustalk.minisimulator.domain.standings.GroupStandings
import com.mustalk.minisimulator.domain.usecases.matches.SimulateAllMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.matches.SimulateNextRoundMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.teams.InitializeTeamsUseCase
import com.mustalk.minisimulator.presentation.utils.ExceptionUtils
import com.mustalk.minisimulator.presentation.utils.exceptions.UnexpectedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for managing the state and actions related to group standings.
 *
 * This ViewModel is responsible for handling operations such as fetching teams, simulating matches,
 * and managing the state of loading indicators and error messages. It interacts with use cases to perform
 * these actions and exposes LiveData objects to observe changes in the state.
 *
 * @HiltViewModel Annotation used by Hilt for dependency injection.
 *
 * @param groupStandings Initial group standings data.
 * @param initializeTeamsUseCase Use case for initializing teams.
 * @param simulateAllMatchesUseCase Use case for simulating all matches.
 * @param simulateNextRoundMatchesUseCase Use case for simulating the next round of matches.
 *
 * @author by MusTalK on 20/07/2024
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val groupStandings: GroupStandings,
        private val initializeTeamsUseCase: InitializeTeamsUseCase,
        private val simulateAllMatchesUseCase: SimulateAllMatchesUseCase,
        private val simulateNextRoundMatchesUseCase: SimulateNextRoundMatchesUseCase,
    ) : ViewModel() {
        /** LiveData holding the current group standings data. */
        private val _groupStandingsLiveData = MutableLiveData<GroupStandings?>()
        val groupStandingsLiveData: LiveData<GroupStandings?> = _groupStandingsLiveData

        /** LiveData indicating whether a loading operation is in progress. */
        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> = _isLoading

        /** LiveData holding the current error message, if any. */
        private val _errorMessage = MutableLiveData<String?>()
        val errorMessage: LiveData<String?> = _errorMessage

        init {
            // Initialize the group standings LiveData with the injected group standings
            _groupStandingsLiveData.value = groupStandings
        }

        /**
         * Fetches teams from the repository and updates the group standings LiveData.
         */
        fun fetchTeams() {
            _isLoading.value = true // Show loading
            viewModelScope.launch {
                try {
                    ExceptionUtils.suspendWrapInUnexpectedException {
                        // Pass groupStandings to use case
                        initializeTeamsUseCase(groupStandings)
                    }
                    _groupStandingsLiveData.value = groupStandings
                    _isLoading.value = false // Hide loading
                } catch (e: TeamFetchException) {
                    _isLoading.value = false // Hide loading
                    _errorMessage.postValue(e.message ?: "Error fetching teams from API")
                } catch (e: AssetLoadingException) {
                    _isLoading.value = false // Hide loading
                    _errorMessage.postValue(e.message ?: "Error loading teams from assets")
                } catch (e: UncheckedTeamFetchException) {
                    // Unwrap the original exception
                    val originalException = e.cause as TeamFetchException
                    _errorMessage.postValue(originalException.message)
                } catch (e: UncheckedAssetLoadingException) {
                    // Unwrap the original exception
                    val originalException = e.cause as AssetLoadingException
                    _errorMessage.postValue(originalException.message)
                } catch (e: UnexpectedException) {
                    _errorMessage.postValue(e.message)
                }
            }
        }

        /**
         * Simulates all matches and updates the group standings LiveData.
         */
        fun simulateAllMatches() {
            viewModelScope.launch {
                try {
                    withContext(Dispatchers.Default) {
                        _groupStandingsLiveData.value?.let {
                            ExceptionUtils.wrapInUnexpectedException {
                                simulateAllMatchesUseCase(it)
                            }
                        }
                    }
                    // Update the LiveData on the main thread
                    _groupStandingsLiveData.postValue(_groupStandingsLiveData.value)
                } catch (e: IndexOutOfBoundsException) {
                    _errorMessage.postValue("Error simulating matches: Attempted to access an invalid index. Details: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    _errorMessage.postValue("Error simulating matches: Provided argument is not valid. Details: ${e.message}")
                } catch (e: UnexpectedException) {
                    _errorMessage.postValue("Unexpected error simulating matches: ${e.message}")
                }
            }
        }

        /**
         * Simulates the next round of matches and updates the group standings LiveData.
         */
        fun simulateNextRoundMatches() {
            viewModelScope.launch {
                try {
                    withContext(Dispatchers.Default) {
                        _groupStandingsLiveData.value?.let {
                            ExceptionUtils.wrapInUnexpectedException {
                                simulateNextRoundMatchesUseCase(it)
                            }
                        }
                    }
                    _groupStandingsLiveData.postValue(_groupStandingsLiveData.value) // Update on main thread
                } catch (e: IndexOutOfBoundsException) {
                    _errorMessage.postValue("Error simulating next round: Attempted to access an invalid index. Details: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    _errorMessage.postValue("Error simulating next round: Provided argument is not valid. Details: ${e.message}")
                } catch (e: UnexpectedException) {
                    _errorMessage.postValue("Unexpected error simulating next round: ${e.message}")
                }
            }
        }

        /**
         * Clears the error message from the ViewModel.
         */
        fun clearErrorMessage() {
            _errorMessage.value = null
        }
    }
