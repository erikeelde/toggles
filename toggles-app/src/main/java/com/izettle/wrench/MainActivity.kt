package com.izettle.wrench

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import se.eelde.toggles.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // navController = findNavController(R.id.nav_host_fragment) // https://issuetracker.google.com/issues/142847973
        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController


        setupActionBarWithNavController(navController, drawerLayout)

        NavigationUI.setupWithNavController(nav_view, navController)
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, drawerLayout)

}
