package se.eelde.toggles.bubble

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import se.eelde.toggles.R
import se.eelde.toggles.TogglesUriMatcher
import se.eelde.toggles.TogglesUriMatcher.Companion.APPLICATION_ID
import se.eelde.toggles.databinding.ActivityBubbleBinding

@AndroidEntryPoint
class BubbleActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityBubbleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBubbleBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_bubble)

        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        binding.root.postDelayed({
            when (TogglesUriMatcher.getTogglesUriMatcher().match(intent.data)) {
                APPLICATION_ID -> {
                    val applicationId = intent.data!!.lastPathSegment!!.toLong()
                    Navigation.findNavController(binding.root).navigate(
                        BubbleActivityDirections.actionBubbleActivityToConfigurationsFragment(
                            applicationId
                        )
                    )
                }
                else -> {
                    throw IllegalArgumentException("This activity does not support this uri: ${intent.data}")
                }
            }
        }, 5000)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }
}
