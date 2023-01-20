package se.eelde.toggles.applications

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ApplicationListView(
    viewModel: ApplicationViewModel,
    navigateToConfigurations: (Long) -> Unit
) {
    val uiState = viewModel.state.collectAsState()

    val context = LocalContext.current

    uiState.value.let {
        Surface(modifier = Modifier.padding(16.dp)) {
            LazyColumn {
                it.applications.forEach { application ->
                    item {
                        var lol: ImageBitmap? = null
                        var secondaryText = ""
                        try {
                            val packageManager = context.packageManager
                            val icon: Drawable =
                                packageManager.getApplicationIcon(application.packageName)
                            lol = icon.toBitmap(width = 128, height = 128).asImageBitmap()
                        } catch (ignored: PackageManager.NameNotFoundException) {
                            secondaryText = stringResource(id = R.string.not_installed)
                        }

                        ListItem(
                            leadingContent = {
                                if (lol != null) {
                                    Image(
                                        painter = BitmapPainter(image = lol),
                                        contentDescription = "Application icon"
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_report_black_24dp),
                                        contentDescription = "Application not installed"
                                    )
                                }
                            },
                            headlineText = { Text(application.applicationLabel) },
                            modifier = Modifier.clickable {
                                navigateToConfigurations(application.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
