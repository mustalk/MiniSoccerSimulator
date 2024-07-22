package com.mustalk.minisimulator.presentation.standings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mustalk.minisimulator.databinding.FragmentGroupStandingsBinding
import com.mustalk.minisimulator.domain.usecases.teams.GetTeamsUseCase
import com.mustalk.minisimulator.presentation.main.MainViewModel
import com.mustalk.minisimulator.presentation.standings.adapters.TeamStandingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment for displaying group standings
 *
 * @author by MusTalK on 22/07/2024
 */

@AndroidEntryPoint
class GroupStandingsFragment : Fragment() {
    @Inject
    lateinit var getTeamsUseCase: GetTeamsUseCase

    private lateinit var adapter: TeamStandingsAdapter
    private lateinit var mainViewModel: MainViewModel
    internal val groupStandingsViewModel: GroupStandingsViewModel by viewModels<GroupStandingsViewModel>()

    private var _binding: FragmentGroupStandingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGroupStandingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter
        adapter =
            TeamStandingsAdapter(
                qualifiers = listOf(),
                totalRounds = 0,
                previousTeamPositions = mutableMapOf()
            )
        binding.standingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.standingsRecyclerView.adapter = adapter

        // Observe standings LiveData and update the adapter
        groupStandingsViewModel.standings.observe(viewLifecycleOwner) { standings ->
            adapter.submitList(standings)
        }

        // Observe hasPlayedMatches LiveData and toggle the empty view
        groupStandingsViewModel.hasPlayedMatches.observe(viewLifecycleOwner) { hasPlayed ->
            toggleEmptyView(!hasPlayed)
        }

        // Observe groupStandings from MainViewModel and update GroupStandingsViewModel
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.groupStandingsLiveData.observe(viewLifecycleOwner) { standings ->
            standings?.let {
                // Use GetTeamsUseCase to fetch teams from the observed groupStandingsLiveData and update GroupStandingsViewModel
                val teams = getTeamsUseCase(it)
                groupStandingsViewModel.updateStandings(teams)

                val numberOfTeams = teams.size
                val numberOfQualifiers = numberOfTeams / 2 // Assuming top 50% teams qualify
                val totalRounds = numberOfTeams - 1

                // Update qualifiers
                val qualifiers = teams.take(numberOfQualifiers).map { team -> team.name }
                adapter.updateQualifiers(qualifiers, totalRounds)
            }
        }
    }

    private fun toggleEmptyView(isEmpty: Boolean) {
        // Toggle the visibility of the empty view
        _binding?.let {
            it.standingsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            it.emptyViewContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding when the fragment is destroyed.
        _binding = null
    }
}
