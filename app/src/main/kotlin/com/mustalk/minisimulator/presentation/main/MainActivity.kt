package com.mustalk.minisimulator.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.ActivityMainBinding
import com.mustalk.minisimulator.presentation.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity of the application.
 *
 * @author by MusTalK on 15/07/2024
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewUtils.setupEdgeToEdgeDisplay(binding.main)

        // Initializations
        setupSupportActionBar(binding.toolbar)
        setupAppBarButtons()
        initializeNavController()
        initViewModel()
    }

    // Set up click listeners and initial visibility for the app bar buttons.
    private fun setupAppBarButtons() {
        binding.btnSimulateMatches.setOnClickListener {
            ViewUtils.animateViewRotation(this, it)
            mainViewModel.simulateNextRoundMatches()
        }

        binding.btnStandings.setOnClickListener {
            showGroupStandings()
            it.post { ViewUtils.animateViewFlip(it) }
        }

        binding.btnMatchResults.setOnClickListener {
            showMatchResults()
            it.post { ViewUtils.animateViewFlip(it) }
        }

        // Initially hide btnMatchResults button as it's the default screen
        ViewUtils.setViewsVisibility(
            viewsToShow = listOf(binding.btnStandings),
            viewsToHide = listOf(binding.btnMatchResults)
        )

        // Set the title of the toolbar to the default screen
        setToolbarTitle(getString(R.string.title_round_match_results))
    }

    private fun initViewModel() {
        // Observe LiveData from the ViewModel to handle the ProgressBar and Toast messages
        mainViewModel.isLoading.observe(this) { isLoading ->
            ViewUtils.toggleViewVisibility(binding.contentMain.progressBar, isLoading)
        }
        mainViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                ViewUtils.showToast(this, it)
                mainViewModel.clearErrorMessage()
            }
        }

        mainViewModel.fetchTeams()
    }

    private fun initializeNavController() {
        val navHostFragment: NavHostFragment = binding.contentMain.navHostFragment.getFragment()
        navController = navHostFragment.navController
    }

    private fun showGroupStandings() {
        setToolbarTitle(getString(R.string.title_group_standings))

        // Navigate to GroupStandingsFragment
        navController.navigate(R.id.action_navMatchResultsFragment_to_navGroupStandingsFragment)

        // Show Match Results button and hide Group Standings button
        ViewUtils.setViewsVisibility(
            viewsToShow = listOf(binding.btnMatchResults),
            viewsToHide = listOf(binding.btnStandings)
        )
    }

    private fun showMatchResults() {
        setToolbarTitle(getString(R.string.title_round_match_results))

        // Navigate to MatchResultsFragment
        navController.navigate(R.id.action_navGroupStandingsFragment_to_navMatchResultsFragment)

        // Show Group Standings button and hide Match Results button
        ViewUtils.setViewsVisibility(
            viewsToShow = listOf(binding.btnStandings),
            viewsToHide = listOf(binding.btnMatchResults)
        )
    }

    private fun setupSupportActionBar(toolbar: Toolbar) {
        // Set up the support action bar
        setSupportActionBar(toolbar)
        setToolbarTitle(getString(R.string.app_name))
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    // Set the title of the toolbar.
    private fun setToolbarTitle(title: String) {
        binding.appTitle.text = title
    }

    // Handle navigation events
    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()
}
