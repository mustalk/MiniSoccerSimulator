package com.mustalk.minisimulator.presentation.matchresults.adapters.ui

import android.widget.ImageView
import android.widget.TextView

/**
 * Data class for storing match winner views
 *
 * @author by MusTalK on 01/09/2024
 */

data class MatchWinnerViews(
    val label: TextView,
    val winnerIs: TextView,
    val teamLogo: ImageView,
    val teamName: TextView,
)
