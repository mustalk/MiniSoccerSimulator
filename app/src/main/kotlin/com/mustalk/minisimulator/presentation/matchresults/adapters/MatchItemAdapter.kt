package com.mustalk.minisimulator.presentation.matchresults.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.MatchItemBinding
import com.mustalk.minisimulator.domain.entities.matches.Match

/**
 * Adapter for individual match items
 *
 * @author by MusTalK on 20/07/2024
 */

class MatchItemAdapter(
    private val onMatchClickListener: (Match) -> Unit,
) : ListAdapter<Match, MatchItemAdapter.MatchViewHolder>(MatchDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MatchViewHolder {
        val binding = MatchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MatchViewHolder,
        position: Int,
    ) {
        val match = getItem(position)
        holder.bind(match, onMatchClickListener)
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(
            oldItem: Match,
            newItem: Match,
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: Match,
            newItem: Match,
        ): Boolean = oldItem == newItem
    }

    class MatchViewHolder(
        private val binding: MatchItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(
            match: Match,
            onMatchClickListener: (Match) -> Unit,
        ) {
            binding.root.setOnClickListener {
                onMatchClickListener(match)
            }

            // Set the match data
            bindMatchData(
                context,
                match,
                MatchItemViews(
                    binding.homeTeamIcon,
                    binding.awayTeamIcon,
                    binding.homeTeamName,
                    binding.awayTeamName,
                    binding.homeTeamScore,
                    binding.awayTeamScore
                )
            )
        }

        // Helper function to avoid code duplication
        private fun bindMatchData(
            context: Context,
            match: Match,
            views: MatchItemViews,
        ) {
            views.homeTeamIcon.setImageResource(match.homeTeam.teamLogo)
            views.awayTeamIcon.setImageResource(match.awayTeam.teamLogo)
            views.homeTeamName.text = match.homeTeam.name
            views.awayTeamName.text = match.awayTeam.name

            views.homeTeamScore.text = match.homeTeamScore.toString()
            views.awayTeamScore.text = match.awayTeamScore.toString()

            val winningColor = context.getColor(R.color.winning_team_color)
            val losingColor = context.getColor(R.color.losing_team_color)
            val winnerRing = R.drawable.ic_green_ring
            val looserRing = R.drawable.ic_red_ring
            val drawRing = R.drawable.ic_grey_ring
            val drawColor = context.getColor(R.color.grey_500)

            // Set score colors based on the result win -> green, lose -> red, draw -> white
            when {
                match.homeTeamScore > match.awayTeamScore -> {
                    views.homeTeamScore.setTextColor(winningColor)
                    views.homeTeamIcon.setBackgroundResource(winnerRing)
                    views.awayTeamScore.setTextColor(losingColor)
                    views.awayTeamIcon.setBackgroundResource(looserRing)
                }
                match.homeTeamScore < match.awayTeamScore -> {
                    views.homeTeamScore.setTextColor(losingColor)
                    views.homeTeamIcon.setBackgroundResource(looserRing)
                    views.awayTeamScore.setTextColor(winningColor)
                    views.awayTeamIcon.setBackgroundResource(winnerRing)
                }
                else -> {
                    views.homeTeamScore.setTextColor(drawColor)
                    views.homeTeamIcon.setBackgroundResource(drawRing)
                    views.awayTeamScore.setTextColor(drawColor)
                    views.awayTeamIcon.setBackgroundResource(drawRing)
                }
            }
        }
    }
}
