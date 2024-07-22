package com.mustalk.minisimulator.presentation.standings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustalk.minisimulator.domain.entities.teams.Team
import com.mustalk.minisimulator.presentation.standings.mappers.TeamStandingMapper
import com.mustalk.minisimulator.presentation.standings.models.TeamStandingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for holding and managing group standings data
 *
 * @author by MusTalK on 22/07/2024
 */

@HiltViewModel
class GroupStandingsViewModel
    @Inject
    constructor(
        private val teamStandingMapper: TeamStandingMapper,
    ) : ViewModel() {
        private val _standings = MutableLiveData<List<TeamStandingItem>>()
        val standings: LiveData<List<TeamStandingItem>> = _standings

        private val _hasPlayedMatches = MutableLiveData<Boolean>()
        val hasPlayedMatches: LiveData<Boolean> = _hasPlayedMatches

        fun updateStandings(teams: List<Team>) {
            viewModelScope.launch {
                val groupStandings = teams.map { teamStandingMapper.mapToTeamStandingItem(it) }
                _standings.value = groupStandings
                _hasPlayedMatches.value = teams.any { it.teamStats.hasPlayed }
            }
        }
    }
