package com.mustalk.minisimulator.presentation.matchresults.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.ItemMatchResultBinding
import com.mustalk.minisimulator.domain.entities.matches.Match

/**
 * Adapter for the match results RecyclerView.
 *
 * @author by MusTalK on 20/07/2024
 */

class MatchResultAdapter(
    private val onMatchClickListener: OnMatchClickListener,
) : ListAdapter<List<Match>, MatchResultAdapter.RoundViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RoundViewHolder {
        val binding = ItemMatchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoundViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RoundViewHolder,
        position: Int,
    ) {
        val round = getItem(position)
        holder.bind(round, position + 1, onMatchClickListener)
    }

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<List<Match>>() {
                override fun areItemsTheSame(
                    oldItem: List<Match>,
                    newItem: List<Match>,
                ): Boolean = oldItem.firstOrNull()?.hashCode() == newItem.firstOrNull()?.hashCode()

                override fun areContentsTheSame(
                    oldItem: List<Match>,
                    newItem: List<Match>,
                ): Boolean =
                    oldItem.firstOrNull()?.homeTeam == newItem.firstOrNull()?.homeTeam &&
                        oldItem.firstOrNull()?.awayTeam == newItem.firstOrNull()?.awayTeam
            }
    }

    class RoundViewHolder(
        private val binding: ItemMatchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            round: List<Match>,
            roundNumber: Int,
            onMatchClickListener: OnMatchClickListener,
        ) {
            // Set up the RecyclerView for this round
            val matchesAdapter =
                MatchItemAdapter {
                    // Handle match click event, we want it to trigger the onMatchClickListener to show the round winners dialog
                    onMatchClickListener.onMatchClick(round, roundNumber)
                }
            binding.roundTextView.text = itemView.context.getString(R.string.round_number, roundNumber)
            binding.matchesRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = matchesAdapter
            }
            // Submit the list of matches for this round
            matchesAdapter.submitList(round)

            // Set click listener for the entire round
            itemView.setOnClickListener {
                // Trigger the onMatchClickListener to show the round winners dialog
                onMatchClickListener.onMatchClick(round, roundNumber)
            }
        }
    }
}
