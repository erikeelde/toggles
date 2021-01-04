package se.eelde.toggles.bubble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.databinding.ActivityBubbleBinding

@AndroidEntryPoint
class BubbleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBubbleBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBubbleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navController = (supportFragmentManager.findFragmentById(binding.navHostFragment2.id) as NavHostFragment).navController
        // NavigationUI.setupActionBarWithNavController(this, navController)
    }
}
