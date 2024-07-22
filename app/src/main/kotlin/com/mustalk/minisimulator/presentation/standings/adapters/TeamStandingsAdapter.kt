package com.mustalk.minisimulator.presentation.standings.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.ItemGroupStandingBinding
import com.mustalk.minisimulator.presentation.standings.models.TeamStandingItem

/**
 * Adapter for the group standings RecyclerView.
 *
 * @author by MusTalK on 22/07/2024
 */

class TeamStandingsAdapter(
    private var qualifiers: List<String>,
    private var totalRounds: Int,
    private val previousTeamPositions: MutableMap<String, Int>,
    diffCallback: DiffUtil.ItemCallback<TeamStandingItem> =
        object : DiffUtil.ItemCallback<TeamStandingItem>() {
            override fun areItemsTheSame(
                oldItem: TeamStandingItem,
                newItem: TeamStandingItem,
            ): Boolean = oldItem.teamName == newItem.teamName

            override fun areContentsTheSame(
                oldItem: TeamStandingItem,
                newItem: TeamStandingItem,
            ): Boolean = oldItem == newItem
        },
) : ListAdapter<TeamStandingItem, TeamStandingsAdapter.TeamResultViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TeamResultViewHolder {
        val binding = ItemGroupStandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamResultViewHolder(binding, totalRounds, previousTeamPositions)
    }

    override fun onBindViewHolder(
        holder: TeamResultViewHolder,
        position: Int,
    ) {
        // Get the item from ListAdapter
        val item = getItem(position)
        holder.bind(item, qualifiers.contains(item.teamName))
    }

    fun updateQualifiers(
        newQualifiers: List<String>,
        totalRounds: Int,
    ) {
        // Update the qualifiers list and refresh the adapter to reflect the changes
        this.qualifiers = newQualifiers
        this.totalRounds = totalRounds
        notifyItemRangeChanged(0, itemCount)
    }

    // Enum for position change
    enum class PositionChange { UP, DOWN, SAME }

    class TeamResultViewHolder(
        private val binding: ItemGroupStandingBinding,
        private val totalRounds: Int,
        private val previousTeamPositions: MutableMap<String, Int>,
    ) : RecyclerView.ViewHolder(binding.root) {
        // Cache context for efficiency
        private val context = binding.root.context

        fun bind(
            item: TeamStandingItem,
            isQualifier: Boolean,
        ) {
            // Update position change indicator
            updateTeamPositionChange(item = item)

            // Set styling to teams advancing to the knockout stage once all the rounds are played
            if (isQualifier && item.pld == totalRounds) {
                applyQualifierStyling(context)
            } else {
                applyNonQualifierStyling()
            }

            // Set content
            binding.positionText.text = context.getString(R.string.label_position_text, bindingAdapterPosition + 1)
            binding.teamLogo.setImageResource(item.teamLogo)
            binding.clubName.text = item.teamName
            binding.columnPld.text = item.pld.toString()
            binding.columnW.text = item.w.toString()
            binding.columnD.text = item.d.toString()
            binding.columnL.text = item.l.toString()
            binding.columnGfGa.text = item.gfGa
            binding.columnGd.text = item.gd
            binding.columnPts.text = item.pts.toString()
        }

        private fun updateTeamPositionChange(item: TeamStandingItem) {
            // Update position change indicator based on position change
            val positionChange = getPositionChange(item)

            when (positionChange) {
                PositionChange.UP -> {
                    binding.posChangeIndicator.visibility = View.VISIBLE
                    binding.posChangeIndicator.setImageResource(R.drawable.ic_arrow_up)
                }
                PositionChange.DOWN -> {
                    binding.posChangeIndicator.setImageResource(R.drawable.ic_arrow_down)
                    binding.posChangeIndicator.visibility = View.VISIBLE
                }
                PositionChange.SAME -> binding.posChangeIndicator.visibility = View.INVISIBLE
            }

            // Store the current position as the previous position for the next round
            previousTeamPositions[item.teamName] = item.teamPosition
        }

        // Add a method to determine position change (you'll need to implement this based on your data)
        private fun getPositionChange(teamStanding: TeamStandingItem): PositionChange {
            val previousPosition = previousTeamPositions[teamStanding.teamName] ?: 0 // Get previous position
            return when {
                previousPosition == 0 -> PositionChange.SAME // Initial state, no change
                previousPosition > teamStanding.teamPosition -> PositionChange.UP
                previousPosition < teamStanding.teamPosition -> PositionChange.DOWN
                else -> PositionChange.SAME
            }
        }

        // Styling methods to highlight qualifier teams, changing backgrounds and adding text shadows
        private fun applyQualifierStyling(context: Context) {
            binding.positionContainer.setBackgroundResource(R.drawable.standing_item_position_highlight_overlay)
            binding.clItemStandingContainer.setBackgroundResource(R.drawable.standing_item_qualifier_background)
            binding.teamLogo.setBackgroundResource(R.drawable.ic_gold_ring)
            binding.clubName.apply {
                setShadowLayer(2f, 1f, 1f, context.getColor(R.color.black))
            }
        }

        // Styling methods for non qualifier teams, setting default backgrounds and text colors
        private fun applyNonQualifierStyling() {
            binding.positionContainer.setBackgroundResource(R.drawable.standing_item_position_overlay)
            binding.clItemStandingContainer.setBackgroundResource(R.drawable.standing_item_background)
            binding.teamLogo.setBackgroundResource(R.color.transparent)
        }
    }
}
