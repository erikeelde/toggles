package se.eelde.toggles.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun HelpView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Text(text = "Implementation", color = Color.White)
    }
}
