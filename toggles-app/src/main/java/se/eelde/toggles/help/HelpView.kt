package se.eelde.toggles.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HelpView() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Implementation", color = Color.White)
    }
}
