package com.mustalk.minisimulator.presentation.matchresults.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.DialogMatchWinnerItemBinding
import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.presentation.matchresults.adapters.ui.MatchWinnerViews

/**
 * Adapter for the round winners dialog.
 *
 * @author by MusTalK on 01/09/2024
 */

class RoundWinnersDialogAdapter : ListAdapter<Match, RoundWinnersDialogAdapter.MatchWinnerViewHolder>(MatchDiffCallback()) {
    class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(
            oldItem: Match,
            newItem: Match,
        ): Boolean = oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(
            oldItem: Match,
            newItem: Match,
        ): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MatchWinnerViewHolder {
        val binding = DialogMatchWinnerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchWinnerViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MatchWinnerViewHolder,
        position: Int,
    ) {
        val match = getItem(position)
        holder.bind(match, position + 1)
    }

    class MatchWinnerViewHolder(
        private val binding: DialogMatchWinnerItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(
            match: Match,
            matchNumber: Int,
        ) {
            // Set match winner views
            val matchWinnerViews = MatchWinnerViews(binding.matchLabel, binding.winnerIsLabel, binding.teamLogo, binding.teamName)
            setMatchWinnerView(context, matchNumber, match, matchWinnerViews)
        }

        // Helper function to update the match winner views based on match data
        private fun setMatchWinnerView(
            context: Context,
            matchNum: Int,
            match: Match,
            views: MatchWinnerViews,
        ) {
            // Set match number
            views.label.text = context.getString(R.string.label_dialog_match, matchNum)
            views.teamName.text =
                getMatchWinnerName(match).ifEmpty {
                    context.getString(R.string.label_dialog_draw)
                }
            // Set team logo, if its a draw, teamLogo is 0
            val teamLogo = getWinnerTeamLogo(match)
            if (teamLogo != 0) {
                views.teamLogo.setImageResource(teamLogo)
            }
            // If its a draw, hide label, team logo and name
            views.winnerIs.visibility = if (teamLogo == 0) View.GONE else View.VISIBLE
            views.teamLogo.visibility = if (teamLogo == 0) View.GONE else View.VISIBLE
        }

        // Helper function to get the winner name based on match data
        private fun getMatchWinnerName(match: Match): String {
            val winner =
                if (match.homeTeamScore > match.awayTeamScore) {
                    match.homeTeam.name
                } else if (match.awayTeamScore > match.homeTeamScore) {
                    match.awayTeam.name
                } else {
                    // return empty string if its a draw
                    ""
                }
            return winner
        }

        // Helper function to get team logo based on match data
        private fun getWinnerTeamLogo(match: Match): Int {
            val winnerLogo =
                if (match.homeTeamScore > match.awayTeamScore) {
                    match.homeTeam.teamLogo
                } else if (match.awayTeamScore > match.homeTeamScore) {
                    match.awayTeam.teamLogo
                } else {
                    0 // returns 0 if its a draw
                }
            return winnerLogo
        }
    }
}
