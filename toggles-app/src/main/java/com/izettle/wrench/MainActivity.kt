package com.izettle.wrench

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.databinding.ActivityMainBinding
import se.eelde.toggles.notification.NotificationWorker

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navController = findNavController(R.id.nav_host_fragment) // https://issuetracker.google.com/issues/142847973
        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        setupActionBarWithNavController(navController, binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        NotificationWorker.scheduleNotification(this)
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, binding.drawerLayout)
}
