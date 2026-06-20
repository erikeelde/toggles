package se.eelde.toggles.composetheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Chrome for a leaf editor when it is presented as a dialog (the editor-presentation toggle is on).
 *
 * Unlike a full-screen [androidx.compose.material3.Scaffold], this sizes to its content height,
 * clamped to a sensible [min, max] range: short editors (e.g. boolean) stay compact without
 * looking awkwardly tiny, while taller editors (e.g. a list) cap out and scroll their content
 * internally. The non-dialog presentation keeps using a Scaffold and is unaffected.
 *
 * Per Material guidance a dialog is dismissed, not navigated up from, so there is no up/back
 * navigation icon — it is dismissed via its own actions, the scrim, or system back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToggleEditorDialog(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 560.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            content()
        }
    }
}
