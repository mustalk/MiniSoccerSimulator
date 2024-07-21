package com.mustalk.minisimulator.presentation.matchresults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mustalk.minisimulator.databinding.FragmentMatchResultsBinding
import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.domain.usecases.matches.GetMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.matches.HasMatchBeenPlayedUseCase
import com.mustalk.minisimulator.domain.usecases.teams.GetTeamSizeUseCase
import com.mustalk.minisimulator.presentation.main.MainViewModel
import com.mustalk.minisimulator.presentation.matchresults.adapters.MatchResultAdapter
import com.mustalk.minisimulator.presentation.matchresults.adapters.OnMatchClickListener
import com.mustalk.minisimulator.presentation.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment for displaying match results.
 *
 * @author by MusTalK on 20/07/2024
 */

@AndroidEntryPoint
class MatchResultsFragment :
    Fragment(),
    OnMatchClickListener {
    private var _binding: FragmentMatchResultsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MatchResultAdapter
    private lateinit var mainViewModel: MainViewModel
    internal val matchResultsViewModel: MatchResultsViewModel by viewModels<MatchResultsViewModel>()

    @Inject
    lateinit var getMatchesUseCase: GetMatchesUseCase

    @Inject
    lateinit var hasMatchBeenPlayedUseCase: HasMatchBeenPlayedUseCase

    @Inject
    lateinit var getTeamSizeUseCase: GetTeamSizeUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMatchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter, with a click listener to show the round winners dialog
        adapter = MatchResultAdapter(this)

        // Set up the RecyclerView with the adapter
        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.resultsRecyclerView.adapter = adapter

        // Observe pairedMatches LiveData and update the adapter
        matchResultsViewModel.pairedMatches.observe(viewLifecycleOwner) { pairedMatches ->
            // we check if any match within the pairedMatches list has been played.
            val hasPlayedMatches =
                pairedMatches.any { innerList ->
                    innerList.any {
                        hasMatchBeenPlayedUseCase.invoke(it)
                    }
                }
            // We pass !hasPlayedMatches to toggleEmptyView().
            // This means the emptyView will be shown if no matches have been played (hasPlayedMatches is false) and hidden otherwise.
            toggleEmptyView(!hasPlayedMatches)

            // Update the adapter with the new paired matches
            adapter.submitList(pairedMatches)

            if (hasPlayedMatches) {
                binding.resultsRecyclerView.apply {
                    // Scroll to the last position on the list
                    val lastItemIndex = pairedMatches.size - 1
                    ViewUtils.smoothScrollToEndOfList(context, this, lastItemIndex)
                }
            }
        }

        // Observe the group standings LiveData from the MainViewModel
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.groupStandingsLiveData.observe(viewLifecycleOwner) { standings ->
            standings?.let {
                // Use GetMatchesUseCase to fetch matches from the GroupStandingsLiveData observed and generate paired matches
                val matches = getMatchesUseCase(it)
                val numTeams = getTeamSizeUseCase(it)
                matchResultsViewModel.generatePairedMatches(matches, numTeams)
            }
        }
    }

    override fun onMatchClick(
        roundMatches: List<Match>,
        roundNumber: Int,
    ) {
        ViewUtils.showToast(requireContext(), "Round $roundNumber")
    }

    // Toggle the visibility of the empty view and recyclerView based on the isEmpty value
    private fun toggleEmptyView(isEmpty: Boolean) {
        _binding?.let {
            it.resultsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            it.emptyViewContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    // Clean up the binding when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
