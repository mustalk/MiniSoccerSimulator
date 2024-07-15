package com.mustalk.minisimulator.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mustalk.minisimulator.R
import com.mustalk.minisimulator.databinding.ActivityMainBinding
import com.mustalk.minisimulator.presentation.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author by MusTalK on 15/07/2024
 *
 * Main activity of the application.
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up Toolbar
        setupSupportActionBar(binding.toolbar)

        ViewUtils.setupEdgeToEdgeDisplay(binding.root)
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
