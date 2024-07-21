package com.mustalk.minisimulator.presentation.matchresults

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustalk.minisimulator.domain.entities.matches.Match
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for holding and managing match results data.
 *
 * @author by MusTalK on 20/07/2024
 */

@HiltViewModel
class MatchResultsViewModel
    @Inject
    constructor() : ViewModel() {
        // LiveData to hold the list of paired matches
        private val _pairedMatches = MutableLiveData<List<List<Match>>>()

        // Expose the LiveData to the UI
        val pairedMatches: LiveData<List<List<Match>>> = _pairedMatches

        // Function to generate paired matches based on the list of matches
        fun generatePairedMatches(
            matches: List<Match>,
            numTeams: Int,
        ) {
            viewModelScope.launch {
                if (matches.isEmpty()) {
                    _pairedMatches.value = emptyList()
                    return@launch
                }

                val matchesPerRound = numTeams / 2
                val pairedMatches = matches.chunked(matchesPerRound)
                _pairedMatches.value = pairedMatches
            }
        }
    }
