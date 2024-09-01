package com.mustalk.minisimulator.presentation.matchresults.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.CustomRoundWinnersDialogBinding
import com.mustalk.minisimulator.domain.entities.matches.Match
import com.mustalk.minisimulator.presentation.matchresults.adapters.RoundWinnersDialogAdapter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for displaying a dialog showing the winners of a round.
 * This class is responsible for creating and managing the dialog, populating it with match data and handling user interaction.
 *
 * @author by MusTalK on 01/09/2024
 */

@Singleton
class RoundWinnersDialogHelper
    @Inject
    constructor() {
        /**
         * Shows the round winners dialog.
         * @param context The context in which to display the dialog.
         * @param matches The list of matches for the round.
         * @param round The round number.
         */
        fun showRoundWinnersDialog(
            context: Context,
            matches: List<Match>,
            round: Int,
        ) {
            val binding = CustomRoundWinnersDialogBinding.inflate(LayoutInflater.from(context))
            val dialog =
                AlertDialog
                    .Builder(context)
                    .setView(binding.root)
                    .create()

            // Set match results
            binding.roundLabel.text = context.getString(R.string.round_number, round)
            val adapter = RoundWinnersDialogAdapter()
            binding.winnersRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }
            // Submit list to adapter
            adapter.submitList(matches)

            // Dismiss dialog on click
            binding.okButton.setOnClickListener { dialog.dismiss() }

            // Show dialog
            dialog.show()
        }
    }
