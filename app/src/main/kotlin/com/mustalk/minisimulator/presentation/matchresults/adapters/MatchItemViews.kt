package com.mustalk.minisimulator.presentation.matchresults.adapters

import android.widget.ImageView
import android.widget.TextView

/**
 * Data class for storing match item views
 *
 * @author by MusTalK on 20/07/2024
 */

data class MatchItemViews(
    val homeTeamIcon: ImageView,
    val awayTeamIcon: ImageView,
    val homeTeamName: TextView,
    val awayTeamName: TextView,
    val homeTeamScore: TextView,
    val awayTeamScore: TextView,
)
